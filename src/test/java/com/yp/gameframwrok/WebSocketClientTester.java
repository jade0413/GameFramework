package com.yp.gameframwrok;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 正确的 WebSocket 客户端测试类
 * 
 * 与 NettyConnectionTester 的区别：
 * 1. 使用 WebSocket 协议而不是原始 TCP
 * 2. 正确处理 WebSocket 握手
 * 3. 发送 WebSocketFrame 而不是 String
 * 
 * @author yyp
 */
@Log4j2
@Component
public class WebSocketClientTester {

    private final List<Channel> connections = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger connectedCount = new AtomicInteger();
    private final AtomicInteger failedCount = new AtomicInteger();
    private final AtomicInteger receivedCount = new AtomicInteger();

    /**
     * 测试指定数量的 WebSocket 连接
     */
    public TestResult testConnections(String host, int port, String path, 
                                     int totalConnections, int batchSize, 
                                     long intervalMs) throws InterruptedException {
        log.info("开始 WebSocket 连接测试: ws://{}:{}{}, 总数: {}, 批次: {}, 间隔: {}ms",
                host, port, path, totalConnections, batchSize, intervalMs);

        resetCounters();
        long startTime = System.currentTimeMillis();

        int batches = (int) Math.ceil((double) totalConnections / batchSize);

        for (int batch = 0; batch < batches; batch++) {
            int currentBatchSize = Math.min(batchSize, totalConnections - batch * batchSize);
            CountDownLatch latch = new CountDownLatch(currentBatchSize);

            log.info("批次 {}/{}: 建立 {} 个连接", batch + 1, batches, currentBatchSize);

            for (int i = 0; i < currentBatchSize; i++) {
                int connectionId = batch * batchSize + i + 1;
                new Thread(() -> connectSingleClient(host, port, path, connectionId, latch))
                        .start();
            }

            latch.await();

            if (batch < batches - 1 && intervalMs > 0) {
                Thread.sleep(intervalMs);
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        TestResult result = new TestResult();
        result.totalAttempted = totalConnections;
        result.successCount = connectedCount.get();
        result.failedCount = failedCount.get();
        result.currentActiveCount = getCurrentConnectionCount();
        result.durationMs = duration;
        result.receivedCount = receivedCount.get();

        log.info("测试完成 - 成功: {}, 失败: {}, 当前活跃: {}, 耗时: {}ms, 收到消息: {}",
                result.successCount, result.failedCount, result.currentActiveCount,
                result.durationMs, result.receivedCount);

        return result;
    }

    /**
     * 建立单个 WebSocket 连接
     */
    private void connectSingleClient(String host, int port, String path, 
                                     int connectionId, CountDownLatch latch) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            URI uri = new URI(String.format("ws://%s:%d%s", host, port, path));

            // WebSocket 握手器
            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory
                    .newHandshaker(uri, WebSocketVersion.V13, null, true,
                            new DefaultHttpHeaders(), 65536);

            WebSocketClientHandler handler = new WebSocketClientHandler(handshaker, connectionId, receivedCount);

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // HTTP 编解码器（用于握手）
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));

                            // 心跳检测
                            pipeline.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));

                            // WebSocket 客户端处理器
                            pipeline.addLast(handler);
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();

            // 等待握手完成
            handler.handshakeFuture().sync();

            connections.add(channel);
            connectedCount.incrementAndGet();

            log.info("连接 {} WebSocket 握手成功", connectionId);

            // 握手成功后发送测试消息
            String authMsg = "1-test_token";
            channel.writeAndFlush(new TextWebSocketFrame(authMsg));
            log.info("连接 {} 发送认证消息: {}", connectionId, authMsg);
