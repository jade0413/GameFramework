package com.yp.gameframwrok.server.manager;

import com.yp.gameframwrok.server.core.Session;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author yyp
 */
@Log4j2
@Component
public class SocketAcceptor {


    @Autowired
    private ISessionManager sessionManager;

    public void registerChannel(Channel channel) {
        ISession session = new Session(channel);
        sessionManager.addSession(session);
    }

    public void unregisterChannel(Channel channel) {
        // 不直接删除，进入脱离状态，给予重连窗口期 如果没有验证的会话，直接删除
        sessionManager.detachSession(channel);
    }
}
