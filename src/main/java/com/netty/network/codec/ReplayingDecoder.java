/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.netty.network.codec;


import com.netty.network.buffer.ByteBuf;
import com.netty.network.buffer.Unpooled;
import com.netty.network.channel.ChannelHandlerContext;
import com.netty.network.util.Signal;
import com.netty.network.util.internal.StringUtil;

import java.util.List;


public abstract class ReplayingDecoder<S> extends ByteToMessageDecoder {

    static final Signal REPLAY = Signal.valueOf(ReplayingDecoder.class, "REPLAY");

    private final ReplayingDecoderByteBuf replayable = new ReplayingDecoderByteBuf();
    private S state;
    private int checkpoint = -1;

    /**
     * Creates a new instance with no initial state (i.e: {@code null}).
     */
    protected ReplayingDecoder() {
        this(null);
    }

    /**
     * Creates a new instance with the specified initial state.
     */
    protected ReplayingDecoder(S initialState) {
        state = initialState;
    }

    /**
     * Stores the internal cumulative buffer's reader position.
     */
    protected void checkpoint() {
        checkpoint = internalBuffer().readerIndex();
    }

    /**
     * Stores the internal cumulative buffer's reader position and updates
     * the current decoder state.
     */
    protected void checkpoint(S state) {
        checkpoint();
        state(state);
    }

    /**
     * Returns the current state of this decoder.
     * @return the current state of this decoder
     */
    protected S state() {
        return state;
    }

    /**
     * Sets the current state of this decoder.
     * @return the old state of this decoder
     */
    protected S state(S newState) {
        S oldState = state;
        state = newState;
        return oldState;
    }

    @Override
    final void channelInputClosed(ChannelHandlerContext ctx, List<Object> out) throws Exception {
        try {
            replayable.terminate();
            if (cumulation != null) {
                callDecode(ctx, internalBuffer(), out);
                decodeLast(ctx, replayable, out);
            } else {
                replayable.setCumulation(Unpooled.EMPTY_BUFFER);
                decodeLast(ctx, replayable, out);
            }
        } catch (Signal replay) {
            // Ignore
            replay.expect(REPLAY);
        }
    }

    @Override
    protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        replayable.setCumulation(in);
        try {
            while (in.isReadable()) {
                int oldReaderIndex = checkpoint = in.readerIndex();
                int outSize = out.size();

                if (outSize > 0) {
                    fireChannelRead(ctx, out, outSize);
                    out.clear();

                    // Check if this handler was removed before continuing with decoding.
                    // If it was removed, it is not safe to continue to operate on the buffer.
                    //
                    // See:
                    // - https://github.com/netty/netty/issues/4635
                    if (ctx.isRemoved()) {
                        break;
                    }
                    outSize = 0;
                }

                S oldState = state;
                int oldInputLength = in.readableBytes();
                try {
                    decode(ctx, replayable, out);

                    // Check if this handler was removed before continuing the loop.
                    // If it was removed, it is not safe to continue to operate on the buffer.
                    //
                    // See https://github.com/netty/netty/issues/1664
                    if (ctx.isRemoved()) {
                        break;
                    }

                    if (outSize == out.size()) {
                        if (oldInputLength == in.readableBytes() && oldState == state) {
                            throw new DecoderException(
                                    StringUtil.simpleClassName(getClass()) + ".decode() must consume the inbound " +
                                    "data or change its state if it did not decode anything.");
                        } else {
                            // Previous data has been discarded or caused state transition.
                            // Probably it is reading on.
                            continue;
                        }
                    }
                } catch (Signal replay) {
                    replay.expect(REPLAY);

                    // Check if this handler was removed before continuing the loop.
                    // If it was removed, it is not safe to continue to operate on the buffer.
                    //
                    // See https://github.com/netty/netty/issues/1664
                    if (ctx.isRemoved()) {
                        break;
                    }

                    // Return to the checkpoint (or oldPosition) and retry.
                    int checkpoint = this.checkpoint;
                    if (checkpoint >= 0) {
                        in.readerIndex(checkpoint);
                    } else {
                        // Called by cleanup() - no need to maintain the readerIndex
                        // anymore because the buffer has been released already.
                    }
                    break;
                }

                if (oldReaderIndex == in.readerIndex() && oldState == state) {
                    throw new DecoderException(
                           StringUtil.simpleClassName(getClass()) + ".decode() method must consume the inbound data " +
                           "or change its state if it decoded something.");
                }
                if (isSingleDecode()) {
                    break;
                }
            }
        } catch (DecoderException e) {
            throw e;
        } catch (Throwable cause) {
            throw new DecoderException(cause);
        }
    }
}
