package com.yp.gameframwrok.server.manager;

import io.netty.channel.Channel;

import java.io.IOException;

/**
 * @author yyp
 */
public interface ISessionManager {

	void init();

	/**
	 * 新增会话
	 */
	void addSession(ISession session);


	void removeSession(ISession session);

	void updateSessionAlive(Channel channel);

	/**
	 * 根据会话ID移除会话
	 */
	void removeSession(int userId) throws IOException;

	void removeSession(Channel channel);


	ISession getSessionById(int sessionId);

	ISession getSessionByChannel(Channel channel);

	/**
	 * 将会话标记为“脱离”状态，保留重连窗口期。
	 */
	void detachSession(Channel channel);

	/**
	 * 通过旧会话ID恢复到新通道。
	 */
	ISession resumeSession(int previousUserId, Channel newChannel);

	/**
	 * 映射用户ID到会话
	 */
	void mapUserIdSession(int userId, Channel channel);
}
