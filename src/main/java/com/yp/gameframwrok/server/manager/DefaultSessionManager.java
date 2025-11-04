package com.yp.gameframwrok.server.manager;

/**
 * @author yyp
 */
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.yp.gameframwrok.server.core.Session;

@Log4j2
@Scope("singleton")
@Component
public class DefaultSessionManager implements ISessionManager {

    @Autowired
    ReconnectTokenManager reconnectTokenManager;

    private  final List<ISession> localSessions;
    private final Map<Integer, ISession> localSessionsById;
    private final Map<Channel, ISession> localSessionsByConnection;
    private final ScheduledExecutorService scheduler;
    private final Map<Integer, Long> detachedExpireAt;

    private final AtomicLong reconnectWindowMillis = new AtomicLong(30_000);

    public DefaultSessionManager() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.localSessionsById = new ConcurrentHashMap<>();
        this.localSessionsByConnection = new ConcurrentHashMap<>();
        this.localSessions = new ArrayList<>();
        this.detachedExpireAt = new ConcurrentHashMap<>();
    }

    public void init() {
        scheduler.scheduleAtFixedRate(() -> {
            if (!localSessions.isEmpty()) {
                localSessions.forEach(session -> {
                    if (session.isTimeOut()) {
                        log.info("会话心跳超时, 会话ID: {}", session.getSessionId());
                        removeSession(session);
                    }
                });
            }
            if (!detachedExpireAt.isEmpty()) {
                reconnectTokenManager.clearExpiredTokens();
                long now = System.currentTimeMillis();
                detachedExpireAt.entrySet().removeIf(e -> {
                    int sid = e.getKey();
                    long expireAt = e.getValue();
                    if (expireAt <= now) {
                        this.removeSession(sid);
                        log.info("重连窗口过期，清理会话: {}", sid);
                        return true;
                    }
                    return false;
                });
            }
        }, 0, 10000, TimeUnit.MILLISECONDS);
    }



    @Override
    public void addSession(ISession session) {
        synchronized (this.localSessions) {
            this.localSessions.add(session);
        }
        this.localSessionsById.put(session.getSessionId(), session);
        this.localSessionsByConnection.put(session.getChannel(), session);
        log.info("连接数量: {}, 用户会话数量: {}", localSessions.size(), localSessionsById.size());
    }

    public void clearSession(Channel channel){
        ISession session = localSessionsByConnection.remove(channel);
        if (session != null) {
            removeSession(session,false);
        }
    }

    public void removeSession(ISession session,boolean isClose){
        if (session == null) {
            return;
        }
        synchronized (this.localSessions) {
            this.localSessions.remove(session);
        }
        this.localSessionsById.remove(session.getSessionId());
        this.localSessionsByConnection.remove(session.getChannel());
        if(isClose){
            session.close();
        }
        log.info("移除会话, 会话ID: {}, 连接数量: {}, 用户会话数量: {}", session.getSessionId(), localSessions.size(), localSessionsById.size());
    }

    @Override
    public void removeSession(ISession session) {
        this.removeSession(session,true);
    }

    @Override
    public ISession getSessionById(int sessionId) {
        return null;
    }

    @Override
    public ISession getSessionByChannel(Channel channel) {
        return localSessionsByConnection.get(channel);
    }

    public void removeSession(int sessionId) {
        ISession session = localSessionsById.get(sessionId);
        if (session != null) {
            removeSession(session);
        }
    }

    @Override
    public void removeSession(Channel channel) {
        ISession session = localSessionsByConnection.get(channel);
        if (session != null) {
            removeSession(session);
        }
    }

    @Override
    public void detachSession(Channel channel) {
        ISession s = localSessionsByConnection.remove(channel);
        if (s instanceof Session) {
            Session ss = (Session) s;
            boolean verified = ss.isVerified();
            // 未验证的连接直接移除
            if(!verified) {
                removeSession(ss);
                return;
            }
            ss.markDetached();
            detachedExpireAt.put(ss.getSessionId(), System.currentTimeMillis() + reconnectWindowMillis.get());
            log.info("会话脱离，等待重连: {}", ss.getSessionId());
        }
    }

    @Override
    public ISession resumeSession(int previousSessionId, Channel newChannel) {
        ISession s = localSessionsById.get(previousSessionId);
        if (!(s instanceof Session)) {
            return null;
        }
        Session ss = (Session) s;
        if (!ss.isDetached()) {
            return null;
        }
        // 关闭重连窗口
        closeDetachedSession(previousSessionId);
        // 清空新会话的session,因为将新的channel绑定到了老的会话
        this.clearSession(newChannel);
        // 重新设置会话ID
        ss.resetSession(newChannel);
        ss.clearDetached();
        // 再移除旧会话
        this.localSessionsByConnection.put(newChannel, ss);
        log.info("会话重连成功: {} -> {}", previousSessionId, newChannel.remoteAddress());
        log.info("新的会话ID: {}", ss.getSessionId());
        return ss;
    }

    public void closeDetachedSession(int sessionId) {
        this.detachedExpireAt.remove(sessionId);
    }

}
