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
package com.netty.network.channel;


import com.netty.network.util.internal.ObjectUtil;
import com.netty.network.util.internal.PlatformDependent;
import com.netty.network.buffer.ByteBufAllocator;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


/**
 * The default {@link ChannelConfig} implementation.
 */
public class DefaultChannelConfig implements ChannelConfig {
    private static final MessageSizeEstimator DEFAULT_MSG_SIZE_ESTIMATOR = DefaultMessageSizeEstimator.DEFAULT;

    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;

    private static final AtomicIntegerFieldUpdater<DefaultChannelConfig> AUTOREAD_UPDATER;
    private static final AtomicReferenceFieldUpdater<DefaultChannelConfig, WriteBufferWaterMark> WATERMARK_UPDATER;

    static {
        AtomicIntegerFieldUpdater<DefaultChannelConfig> autoReadUpdater =
            PlatformDependent.newAtomicIntegerFieldUpdater(DefaultChannelConfig.class, "autoRead");
        if (autoReadUpdater == null) {
            autoReadUpdater = AtomicIntegerFieldUpdater.newUpdater(DefaultChannelConfig.class, "autoRead");
        }
        AUTOREAD_UPDATER = autoReadUpdater;

        AtomicReferenceFieldUpdater<DefaultChannelConfig, WriteBufferWaterMark> watermarkUpdater =
                PlatformDependent.newAtomicReferenceFieldUpdater(DefaultChannelConfig.class, "writeBufferWaterMark");
        if (watermarkUpdater == null) {
            watermarkUpdater = AtomicReferenceFieldUpdater.newUpdater(
                    DefaultChannelConfig.class, WriteBufferWaterMark.class, "writeBufferWaterMark");
        }
        WATERMARK_UPDATER = watermarkUpdater;
    }

    protected final Channel channel;

    private volatile ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
    private volatile RecvByteBufAllocator rcvBufAllocator;
    private volatile MessageSizeEstimator msgSizeEstimator = DEFAULT_MSG_SIZE_ESTIMATOR;

    private volatile int connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT;
    private volatile int writeSpinCount = 16;
    @SuppressWarnings("FieldMayBeFinal")
    private volatile int autoRead = 1;
    private volatile boolean autoClose = true;
    private volatile WriteBufferWaterMark writeBufferWaterMark = WriteBufferWaterMark.DEFAULT;
    private volatile boolean pinEventExecutor = true;

    public DefaultChannelConfig(Channel channel) {
        this(channel, new AdaptiveRecvByteBufAllocator());
    }

    protected DefaultChannelConfig(Channel channel, RecvByteBufAllocator allocator) {
        setRecvByteBufAllocator(allocator, channel.metadata());
        this.channel = channel;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(
                null,
                ChannelOption.CONNECT_TIMEOUT_MILLIS, ChannelOption.MAX_MESSAGES_PER_READ, ChannelOption.WRITE_SPIN_COUNT,
                ChannelOption.ALLOCATOR, ChannelOption.AUTO_READ, ChannelOption.AUTO_CLOSE, ChannelOption.RCVBUF_ALLOCATOR, ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK,
                ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, ChannelOption.WRITE_BUFFER_WATER_MARK, ChannelOption.MESSAGE_SIZE_ESTIMATOR,
                ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP);
    }

