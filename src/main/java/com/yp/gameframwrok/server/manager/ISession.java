package com.yp.gameframwrok.server.manager;

import io.netty.channel.Channel;

/**
 * @author yyp
 */
public interface ISession {

    /**
     * 会话ID
     */
    int getSessionId();

    Channel getChannel();

    boolean isTimeOut();

    void close();

    void resetSession(Channel channel);

    Integer getUserId();

    boolean isVerified();

    void setVerified(int userId);

    void updateAliveTime(long aliveTime);
}
