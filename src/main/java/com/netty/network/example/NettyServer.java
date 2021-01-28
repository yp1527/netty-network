package com.netty.network.example;

import com.netty.network.channel.ChannelOption;
import com.netty.network.channel.EventLoopGroup;
import com.netty.network.bootstrap.ServerBootstrap;
import com.netty.network.channel.Channel;
import com.netty.network.channel.nio.NioEventLoopGroup;
import com.netty.network.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    /**
     * 端口
     */
    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void run() {
        //EventLoopGroup是用来处理IO操作的多线程事件循环器
        //负责接收客户端连接线程
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //负责处理客户端i/o事件、task任务、监听任务组
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //启动 NIO 服务的辅助启动类
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        //配置 Channel
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ServerIniterHandler());
        //BACKLOG用于构造服务端套接字ServerSocket对象，
        // 标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        //是否启用心跳保活机制
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            //绑定服务端口监听
            Channel channel = bootstrap.bind(port).sync().channel();
            System.out.println("server run in port " + port);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //关闭事件流组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyServer(8899).run();
    }
}
