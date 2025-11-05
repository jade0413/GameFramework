# WebSocket 连接问题分析与解决方案

## 🔍 问题描述

**现象**：
- 测试客户端在 `NettyConnectionTester.java:132` 发送 JSON 文本消息后，服务端 `WebSocketFrameHandler` 没有收到
- `channelActive` 能正常触发，说明 TCP 连接已建立
- 但消息无法到达 `channelRead0` 方法

## 🎯 根本原因分析

### 问题 1：客户端不是 WebSocket 协议（最关键）

**测试客户端的 Pipeline**：
```java
// NettyConnectionTester.java:112-118
pipeline.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
pipeline.addLast(new StringEncoder());     // ❌ 普通字符串编码器
pipeline.addLast(new StringDecoder());     // ❌ 普通字符串解码器
pipeline.addLast(new ConnectionTestHandler(connectionId));
```

**服务端的 Pipeline**：
```java
// WebSocketServerInitializer.java:50-63
pipeline.addLast(new HttpServerCodec());              // HTTP 编解码
pipeline.addLast(new HttpObjectAggregator(65536));    // HTTP 消息聚合
pipeline.addLast(new WebSocketServerProtocolHandler("/websocket")); // WebSocket 握手
pipeline.addLast(new IdleStateHandler(...));
pipeline.addLast(heartBeatServerHandler);
pipeline.addLast(new WebSocketServerCompressionHandler(...));
pipeline.addLast(webSocketFrameHandler);              // 处理 WebSocketFrame
```

**关键问题**：
1. ✅ 客户端发送的是**原始 TCP 字符串**（String）
2. ❌ 服务端期望的是 **WebSocket 协议帧**（WebSocketFrame）
3. ⚠️ 客户端**没有进行 WebSocket 握手**（HTTP Upgrade）

### 数据流对比

#### 当前错误流程（客户端 → 服务端）

```
客户端:
  String "{"type":"auth",...}"
    ↓ StringEncoder
  TCP 字节流: [7B 22 74 79 70 65 22 ...]

服务端 Pipeline:
  [TCP 字节流]
    ↓ HttpServerCodec (期望 HTTP 请求)
    ❌ 解析失败：不是有效的 HTTP 请求
    ↓ 消息被丢弃或触发异常
  [无法到达 WebSocketFrameHandler]
```

#### 正确的 WebSocket 流程

```
客户端:
  1. HTTP Upgrade 请求 (握手)
     GET /websocket HTTP/1.1
     Upgrade: websocket
     Connection: Upgrade
     Sec-WebSocket-Key: xxx
    ↓
  2. 服务端响应 101 Switching Protocols
    ↓
  3. 发送 WebSocket Frame
     [FIN=1][Opcode=1(Text)][Mask=1][Payload="..."]
    
服务端:
  [HTTP 握手请求]
    ↓ HttpServerCodec
  [FullHttpRequest]
    ↓ WebSocketServerProtocolHandler
  [握手成功，协议升级]
    ↓ 后续消息
  [WebSocketFrame]
    ↓ WebSocketFrameHandler.channelRead0()
  ✅ 消息成功到达
```

---

## 💡 解决方案

### 方案 1：修改客户端为标准 WebSocket 客户端（推荐）

#### 1.1 添加 WebSocket 依赖（已有 Netty）

```xml
<!-- pom.xml 已包含 netty-all -->
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.2.7.Final</version>
</dependency>
```

#### 1.2 完整的 WebSocket 客户端实现

