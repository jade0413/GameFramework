package com.yp.gameframwrok.server.handler;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.yp.gameframwrok.engine.message.MessageEvent;
import com.yp.gameframwrok.engine.message.MessageEventTask;
import com.yp.gameframwrok.model.message.OuterMessage;
import com.yp.gameframwrok.server.manager.ISession;
import com.yp.gameframwrok.server.manager.SocketAcceptor;
import com.yp.gameframwrok.server.manager.ReconnectTokenManager;
import com.yp.gameframwrok.server.manager.ISessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.SocketException;
import java.util.Locale;

/**
 * Echoes uppercase content of text frames.
 */
@Log4j2
@Component
@ChannelHandler.Sharable
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Autowired
    SocketAcceptor socketAcceptor;
    @Autowired
    ReconnectTokenManager reconnectTokenManager;
    @Autowired
    ISessionManager sessionManager;

    @Autowired
    MessageEventTask messageEventTask;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("创建链接-----: " + ctx.channel().remoteAddress());
        socketAcceptor.registerChannel(ctx.channel());
        // 下发一次性重连令牌（窗口期内有效）
//        int sid = ctx.channel().id().hashCode();
//        String token = reconnectTokenManager.issue(sid);
//        ctx.writeAndFlush(new TextWebSocketFrame("TOKEN:" + token));
//        log.info("RESUME:{}", token);
//        log.info("会话ID:{}", sid);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("链接关闭: " + ctx.channel().remoteAddress());
        socketAcceptor.unregisterChannel(ctx.channel());
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        if (msg instanceof FullHttpRequest) {
//            FullHttpRequest request = (FullHttpRequest) msg;
//
//            try {
//                // 处理WebSocket握手请求
//                if (isWebSocketHandshake(request)) {
//                    handleHandshake(ctx, request);
//                    log.info("WebSocket握手成功: {}", ctx.channel().remoteAddress());
//                    return; // 握手成功，不再继续传递
//                }
//            } finally {
//                // 释放请求对象
//                request.release();
//            }
//        }
//        super.channelRead(ctx, msg);
//    }
//    private boolean isWebSocketHandshake(FullHttpRequest request) {
//        HttpHeaders headers = request.headers();
//        return request.method() == HttpMethod.GET &&
//                "WebSocket".equalsIgnoreCase(headers.get("Upgrade")) &&
//                headers.contains("Sec-WebSocket-Key");
//    }
//    private void handleHandshake(ChannelHandlerContext ctx, FullHttpRequest request) {
//        // 创建握手响应
//        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
//                HttpVersion.HTTP_1_1,
//                HttpResponseStatus.SWITCHING_PROTOCOLS
//        );
//
//        HttpHeaders headers = response.headers();
//        headers.set("Upgrade", "websocket");
//        headers.set("Connection", "Upgrade");
//
//        // 计算Sec-WebSocket-Accept
//        String webSocketKey = request.headers().get("Sec-WebSocket-Key");
//        String acceptSeed = webSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
//        try {
//            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
//            byte[] hash = sha1.digest(acceptSeed.getBytes(StandardCharsets.UTF_8));
//            String accept = Base64.getEncoder().encodeToString(hash);
//            headers.set("Sec-WebSocket-Accept", accept);
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("SHA-1 not available", e);
//        }
//
//        // 添加其他必要的头部
//        headers.set("Server", "Netty-WebSocket-Server");
//
//        // 发送握手响应
//        ctx.writeAndFlush(response).addListener(future -> {
//            if (future.isSuccess()) {
//                log.info("WebSocket协议升级完成，连接已建立");
//                // 握手完成后可以发送欢迎消息
//                sendWelcomeMessage(ctx);
//            }
//        });
//    }
//
//    private void sendWelcomeMessage(ChannelHandlerContext ctx) {
//        String welcomeMsg = "{\"type\":\"handshake\",\"status\":200,\"message\":\"WebSocket连接成功\"}";
//
//        // 构建WebSocket文本帧
//        TextWebSocketFrame frame = new TextWebSocketFrame(welcomeMsg);
//        ctx.writeAndFlush(frame);
//
//        log.info("欢迎消息已发送给客户端");
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws InvalidProtocolBufferException {
        Channel channel = ctx.channel();
        log.info("收到消息:{}", frame);
        if (frame instanceof TextWebSocketFrame textWebSocketFrame) {
            String request = textWebSocketFrame.text();
            log.info("收到客户端消息:{}",channel.id().hashCode() + "->" + request);
            MessageEvent messageEvent = new MessageEvent();
            messageEvent.setPlayerId(1);
            messageEvent.setData(ByteString.copyFromUtf8(request));
            messageEvent.setMainType(1);
            messageEvent.setSubType(1);
            // 消息直接交给Disruptor处理
            messageEventTask.messageEventProducer().onData(messageEvent);
            if (request.startsWith("RESUME:")) {
                String token = request.substring("RESUME:".length());
                Integer oldSid = reconnectTokenManager.consume(token);
                if (oldSid != null && sessionManager.resumeSession(oldSid, channel) != null) {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame("RESUME_OK"));
                } else {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame("RESUME_FAIL"));
                }
                return;
            }
            TextWebSocketFrame socketFrame = new TextWebSocketFrame(request.toUpperCase(Locale.US));
            String text = socketFrame.text();
            long l = System.currentTimeMillis();
            ctx.channel().writeAndFlush(new TextWebSocketFrame(String.valueOf(l)));
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof PongWebSocketFrame) {
            // 忽略客户端Pong
        } else if (frame instanceof BinaryWebSocketFrame binaryWebSocketFrame) {
            ISession session = sessionManager.getSessionByChannel(channel);
            if(session == null || !session.isVerified()){
                log.error("session not found or not verified, channel: {}", channel);
                return;
            }
            byte[] content = binaryWebSocketFrame.content().array();
            OuterMessage.DataPackage outerMessage = OuterMessage.DataPackage.parseFrom(content);
            int mainType = outerMessage.getMainType();
            int subType = outerMessage.getSubType();
            MessageEvent messageEvent = new MessageEvent();
            messageEvent.setPlayerId(session.getUserId());
            messageEvent.setData(outerMessage.getData());
            messageEvent.setMainType(mainType);
            messageEvent.setSubType(subType);
            // 消息直接交给Disruptor处理
            messageEventTask.messageEventProducer().onData(messageEvent);
        } else if (frame instanceof CloseWebSocketFrame) {
            log.error("closed remoteAddress: " + ctx.channel().remoteAddress());
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        boolean writable = channel.isWritable();
        log.info("通道可写性变更: {}, writable={}", channel.remoteAddress(), writable);
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (isConnectionReset(cause)) {
            // Connection reset 是正常现象，记录为 INFO 级别
            log.info("客户端连接重置: {} ",
                    ctx.channel().remoteAddress());
        } else {
            // 其他异常记录为 ERROR 级别
            log.error("WebSocket 异常 : {}",
                     cause.getMessage());
        }
        // 优雅关闭连接
        closeConnectionGracefully(ctx);
    }

    /**
     * 判断是否为连接重置异常
     */
    private boolean isConnectionReset(Throwable cause) {
        return cause instanceof SocketException &&  ("Connection reset".equals(cause.getMessage()) || "Connection reset by peer".equals(cause.getMessage()));
    }

    /**
     * 优雅关闭连接
     */
    private void closeConnectionGracefully(ChannelHandlerContext ctx) {
        try {
            if (ctx.channel().isActive()) {
                // 发送关闭帧
                ctx.writeAndFlush(new CloseWebSocketFrame())
                        .addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.close();
            }
        } catch (Exception e) {
            log.debug("关闭连接时发生异常: {}", e.getMessage());
            ctx.close();
        }
    }

    
}
