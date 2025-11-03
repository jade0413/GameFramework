package com.yp.gameframwrok.server.netty;

import com.yp.gameframwrok.server.handler.HeartBeatServerHandler;
import com.yp.gameframwrok.server.handler.WebSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

@Log4j2
@Component
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    public static final String WEBSOCKET_PATH = "/websocket";
    private static final int MAX_CONTENT_LENGTH = 65536;


	@Autowired
	WebSocketFrameHandler webSocketFrameHandler;
	@Autowired
	HeartBeatServerHandler heartBeatServerHandler;
    @Autowired
    NettyProperties nettyProperties;

	@Override
	public void initChannel(SocketChannel ch) throws CertificateException, SSLException {
		final SslContext sslCtx;
        if (nettyProperties.isSslEnabled()) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
		} else {
			sslCtx = null;
		}
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		}
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
		pipeline.addLast(new WebSocketServerProtocolHandler(nettyProperties.getPath(), null, true));
		pipeline.addLast(new IdleStateHandler(
				nettyProperties.getReaderIdleSeconds(),
				nettyProperties.getWriterIdleSeconds(),
				nettyProperties.getAllIdleSeconds()
		));
		pipeline.addLast(heartBeatServerHandler);
		// WebSocket协议处理器 - 这会自动处理握手
		if (nettyProperties.isCompressionEnabled()) {
            pipeline.addLast(new WebSocketServerCompressionHandler(nettyProperties.getCompressionThreshold()));
        }
		pipeline.addLast(webSocketFrameHandler);
	}
}