```java
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

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Log4j2
public class WebSocketClientTester {

    private Channel channel;
    private EventLoopGroup group;

    /**
     * 连接到 WebSocket 服务器
     */
    public void connect(String host, int port, String path) throws Exception {
        URI uri = new URI(String.format("ws://%s:%d%s", host, port, path));
        
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        
        // WebSocket 握手器
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory
            .newHandshaker(uri, WebSocketVersion.V13, null, true, 
                          new DefaultHttpHeaders(), 65536);
        
        WebSocketClientHandler handler = new WebSocketClientHandler(handshaker);
        
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    
                    // HTTP 编解码器（用于握手）
                    pipeline.addLast(new HttpClientCodec());
                    pipeline.addLast(new HttpObjectAggregator(65536));
                    
                    // 心跳
                    pipeline.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
                    
                    // WebSocket 客户端处理器
                    pipeline.addLast(handler);
                }
            });
        
        channel = bootstrap.connect(host, port).sync().channel();
        
        // 等待握手完成
        handler.handshakeFuture().sync();
        
        log.info("WebSocket 连接成功: {}", uri);
    }
    
    /**
     * 发送文本消息
     */
    public void sendText(String text) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(text));
            log.info("发送文本消息: {}", text);
        }
    }
    
    /**
     * 发送二进制消息（Protobuf）
     */
    public void sendBinary(byte[] data) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new BinaryWebSocketFrame(
                io.netty.buffer.Unpooled.wrappedBuffer(data)
            ));
            log.info("发送二进制消息: {} bytes", data.length);
        }
    }
    
    /**
     * 关闭连接
     */
    public void close() {
        if (channel != null) {
            channel.writeAndFlush(new CloseWebSocketFrame());
            channel.closeFuture().awaitUninterruptibly();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }
    
    /**
     * WebSocket 客户端处理器
     */
    private static class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
        
        private final WebSocketClientHandshaker handshaker;
        private ChannelPromise handshakeFuture;
        
        public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
            this.handshaker = handshaker;
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
            log.info("开始 WebSocket 握手");
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            Channel ch = ctx.channel();
            
            if (!handshaker.isHandshakeComplete()) {
                // 处理握手响应
                try {
                    handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                    handshakeFuture.setSuccess();
                    log.info("WebSocket 握手完成");
                } catch (Exception e) {
                    handshakeFuture.setFailure(e);
                    log.error("WebSocket 握手失败", e);
                }
                return;
            }
            
            // 处理 WebSocket 消息
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
                log.info("收到文本消息: {}", textFrame.text());
            } else if (frame instanceof BinaryWebSocketFrame) {
                BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
                log.info("收到二进制消息: {} bytes", binaryFrame.content().readableBytes());
            } else if (frame instanceof PongWebSocketFrame) {
                log.debug("收到 Pong");
            } else if (frame instanceof CloseWebSocketFrame) {
                log.info("收到关闭帧");
                ch.close();
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("客户端异常", cause);
            if (!handshakeFuture.isDone()) {
                handshakeFuture.setFailure(cause);
            }
            ctx.close();
        }
    }
    
    // ========== 测试主方法 ==========
    
    public static void main(String[] args) throws Exception {
        WebSocketClientTester client = new WebSocketClientTester();
        
        try {
            // 连接
            client.connect("localhost", 8080, "/websocket");
            
            // 等待 1 秒
            Thread.sleep(1000);
            
            // 发送文本消息
            client.sendText("{\"type\":\"auth\",\"clientId\":1,\"timestamp\":" + 
                           System.currentTimeMillis() + "}");
            
            // 发送二进制消息（Protobuf）
            // OuterMessage.DataPackage message = OuterMessage.DataPackage.newBuilder()
            //     .setMainType(1)
            //     .setSubType(1)
            //     .setData(ByteString.copyFromUtf8("test"))
            //     .build();
            // client.sendBinary(message.toByteArray());
            
            // 保持连接 10 秒
            Thread.sleep(10000);
            
        } finally {
            client.close();
        }
    }
}
```

#### 1.3 修改原测试类

```java
// NettyConnectionTester.java 的修改
private void connectSingleClient(String host, int port, int connectionId, CountDownLatch latch) {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
        URI uri = new URI(String.format("ws://%s:%d/websocket", host, port));
        
        // WebSocket 握手器
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory
            .newHandshaker(uri, WebSocketVersion.V13, null, true, 
                          new DefaultHttpHeaders(), 65536);
        
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        // ✅ WebSocket 客户端 Pipeline
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        pipeline.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
                        pipeline.addLast(new WebSocketClientProtocolHandler(handshaker));
                        pipeline.addLast(new ConnectionTestHandler(connectionId));
                    }
                });

        ChannelFuture future = bootstrap.connect(host, port).sync();
        Channel channel = future.channel();

        connections.add(channel);
        connectedCount.incrementAndGet();

        log.info("连接 {} 建立成功", connectionId);

        // ✅ 等待握手完成后再发送消息
        Thread.sleep(500);
        
        // ✅ 发送 WebSocket 文本帧
        String authMsg = String.format("{\"type\":\"auth\",\"clientId\":%d,\"timestamp\":%d}",
                connectionId, System.currentTimeMillis());
        channel.writeAndFlush(new TextWebSocketFrame(authMsg));

    } catch (Exception e) {
        failedCount.incrementAndGet();
        log.error("连接 {} 失败: {}", connectionId, e.getMessage());
        group.shutdownGracefully();
    } finally {
        latch.countDown();
    }
}
```

