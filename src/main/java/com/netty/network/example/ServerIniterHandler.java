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
public class ServerIniterHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //管道注册handler
        ChannelPipeline pipeline = socketChannel.pipeline();
        //编码通道处理
        pipeline.addLast("decode", new StringDecoder());
        //转码通道处理
        pipeline.addLast("encode", new StringEncoder());
        //聊天服务通道处理
        pipeline.addLast("chat", new ServerHandler());
    }
}
