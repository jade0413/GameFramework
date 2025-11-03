package com.yp.gameframwrok.server.core;

import com.yp.gameframwrok.server.manager.ISession;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public class Session implements ISession {


    private volatile Channel connection;

    private int sessionId;

    private String nodeId;

    private long aliveTime;

    private Integer playerId;

    private volatile long creationTime;

    private volatile long lastReadTime;

    private volatile long lastWriteTime;

    private volatile int expireTime;

    private volatile boolean detached;

    private volatile long detachTime;

    public Session(Channel channel) {
        this.sessionId = channel.id().hashCode();
        this.connection = channel;
        this.aliveTime = System.currentTimeMillis();
        this.expireTime = 0;
    }

    @Override
    public Channel getChannel() {
        return connection;
    }

    /**
     * 如果设置超市时间不大于0, 则不会超时
     * @return
     */
    public boolean isTimeOut() {
        long currentTime = System.currentTimeMillis();
        if (expireTime > 0 && aliveTime + expireTime < currentTime) {
            log.info("TimeOutConnection: " + connection.remoteAddress() + ", aliveTime: " +
                    this.aliveTime + ", now: " + System.currentTimeMillis() +
                    ", timeout: " + (currentTime - this.aliveTime));
            return true;
        }
        return false;
    }


    public boolean isVerified() {
        return this.playerId != null && this.playerId > 0;
    }


    public void close() {
        if (this.connection != null) {
            this.connection.close();
        }
    }

    @Override
    public void resetSession(Channel channel) {
        this.connection = channel;
        this.sessionId = channel.id().hashCode();
    }

    public boolean isDetached() {
        return detached;
    }

    public void markDetached() {
        this.detached = true;
        this.detachTime = System.currentTimeMillis();
    }

    public void clearDetached() {
        this.detached = false;
        this.detachTime = 0L;
    }

    public long getDetachTime() {
        return detachTime;
    }
}
