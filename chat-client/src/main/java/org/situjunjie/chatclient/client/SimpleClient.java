package org.situjunjie.chatclient.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.situjunjie.chatclient.handler.EchoHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @className: SimpleClient
 * @description: 简单的ChatIM客户端
 * @author: situjunjie
 * @date: 2022/3/19
 **/
@Service
@Slf4j
public class SimpleClient {

    @Value("${im.server.ip}")
    private String serverAddr;

    @Value("${im.server.port}")
    private Integer serverPort;

    private EventLoopGroup workerGroup;

    @Autowired
    private EchoHandler echoHandler;

    private Scanner sc = new Scanner(System.in);

    public void run(){
        Bootstrap bootstrap = new Bootstrap();
        workerGroup = new NioEventLoopGroup();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(echoHandler);
                    }
                });
        try {
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(serverAddr, serverPort)).sync();
            if (channelFuture.isSuccess()){
                log.info("连接服务器成功");
            }
            while(true){
                log.info("输入消息:");
                String s = sc.nextLine();
                ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
                byteBuf.writeBytes(s.getBytes(StandardCharsets.UTF_8));
                channelFuture.channel().writeAndFlush(byteBuf);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
