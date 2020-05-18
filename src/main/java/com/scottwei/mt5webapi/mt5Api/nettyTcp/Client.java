package com.scottwei.mt5webapi.mt5Api.nettyTcp;

import com.scottwei.mt5webapi.mt5Api.manager.ClientManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Scott Wei
 * @date 2019/7/30 10:16
 **/
@Component
public class Client {
    private final Logger logger = LoggerFactory.getLogger(Client.class);

    public Bootstrap bootstrap;
    private final int workerThreads = Runtime.getRuntime().availableProcessors() << 1;
    private final int workerIoRatio = 50;
    private final ClientIdleStateTrigger clientIdleStateTrigger = new ClientIdleStateTrigger();
    private final ProtocolEncoder protocolEncoder = new ProtocolEncoder();
    public final EventLoopGroup worker = initWorkerEventLoopGroup();
    private final ClientHandler clientHandler = new ClientHandler(this);
    @Value("${socketserver.host}")
    public String host;
    @Value("${socketserver.port}")
    public int port;

    public final ChannelFutureListener channelFutureListener = cf -> {
        if (!cf.isSuccess()) {
            logger.info("=============>tcp client start error.");
            worker.schedule(() -> {
                ChannelFuture cf1 = bootstrap.connect(new InetSocketAddress(host, port));
                cf1.addListener(this.channelFutureListener);
            }, 3, TimeUnit.SECONDS);
        } else {
            ClientManager.getInstance().setChannel(cf.channel());
            logger.info("=============>tcp client start success.");
        }
    };

    //init 初始化方法
    @PostConstruct
    public void start(){
        bootstrap = initBootstrap();
        ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
        channelFuture.addListener(channelFutureListener);
    }

    //destroy
    @PreDestroy
    public void destroy(){
        ClientManager.getInstance().closeChannel();
        worker.shutdownGracefully().syncUninterruptibly();
    }

    private final Bootstrap initBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(0,20,0));
                        pipeline.addLast(clientIdleStateTrigger);
                        pipeline.addLast(new ProtocolDecoder());
                        pipeline.addLast(protocolEncoder);
                        pipeline.addLast(clientHandler);
                    }
                });
        bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(512 * 1024, 1024 * 1024))
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.ALLOW_HALF_CLOSURE, false);
        if(Epoll.isAvailable()) {
            bootstrap.option(EpollChannelOption.TCP_CORK, false)
                    .option(EpollChannelOption.TCP_QUICKACK, true)
                    .option(EpollChannelOption.IP_TRANSPARENT, false)
                    .option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
        }
        return bootstrap;
    }

    private final EventLoopGroup initWorkerEventLoopGroup() {
        ThreadFactory workerTF = new DefaultThreadFactory("mt5webapi.netty.worker",Thread.MAX_PRIORITY);
        if(Epoll.isAvailable()) {
            EpollEventLoopGroup workerEpollEventLoopGroup = new EpollEventLoopGroup(workerThreads, workerTF);
            workerEpollEventLoopGroup.setIoRatio(workerIoRatio);
            return workerEpollEventLoopGroup;
        }else {
            NioEventLoopGroup workerNioEventLoopGroup = new NioEventLoopGroup(workerThreads, workerTF);
            workerNioEventLoopGroup.setIoRatio(workerIoRatio);
            return workerNioEventLoopGroup;
        }
    }
}
