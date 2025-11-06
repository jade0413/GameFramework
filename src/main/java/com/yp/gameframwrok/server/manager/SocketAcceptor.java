package com.yp.gameframwrok.server.manager;

import com.yp.gameframwrok.engine.core.UserCacheManger;
import com.yp.gameframwrok.model.cache.UserCache;
import com.yp.gameframwrok.server.core.Session;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
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

    @Autowired
    private UserCacheManger userCacheManger;


    @Autowired
    ReconnectTokenManager reconnectTokenManager;

    public void registerChannel(Channel channel) {
        ISession session = new Session(channel);
        sessionManager.addSession(session);
    }

    public void unregisterChannel(Channel channel) {
        // 不直接删除，进入脱离状态，给予重连窗口期 如果没有验证的会话，直接删除
//        sessionManager.detachSession(channel);
        sessionManager.removeSession(channel);
    }

    public void checkVerify(String token, Channel channel){
        String[] split = token.split("-");
        int userId = Integer.parseInt(split[0]);
        UserCache userCache = userCacheManger.getUser(userId);
        if (userCache == null) {
            throw new RuntimeException("用户不存在");
        }
        String compareToken = userCache.getToken();
        if(compareToken != null && compareToken.equals(token)){
            ISession sessionById = sessionManager.getSessionById(userId);
            if(sessionById != null){
                log.info("用户已登录, 会话ID: {}", sessionById.getSessionId());
                sessionManager.removeSession(channel);
                return;
            }
            ISession session = sessionManager.getSessionByChannel(channel);
            if(session == null){
                throw new RuntimeException("会话不存在");
            }
            sessionManager.mapUserIdSession(userId,channel);
            // 下发一次性重连令牌（窗口期内有效）
            String resumeToken = reconnectTokenManager.issue(userId);
            channel.writeAndFlush(new TextWebSocketFrame("TOKEN:" + resumeToken));
            log.info("RESUME:{}", resumeToken);
            log.info("会话ID:{}", userId);
        }
    }

    public void reconnection(String token, Channel channel) {
        Integer oldUserId = reconnectTokenManager.consume(token);
        if(oldUserId == null){
            throw new RuntimeException("令牌不存在");
        }
        ISession session = sessionManager.getSessionByChannel(channel);
        if(session == null){
            throw new RuntimeException("会话不存在");
        }
        if (sessionManager.resumeSession(oldUserId, channel) != null) {
            channel.writeAndFlush(new TextWebSocketFrame("RESUME_OK"));
        } else {
            channel.writeAndFlush(new TextWebSocketFrame("RESUME_FAIL"));
        }
    }
}
