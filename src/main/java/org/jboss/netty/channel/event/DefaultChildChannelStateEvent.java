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
package org.jboss.netty.channel.event;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import static org.jboss.netty.channel.Channels.*;

/**
 * The default {@link ChildChannelStateEvent} implementation.
 */
public class DefaultChildChannelStateEvent implements ChildChannelStateEvent {

    private final Channel parentChannel;
    private final Channel childChannel;

    /**
     * Creates a new instance.
     */
    public DefaultChildChannelStateEvent(Channel parentChannel, Channel childChannel) {
        if (parentChannel == null) {
            throw new NullPointerException("parentChannel");
        }
        if (childChannel == null) {
            throw new NullPointerException("childChannel");
        }
        this.parentChannel = parentChannel;
        this.childChannel = childChannel;
    }

    public Channel getChannel() {
        return parentChannel;
    }

    public ChannelFuture getFuture() {
        return succeededFuture(getChannel());
    }

    public Channel getChildChannel() {
        return childChannel;
    }

    @Override
    public String toString() {
        String channelString = getChannel().toString();
        StringBuilder buf = new StringBuilder(channelString.length() + 32);
        buf.append(channelString);
        buf.append(getChildChannel().isOpen()? " CHILD_OPEN: " : " CHILD_CLOSED: ");
        buf.append(getChildChannel().getId());
        return buf.toString();
    }
}