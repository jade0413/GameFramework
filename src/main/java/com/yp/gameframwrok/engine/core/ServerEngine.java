package com.yp.gameframwrok.engine.core;

import com.yp.gameframwrok.server.manager.ISession;
import com.yp.gameframwrok.server.manager.ISessionManager;
import com.yp.gameframwrok.server.netty.WebSocketServer;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yyp
 */
@Component
@Log4j2
public class ServerEngine {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private ISessionManager sessionManager;

    /**
     * 启动服务器
     */
    @PostConstruct
    public void start() {
        log.info("ServerEngine start");
        // 初始化启动session管理器
        sessionManager.init();
        try {
            // 开启服务器监听
            webSocketServer.start();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("ServerEngine start error", e);
        }
    }
}
