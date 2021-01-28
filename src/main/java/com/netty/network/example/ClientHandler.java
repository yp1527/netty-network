package com.netty.network.test;


import com.netty.network.channel.ChannelHandlerContext;
import com.netty.network.channel.SimpleChannelInboundHandler;

/**
 * @author 杨平
 * @date 2021/1/20
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        //打印服务端的发送数据
        System.out.println(s);
    }
}