    protected Map<ChannelOption<?>, Object> getOptions(
            Map<ChannelOption<?>, Object> result, ChannelOption<?>... options) {
        if (result == null) {
            result = new IdentityHashMap<ChannelOption<?>, Object>();
        }
        for (ChannelOption<?> o: options) {
            result.put(o, getOption(o));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean setOptions(Map<ChannelOption<?>, ?> options) {
        if (options == null) {
            throw new NullPointerException("options");
        }

        boolean setAllOptions = true;
        for (Entry<ChannelOption<?>, ?> e: options.entrySet()) {
            if (!setOption((ChannelOption<Object>) e.getKey(), e.getValue())) {
                setAllOptions = false;
            }
        }

        return setAllOptions;
    }

    @Override
    @SuppressWarnings({ "unchecked", "deprecation" })
    public <T> T getOption(ChannelOption<T> option) {
        if (option == null) {
            throw new NullPointerException("option");
        }

        if (option == ChannelOption.CONNECT_TIMEOUT_MILLIS) {
            return (T) Integer.valueOf(getConnectTimeoutMillis());
        }
        if (option == ChannelOption.MAX_MESSAGES_PER_READ) {
            return (T) Integer.valueOf(getMaxMessagesPerRead());
        }
        if (option == ChannelOption.WRITE_SPIN_COUNT) {
            return (T) Integer.valueOf(getWriteSpinCount());
        }
        if (option == ChannelOption.ALLOCATOR) {
            return (T) getAllocator();
        }
        if (option == ChannelOption.RCVBUF_ALLOCATOR) {
            return (T) getRecvByteBufAllocator();
        }
        if (option == ChannelOption.AUTO_READ) {
            return (T) Boolean.valueOf(isAutoRead());
        }
        if (option == ChannelOption.AUTO_CLOSE) {
            return (T) Boolean.valueOf(isAutoClose());
        }
        if (option == ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK) {
            return (T) Integer.valueOf(getWriteBufferHighWaterMark());
        }
        if (option == ChannelOption.WRITE_BUFFER_LOW_WATER_MARK) {
            return (T) Integer.valueOf(getWriteBufferLowWaterMark());
        }
        if (option == ChannelOption.WRITE_BUFFER_WATER_MARK) {
            return (T) getWriteBufferWaterMark();
        }
        if (option == ChannelOption.MESSAGE_SIZE_ESTIMATOR) {
            return (T) getMessageSizeEstimator();
        }
        if (option == ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP) {
            return (T) Boolean.valueOf(getPinEventExecutorPerGroup());
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == ChannelOption.CONNECT_TIMEOUT_MILLIS) {
            setConnectTimeoutMillis((Integer) value);
        } else if (option == ChannelOption.MAX_MESSAGES_PER_READ) {
            setMaxMessagesPerRead((Integer) value);
        } else if (option == ChannelOption.WRITE_SPIN_COUNT) {
            setWriteSpinCount((Integer) value);
        } else if (option == ChannelOption.ALLOCATOR) {
            setAllocator((ByteBufAllocator) value);
        } else if (option == ChannelOption.RCVBUF_ALLOCATOR) {
            setRecvByteBufAllocator((RecvByteBufAllocator) value);
        } else if (option == ChannelOption.AUTO_READ) {
            setAutoRead((Boolean) value);
        } else if (option == ChannelOption.AUTO_CLOSE) {
            setAutoClose((Boolean) value);
        } else if (option == ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK) {
            setWriteBufferHighWaterMark((Integer) value);
        } else if (option == ChannelOption.WRITE_BUFFER_LOW_WATER_MARK) {
            setWriteBufferLowWaterMark((Integer) value);
        } else if (option == ChannelOption.WRITE_BUFFER_WATER_MARK) {
            setWriteBufferWaterMark((WriteBufferWaterMark) value);
        } else if (option == ChannelOption.MESSAGE_SIZE_ESTIMATOR) {
            setMessageSizeEstimator((MessageSizeEstimator) value);
        } else if (option == ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP) {
            setPinEventExecutorPerGroup((Boolean) value);
        } else {
            return false;
        }

        return true;
    }

    protected <T> void validate(ChannelOption<T> option, T value) {
        if (option == null) {
            throw new NullPointerException("option");
        }
        option.validate(value);
    }

    @Override
    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    @Override
    public ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        if (connectTimeoutMillis < 0) {
            throw new IllegalArgumentException(String.format(
                    "connectTimeoutMillis: %d (expected: >= 0)", connectTimeoutMillis));
        }
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * @throws IllegalStateException if {@link #getRecvByteBufAllocator()} does not return an object of type
     * {@link MaxMessagesRecvByteBufAllocator}.
     */
    @Override
    @Deprecated
    public int getMaxMessagesPerRead() {
        try {
            MaxMessagesRecvByteBufAllocator allocator = getRecvByteBufAllocator();
            return allocator.maxMessagesPerRead();
        } catch (ClassCastException e) {
            throw new IllegalStateException("getRecvByteBufAllocator() must return an object of type " +
                    "MaxMessagesRecvByteBufAllocator", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * @throws IllegalStateException if {@link #getRecvByteBufAllocator()} does not return an object of type
     * {@link MaxMessagesRecvByteBufAllocator}.
     */
    @Override
    @Deprecated
    public ChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        try {
            MaxMessagesRecvByteBufAllocator allocator = getRecvByteBufAllocator();
            allocator.maxMessagesPerRead(maxMessagesPerRead);
            return this;
        } catch (ClassCastException e) {
            throw new IllegalStateException("getRecvByteBufAllocator() must return an object of type " +
                    "MaxMessagesRecvByteBufAllocator", e);
        }
    }

    @Override
    public int getWriteSpinCount() {
        return writeSpinCount;
    }

    @Override
    public ChannelConfig setWriteSpinCount(int writeSpinCount) {
        if (writeSpinCount <= 0) {
            throw new IllegalArgumentException(
                    "writeSpinCount must be a positive integer.");
        }
        this.writeSpinCount = writeSpinCount;
        return this;
    }

    @Override
    public ByteBufAllocator getAllocator() {
        return allocator;
    }

    @Override
    public ChannelConfig setAllocator(ByteBufAllocator allocator) {
        if (allocator == null) {
            throw new NullPointerException("allocator");
        }
        this.allocator = allocator;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecvByteBufAllocator> T getRecvByteBufAllocator() {
        return (T) rcvBufAllocator;
    }

    @Override
    public ChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        rcvBufAllocator = ObjectUtil.checkNotNull(allocator, "allocator");
        return this;
    }

    /**
     * Set the {@link RecvByteBufAllocator} which is used for the channel to allocate receive buffers.
     * @param allocator the allocator to set.
     * @param metadata Used to set the {@link ChannelMetadata#defaultMaxMessagesPerRead()} if {@code allocator}
     * is of type {@link MaxMessagesRecvByteBufAllocator}.
     */
    private void setRecvByteBufAllocator(RecvByteBufAllocator allocator, ChannelMetadata metadata) {
        if (allocator instanceof MaxMessagesRecvByteBufAllocator) {
            ((MaxMessagesRecvByteBufAllocator) allocator).maxMessagesPerRead(metadata.defaultMaxMessagesPerRead());
        } else if (allocator == null) {
            throw new NullPointerException("allocator");
        }
        rcvBufAllocator = allocator;
    }

    @Override
    public boolean isAutoRead() {
        return autoRead == 1;
    }

    @Override
    public ChannelConfig setAutoRead(boolean autoRead) {
        boolean oldAutoRead = AUTOREAD_UPDATER.getAndSet(this, autoRead ? 1 : 0) == 1;
        if (autoRead && !oldAutoRead) {
            channel.read();
        } else if (!autoRead && oldAutoRead) {
            autoReadCleared();
        }
        return this;
    }

    /**
     * Is called once {@link #setAutoRead(boolean)} is called with {@code false} and {@link #isAutoRead()} was
     * {@code true} before.
     */
    protected void autoReadCleared() { }

    @Override
    public boolean isAutoClose() {
        return autoClose;
    }

    @Override
    public ChannelConfig setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
        return this;
    }

    @Override
    public int getWriteBufferHighWaterMark() {
        return writeBufferWaterMark.high();
    }

    @Override
    public ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        if (writeBufferHighWaterMark < 0) {
            throw new IllegalArgumentException(
                    "writeBufferHighWaterMark must be >= 0");
        }
        for (;;) {
            WriteBufferWaterMark waterMark = writeBufferWaterMark;
            if (writeBufferHighWaterMark < waterMark.low()) {
                throw new IllegalArgumentException(
                        "writeBufferHighWaterMark cannot be less than " +
                                "writeBufferLowWaterMark (" + waterMark.low() + "): " +
                                writeBufferHighWaterMark);
            }
            if (WATERMARK_UPDATER.compareAndSet(this, waterMark,
                    new WriteBufferWaterMark(waterMark.low(), writeBufferHighWaterMark, false))) {
                return this;
            }
        }
    }

    @Override
    public int getWriteBufferLowWaterMark() {
        return writeBufferWaterMark.low();
    }

    @Override
    public ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        if (writeBufferLowWaterMark < 0) {
            throw new IllegalArgumentException(
                    "writeBufferLowWaterMark must be >= 0");
        }
        for (;;) {
            WriteBufferWaterMark waterMark = writeBufferWaterMark;
            if (writeBufferLowWaterMark > waterMark.high()) {
                throw new IllegalArgumentException(
                        "writeBufferLowWaterMark cannot be greater than " +
                                "writeBufferHighWaterMark (" + waterMark.high() + "): " +
                                writeBufferLowWaterMark);
            }
            if (WATERMARK_UPDATER.compareAndSet(this, waterMark,
                    new WriteBufferWaterMark(writeBufferLowWaterMark, waterMark.high(), false))) {
                return this;
            }
        }
    }

    @Override
    public ChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        this.writeBufferWaterMark = ObjectUtil.checkNotNull(writeBufferWaterMark, "writeBufferWaterMark");
        return this;
    }

    @Override
    public WriteBufferWaterMark getWriteBufferWaterMark() {
        return writeBufferWaterMark;
    }

    @Override
    public MessageSizeEstimator getMessageSizeEstimator() {
        return msgSizeEstimator;
    }

    @Override
    public ChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        if (estimator == null) {
            throw new NullPointerException("estimator");
        }
        msgSizeEstimator = estimator;
        return this;
    }

    private ChannelConfig setPinEventExecutorPerGroup(boolean pinEventExecutor) {
        this.pinEventExecutor = pinEventExecutor;
        return this;
    }

    private boolean getPinEventExecutorPerGroup() {
        return pinEventExecutor;
    }

}