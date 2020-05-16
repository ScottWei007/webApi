package com.scottwei.mt5webapi.mt5Api.nettyTcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Scott Wei on 8/3/2018.
 */
public class Server {
    private final Logger logger = LoggerFactory.getLogger(Server.class);
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private static final int bossThreads = 1;
    private static final int workerThreads = Runtime.getRuntime().availableProcessors() << 1;
    private static final int bossiIoRatio = 100;
    private static final int workerIoRatio = 100;
    private ServerBootstrap bootstrap;
    private final ProtocolEncoder protocolEncoder = new ProtocolEncoder();

    @Value("${socketserver.port}")
    private int port = 28888;

    //初始化bean時實行該方法
    @PostConstruct
    public void start() {
        try {
            initEventLoopGroup();
            bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(0,20,0));
                            pipeline.addLast(new ProtocolDecoder());
                            pipeline.addLast(protocolEncoder);
                            pipeline.addLast(new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(512 * 1024, 1024 * 1024))
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);
            if(Epoll.isAvailable()) {
                bootstrap.option(EpollChannelOption.SO_REUSEPORT, false)
                        .option(EpollChannelOption.IP_FREEBIND, false)
                        .option(EpollChannelOption.IP_TRANSPARENT, false)
                        .option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED)
                        .childOption(EpollChannelOption.TCP_CORK, false)
                        .childOption(EpollChannelOption.TCP_QUICKACK, true)
                        .childOption(EpollChannelOption.IP_TRANSPARENT, false)
                        .childOption(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
            }
            ChannelFuture future = bootstrap.bind(new InetSocketAddress(port)).sync();
            logger.info("=============>tcp server start.");
            // wait until the server socket is closed,這裏阻塞住了
//            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("=============>tcp server error: " + e);
            e.printStackTrace();
        }
    }

    //關閉時釋放資源
    @PreDestroy
    public void destroy(){
        boss.shutdownGracefully().syncUninterruptibly();
        worker.shutdownGracefully().syncUninterruptibly();
    }

    private final void initEventLoopGroup() {
        ThreadFactory boosTF = new DefaultThreadFactory("mt5webapi.netty.boos",Thread.MAX_PRIORITY);
        ThreadFactory workerTF = new DefaultThreadFactory("mt5webapi.netty.worker",Thread.MAX_PRIORITY);
        if(Epoll.isAvailable()) {
            EpollEventLoopGroup bossEpollEventLoopGroup = new EpollEventLoopGroup(bossThreads, boosTF);
            bossEpollEventLoopGroup.setIoRatio(bossiIoRatio);
            boss = bossEpollEventLoopGroup;
            EpollEventLoopGroup workerEpollEventLoopGroup = new EpollEventLoopGroup(workerThreads, workerTF);
            workerEpollEventLoopGroup.setIoRatio(workerIoRatio);
            worker = workerEpollEventLoopGroup;
        }else {
            NioEventLoopGroup bossNioEventLoopGroup = new NioEventLoopGroup(bossThreads, boosTF);
            bossNioEventLoopGroup.setIoRatio(bossiIoRatio);
            boss = bossNioEventLoopGroup;
            NioEventLoopGroup workerNioEventLoopGroup = new NioEventLoopGroup(workerThreads, workerTF);
            workerNioEventLoopGroup.setIoRatio(workerIoRatio);
            worker = workerNioEventLoopGroup;
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
