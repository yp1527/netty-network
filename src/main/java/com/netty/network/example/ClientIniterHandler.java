package com.netty.network.example;


import com.netty.network.channel.ChannelInitializer;
import com.netty.network.channel.ChannelPipeline;
import com.netty.network.channel.socket.SocketChannel;
import com.netty.network.codec.string.StringDecoder;
import com.netty.network.codec.string.StringEncoder;

/**
 * @author 杨平
 * @date 2021/1/20
 */
public class ClientIniterHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //注册管道
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        //pipeline.addLast("http", new HttpClientCodec());
        pipeline.addLast("chat", new ClientHandler());
    }
}