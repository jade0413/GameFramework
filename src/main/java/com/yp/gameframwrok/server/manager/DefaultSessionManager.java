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
import java.util.Iterator;
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
            try {
                log.info("会话心跳检查, 总会话数量: {}, 用户会话数量: {}, 连接会话数量: {}", localSessions.size(), localSessionsById.size(), localSessionsByConnection.size());
                List<ISession> sessionsCopy;
                synchronized (this.localSessions) {
                    // 创建localSessions的一个快照，避免遍历时并发修改异常
                    sessionsCopy = new ArrayList<>(localSessions);
                }
                if (!sessionsCopy.isEmpty()) {
                    for (ISession session : sessionsCopy) {
                        log.info("会话心跳检查, 会话ID: {}", session);
                        if (session.isTimeOut()) {
                            log.info("会话心跳超时, 会话ID: {}", session.getSessionId());
                            this.removeSession(session);
                        }
                    }
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
            } catch (Exception e) {
                log.error("会话心跳检查异常", e);
            }
        }, 0, 10000, TimeUnit.MILLISECONDS);
    }

    public void updateSessionAlive(Channel channel){
        ISession session = getSessionByChannel(channel);
        if (session != null) {
            session.updateAliveTime(System.currentTimeMillis());
        }
    }

    @Override
    public void addSession(ISession session) {
        synchronized (this.localSessions) {
            this.localSessions.add(session);
        }
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
        if(session.getUserId() != null){
            localSessionsById.remove(session.getUserId());
        }
        this.localSessionsByConnection.remove(session.getChannel());
        if(isClose){
            session.close();
        }
        log.info("移除会话, 会话ID: {}, 连接数量: {}, 用户会话数量: {}, 连接会话数量: {}", session.getSessionId(), localSessions.size(), localSessionsById.size(),localSessionsByConnection.size());
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

    public void removeSession(int userId) {
        ISession session = localSessionsById.get(userId);
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
            detachedExpireAt.put(ss.getUserId(), System.currentTimeMillis() + reconnectWindowMillis.get());
            log.info("会话脱离，等待重连: {}", ss.getUserId());
        }
    }

    @Override
    public ISession resumeSession(int previousUserId, Channel newChannel) {
        ISession s = localSessionsById.get(previousUserId);
        if (!(s instanceof Session ss)) {
            return null;
        }
        if (!ss.isDetached()) {
            return null;
        }
        // 关闭重连窗口
        closeDetachedSession(previousUserId);
        // 清空新会话的session,因为将新的channel绑定到了老的会话
        this.clearSession(newChannel);
        // 重新设置会话ID
        ss.resetSession(newChannel);
        ss.clearDetached();
        // 再移除旧会话
        this.localSessionsByConnection.put(newChannel, ss);
        log.info("会话重连成功: {} -> {}", previousUserId, newChannel.remoteAddress());
        log.info("新的会话ID: {}", ss.getSessionId());
        return ss;
    }

    public void closeDetachedSession(int sessionId) {
        this.detachedExpireAt.remove(sessionId);
    }

    public synchronized void mapUserIdSession(int userId, Channel channel){
        ISession session = getSessionByChannel(channel);
        if (session != null) {
            // 如果已经校验过 并且是同一个channel，直接返回
            if(session.isVerified() && session.getUserId() == userId){
                return;
            }
            session.setVerified(userId);
            ISession iSession = localSessionsById.get(userId);
            if(iSession != null){
                log.info("用户 {} 已登录，重新绑定会话, 会话ID: {}", userId, iSession.getSessionId());
                removeSession(iSession);
            }
            localSessionsById.put(userId, session);
            log.info("用户 {} 登录成功, 用户会话数量: {}", userId, localSessionsById.size());
        }
    }
}