---

### 方案 2：使用现成的 WebSocket 客户端库（最简单）

#### 2.1 使用 Java-WebSocket 库

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.java-websocket</groupId>
    <artifactId>Java-WebSocket</artifactId>
    <version>1.5.4</version>
    <scope>test</scope>
</dependency>
```

```java
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class SimpleWebSocketClient extends WebSocketClient {
    
    public SimpleWebSocketClient(URI serverUri) {
        super(serverUri);
    }
    
    @Override
    public void onOpen(ServerHandshake handshake) {
        log.info("连接成功");
        // 发送消息
        send("{\"type\":\"auth\",\"clientId\":1}");
    }
    
    @Override
    public void onMessage(String message) {
        log.info("收到消息: {}", message);
    }
    
    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("连接关闭: {} - {}", code, reason);
    }
    
    @Override
    public void onError(Exception ex) {
        log.error("连接错误", ex);
    }
    
    // 使用示例
    public static void main(String[] args) throws Exception {
        URI uri = new URI("ws://localhost:8080/websocket");
        SimpleWebSocketClient client = new SimpleWebSocketClient(uri);
        client.connect();
        
        Thread.sleep(10000);
        client.close();
    }
}
```

---

## 🔬 调试验证步骤

### 1. 使用浏览器测试（最简单）

打开浏览器控制台（F12），输入：

```javascript
const ws = new WebSocket('ws://localhost:8080/websocket');

ws.onopen = () => {
    console.log('✅ 连接成功');
    // 发送文本消息
    ws.send('{"type":"auth","clientId":1}');
};

ws.onmessage = (event) => {
    console.log('📨 收到消息:', event.data);
};

ws.onerror = (error) => {
    console.error('❌ 错误:', error);
};

ws.onclose = () => {
    console.log('🔌 连接关闭');
};
```

**预期结果**：
- 服务端 `channelActive` 日志：`创建链接-----: /127.0.0.1:xxxxx`
- 服务端 `channelRead0` 日志：`收到客户端消息: {...}`

### 2. 使用 Postman 测试

1. 新建 WebSocket Request
2. URL: `ws://localhost:8080/websocket`
3. 点击 Connect
4. 发送消息：`{"type":"auth","clientId":1}`

### 3. 查看服务端日志验证握手

在 `WebSocketServerInitializer` 添加日志 Handler：

```java
@Override
public void initChannel(SocketChannel ch) throws CertificateException, SSLException {
    ChannelPipeline pipeline = ch.pipeline();
    
    // 添加日志 Handler（调试用）
    pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
    
    pipeline.addLast(new HttpServerCodec());
    // ... 其他 Handler
}
```

**正常握手日志应该包含**：
```
[DEBUG] WRITE: HttpResponse(...)
        Upgrade: websocket
        Connection: Upgrade
        Sec-WebSocket-Accept: xxx
```

---

## 📊 问题总结

| 问题 | 原因 | 影响 |
|------|------|------|
| ❌ 使用 StringEncoder/Decoder | 发送原始 TCP 数据 | 服务端无法识别为 HTTP/WebSocket |
| ❌ 没有 HTTP 握手 | 客户端直接发送数据 | `WebSocketServerProtocolHandler` 拒绝连接 |
| ❌ 协议不匹配 | 客户端 String vs 服务端 WebSocketFrame | 消息被丢弃 |

## ✅ 解决方案对比

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| Netty WebSocket 客户端 | 完全控制、高性能 | 代码复杂 | ⭐⭐⭐⭐ |
| Java-WebSocket 库 | 简单易用 | 额外依赖 | ⭐⭐⭐⭐⭐ |
| 浏览器 JavaScript | 零依赖、快速验证 | 只能手动测试 | ⭐⭐⭐（调试用）|

---

## 🚀 快速修复

**最快的修复方式**：使用浏览器或 Postman 先验证服务端是否正常，然后再用正确的 WebSocket 客户端替换测试代码。

需要我帮您实现完整的 WebSocket 客户端测试类吗？

