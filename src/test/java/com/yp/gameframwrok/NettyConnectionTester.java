package com.yp.gameframwrok;

import com.yp.gameframwrok.model.message.OuterMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yyp
 */
@Log4j2
@Component
public class NettyConnectionTester {

    private final List<Channel> connections = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger connectedCount = new AtomicInteger();
    private final AtomicInteger failedCount = new AtomicInteger();

    /**
     * 测试指定数量的连接
     */
    public ConnectionTestResult testConnections(String host, int port, int totalConnections,
                                                int batchSize, long intervalMs) throws InterruptedException {
        log.info("开始连接测试: {}:{}, 总数: {}, 批次: {}, 间隔: {}ms",
                host, port, totalConnections, batchSize, intervalMs);

        resetCounters();

        ExecutorService executor = Executors.newFixedThreadPool(batchSize);
        CountDownLatch latch = new CountDownLatch(totalConnections);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalConnections; i++) {
            final int connectionId = i;
            executor.submit(() -> {
                try {
                    connectSingleClient(host, port, connectionId, latch);
                    // 控制连接速率
                    if (intervalMs > 0) {
                        Thread.sleep(intervalMs);
                    }
                } catch (Exception e) {
                    log.error("连接 {} 创建失败: {}", connectionId, e.getMessage());
                    failedCount.incrementAndGet();
                    latch.countDown();
                }
            });

            // 批次控制
            if ((i + 1) % batchSize == 0) {
                log.info("已提交 {} 个连接请求", i + 1);
            }
        }

        try {
            // 等待所有连接完成
            boolean completed = latch.await(120, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("连接测试超时，已完成: {}/{}", connectedCount.get(), totalConnections);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        sendMessageToAll("hello");
        long endTime = System.currentTimeMillis();
//        Thread.sleep(60000);
        executor.shutdown();


        return new ConnectionTestResult(
                connectedCount.get(),
                failedCount.get(),
                totalConnections,
                endTime - startTime,
                connections.size()
        );
    }

    private void connectSingleClient(String host, int port, int connectionId, CountDownLatch latch) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加必要的心跳和编解码器
                            pipeline.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new ConnectionTestHandler(connectionId));
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();

            connections.add(channel);
            connectedCount.incrementAndGet();

            log.debug("连接 {} 建立成功", connectionId);

            // 发送认证或心跳消息
            String authMsg = String.format("{\"type\":\"auth\",\"clientId\":%d,\"timestamp\":%d}",
                    connectionId, System.currentTimeMillis());
            channel.writeAndFlush(authMsg);

        } catch (Exception e) {
            failedCount.incrementAndGet();
            log.error("连接 {} 失败: {}", connectionId, e.getMessage());
            group.shutdownGracefully();
        } finally {
            latch.countDown();
        }
    }

    /**
     * 向所有活跃连接发送消息
     */
    public int sendMessageToAll(String message) {
        int sentCount = 0;
        for (Channel channel : connections) {
            if (channel != null && channel.isActive()) {
                try {
                    channel.writeAndFlush(message);
                    sentCount++;
                } catch (Exception e) {
                    log.debug("向连接发送消息失败: {}", e.getMessage());
                }
            }
        }
        return sentCount;
    }
    /**
     * 获取当前活跃连接数
     */
    public int getCurrentConnectionCount() {
        return (int) connections.stream()
                .filter(channel -> channel != null && channel.isActive())
                .count();
    }
    /**
     * 发送心跳消息到所有连接
     */
    public int sendHeartbeatToAll() {
        String heartbeatMsg = "{\"type\":\"heartbeat\",\"timestamp\":" + System.currentTimeMillis() + "}";
        return sendMessageToAll(heartbeatMsg);
    }
    /**
     * 关闭所有连接
     */
    public void closeAllConnections() {
        log.info("开始关闭 {} 个连接", connections.size());

        int closedCount = 0;
        for (Channel channel : connections) {
            if (channel != null && channel.isActive()) {
                channel.close().awaitUninterruptibly();
                closedCount++;
            }
        }

        connections.clear();
        log.info("已关闭 {} 个连接", closedCount);
    }

    private void resetCounters() {
        connectedCount.set(0);
        failedCount.set(0);
        connections.clear();
    }

  /**
   * 连接测试处理器
   */
  private static class ConnectionTestHandler extends SimpleChannelInboundHandler<String> {
    private final int connectionId;

    public ConnectionTestHandler(int connectionId) {
      this.connectionId = connectionId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
      log.debug("连接 {} 收到消息: {}", connectionId, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
      log.debug("连接 {} 断开", connectionId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      log.error("连接 {} 异常: {}", connectionId, cause.getMessage());
      ctx.close();
    }
        }
        @Data
    public static  class ConnectionTestResult {
        private final int successCount;
        private final int failedCount;
        private final int totalCount;
        private final long durationMs;
        private final int currentConnections;
        public ConnectionTestResult(int successCount, int failedCount, int totalCount,
                                    long durationMs, int currentConnections) {
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.totalCount = totalCount;
            this.durationMs = durationMs;
            this.currentConnections = currentConnections;
        }
        // getters...
        public double getSuccessRate() {
            return totalCount == 0 ? 0 : (double) successCount / totalCount * 100;
        }

        public double getConnectionsPerSecond() {
            return durationMs == 0 ? 0 : (double) successCount / (durationMs / 1000.0);
        }
    }
}
