package com.netty.network.example;


import com.netty.network.channel.EventLoopGroup;
import com.netty.network.bootstrap.Bootstrap;
import com.netty.network.channel.Channel;
import com.netty.network.channel.nio.NioEventLoopGroup;
import com.netty.network.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author 杨平
 * @date 2021/1/20
 */
public class NettyClient {

    private String ip;

    private int port;

    private boolean stop = false;

    public NettyClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void run() throws IOException {
        //设置一个多线程循环器
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //启动附注类
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        //指定所使用的NIO传输channel
        bootstrap.channel(NioSocketChannel.class);
        //指定客户端初始化处理
        bootstrap.handler(new ClientIniterHandler());
        try {
            //连接服务
            Channel channel = bootstrap.connect(ip, port).sync().channel();
            while (true) {
                //向服务端发送内容
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String content = reader.readLine();
                if (content!=null && !content.equals("")) {
                    if (content.equalsIgnoreCase("q")) {
                        System.exit(1);
                    }
                    channel.writeAndFlush(content);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new NettyClient("localhost", 8899).run();
    }
}
