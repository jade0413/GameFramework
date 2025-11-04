package com.yp.gameframwrok.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.SmartLifecycle;
import org.springframework.lang.NonNull;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
@Component
public class WebSocketServer implements SmartLifecycle {

	@Value("${netty.websocket.port}")
	Integer bindPort;

    @Autowired
    WebSocketServerInitializer webSocketServerInitializer;

    @Autowired
    NettyProperties nettyProperties;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private volatile Channel serverChannel;
    private final AtomicBoolean running = new AtomicBoolean(false);

	public WebSocketServer() {
		int cpuNum = Runtime.getRuntime().availableProcessors();
		 bossGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(cpuNum) : new NioEventLoopGroup(cpuNum);
		 workerGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(cpuNum * 2) : new NioEventLoopGroup(cpuNum * 2);
	}

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
        serverBootstrap.childHandler(webSocketServerInitializer);

        serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
        if (Epoll.isAvailable()) {
            serverBootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
        }
        serverBootstrap.option(ChannelOption.SO_RCVBUF, 1024 * 64);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024 * 20);
        serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.childOption(ChannelOption.SO_LINGER, 5);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.childOption(ChannelOption.SO_SNDBUF, 1024 * 64);
        serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        serverBootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(64 * 1024, 128 * 1024));

        serverBootstrap.bind(bindPort).addListener(f -> {
            if (f.isSuccess()) {
                serverChannel = ((ChannelFuture) f).channel();
                log.info("Gate bind addr(by .properties): ws://0.0.0.0:" + bindPort + nettyProperties.getPath());
            } else {
                running.set(false);
                log.error("WebSocketServer bind failed", f.cause());
            }
        });
    }

    @Override
    public void stop() {
        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
                serverChannel = null;
            }
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            running.set(false);
        }
    }

    @Override
    public void stop(@NonNull Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return 0;
    }


	@PreDestroy
    public void destroy() {
        log.info("server shutdown.");
        stop();
    }
}