//            Thread.sleep(100000);
        } catch (Exception e) {
            failedCount.incrementAndGet();
            log.error("连接 {} 失败: {}", connectionId, e.getMessage());
            group.shutdownGracefully();
        } finally {
            latch.countDown();
        }
    }

    /**
     * 向所有活跃连接发送文本消息
     */
    public int sendTextToAll(String message) {
        int sentCount = 0;
        for (Channel channel : connections) {
            if (channel != null && channel.isActive()) {
                try {
                    channel.writeAndFlush(new TextWebSocketFrame(message));
                    sentCount++;
                } catch (Exception e) {
                    log.debug("向连接发送消息失败: {}", e.getMessage());
                }
            }
        }
        log.info("向 {} 个连接发送文本消息: {}", sentCount, message);
        return sentCount;
    }

    /**
     * 向所有活跃连接发送二进制消息（Protobuf）
     */
    public int sendBinaryToAll(byte[] data) {
        int sentCount = 0;
        for (Channel channel : connections) {
            if (channel != null && channel.isActive()) {
                try {
                    channel.writeAndFlush(new BinaryWebSocketFrame(
                            io.netty.buffer.Unpooled.wrappedBuffer(data)
                    ));
                    sentCount++;
                } catch (Exception e) {
                    log.debug("向连接发送消息失败: {}", e.getMessage());
                }
            }
        }
        log.info("向 {} 个连接发送二进制消息: {} bytes", sentCount, data.length);
        return sentCount;
    }

    /**
     * 发送心跳到所有连接
     */
    public int sendHeartbeatToAll() {
        String heartbeatMsg = "{\"type\":\"heartbeat\",\"timestamp\":" + 
                             System.currentTimeMillis() + "}";
        return sendTextToAll(heartbeatMsg);
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
     * 关闭所有连接
     */
    public void closeAllConnections() {
        log.info("开始关闭 {} 个连接", connections.size());
        for (Channel channel : connections) {
            if (channel != null && channel.isActive()) {
                try {
                    channel.writeAndFlush(new CloseWebSocketFrame()).sync();
                    channel.close().sync();
                } catch (Exception e) {
                    log.debug("关闭连接失败: {}", e.getMessage());
                }
            }
        }
        connections.clear();
        log.info("所有连接已关闭");
    }

    private void resetCounters() {
        connectedCount.set(0);
        failedCount.set(0);
        receivedCount.set(0);
        connections.clear();
    }

    /**
     * WebSocket 客户端处理器
     */
    private static class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

        private final WebSocketClientHandshaker handshaker;
        private final int connectionId;
        private final AtomicInteger receivedCount;
        private ChannelPromise handshakeFuture;

        public WebSocketClientHandler(WebSocketClientHandshaker handshaker, 
                                      int connectionId, 
                                      AtomicInteger receivedCount) {
            this.handshaker = handshaker;
            this.connectionId = connectionId;
            this.receivedCount = receivedCount;
        }

        public ChannelFuture handshakeFuture() {
            return handshakeFuture;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            handshakeFuture = ctx.newPromise();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // 发起 WebSocket 握手
            handshaker.handshake(ctx.channel());
            log.debug("连接 {} 开始 WebSocket 握手", connectionId);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            Channel ch = ctx.channel();

            if (!handshaker.isHandshakeComplete()) {
                // 处理握手响应
                try {
                    handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                    handshakeFuture.setSuccess();
                    log.debug("连接 {} WebSocket 握手完成", connectionId);
                } catch (Exception e) {
                    handshakeFuture.setFailure(e);
                    log.error("连接 {} WebSocket 握手失败", connectionId, e);
                }
                return;
            }

            // 处理握手后的 WebSocket 消息
            if (msg instanceof FullHttpResponse) {
                FullHttpResponse response = (FullHttpResponse) msg;
                throw new IllegalStateException(
                        "Unexpected FullHttpResponse: " + response.status() + ", " +
                                response.content().toString(io.netty.util.CharsetUtil.UTF_8)
                );
            }

            WebSocketFrame frame = (WebSocketFrame) msg;

            if (frame instanceof TextWebSocketFrame) {
                TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                receivedCount.incrementAndGet();
                log.info("连接 {} 收到文本消息: {}", connectionId, textFrame.text());
            } else if (frame instanceof BinaryWebSocketFrame) {
                BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
                receivedCount.incrementAndGet();
                log.info("连接 {} 收到二进制消息: {} bytes", 
                        connectionId, binaryFrame.content().readableBytes());
            } else if (frame instanceof PongWebSocketFrame) {
                log.debug("连接 {} 收到 Pong", connectionId);
            } else if (frame instanceof CloseWebSocketFrame) {
                log.info("连接 {} 收到关闭帧", connectionId);
                ch.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("连接 {} 异常", connectionId, cause);
            if (!handshakeFuture.isDone()) {
                handshakeFuture.setFailure(cause);
            }
            ctx.close();
        }
    }

    /**
     * 测试结果
     */
    public static class TestResult {
        public int totalAttempted;
        public int successCount;
        public int failedCount;
        public int currentActiveCount;
        public int receivedCount;
        public long durationMs;

        @Override
        public String toString() {
            return String.format(
                    "TestResult{尝试=%d, 成功=%d, 失败=%d, 活跃=%d, 收到消息=%d, 耗时=%dms}",
                    totalAttempted, successCount, failedCount, currentActiveCount,
                    receivedCount, durationMs
            );
        }
    }

    /**
     * 测试主方法
     */
    public static void main(String[] args) {
        WebSocketClientTester tester = new WebSocketClientTester();

        try {
            // 测试 10 个连接
            TestResult result = tester.testConnections(
                    "localhost", 8080, "/websocket",
                    10, 5, 100
            );

            log.info("测试结果: {}", result);

            // 等待 2 秒
            Thread.sleep(2000);

            // 向所有连接发送心跳
            tester.sendHeartbeatToAll();

            // 再等待 3 秒
            Thread.sleep(3000);

            // 关闭所有连接
            tester.closeAllConnections();

        } catch (Exception e) {
            log.error("测试失败", e);
        }
    }
}

