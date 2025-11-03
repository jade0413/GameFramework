package com.yp.gameframwrok.server.netty;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "netty.websocket")
public class NettyProperties {

    /** WebSocket 路径，例如 /websocket */
    private String path = "/websocket";

    /** 是否启用自签名 SSL（仅开发测试使用） */
    private boolean sslEnabled = false;

    /** IdleStateHandler 配置（秒） */
    private int readerIdleSeconds = 30;
    private int writerIdleSeconds = 60;
    private int allIdleSeconds = 120;

    /** 启用压缩以及阈值（字节） */
    private boolean compressionEnabled = true;
    private int compressionThreshold = 2048;
}


