package com.yp.gameframwrok.engine.message;

import java.util.concurrent.locks.ReentrantLock;

public class MessageUtil {
	private static final ReentrantLock lock = new ReentrantLock();
	private static MessageEventTaskManager manager = null;

//	public static void remoteListen(Ignite ignite, String address, MessageManager messageManager) {
//		IgniteMessaging rmtMsg = ignite.message(ignite.cluster().forRemotes());
//		rmtMsg.remoteListen(address, new IgniteBiPredicate<UUID, byte[]>() {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public boolean apply(UUID node, byte[] data) {
//				handle(address, data, messageManager);
//				return true;
//			}
//		});
//	}

//	public static void localListen(Ignite ignite, String topic, MessageManager messageManager) {
//		ignite.message().localListen(topic, new IgniteBiPredicate<UUID, byte[]>() {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public boolean apply(UUID node, byte[] data) {
//				handle(topic, data, messageManager);
//				return true;
//			}
//		});
//	}

	private static void handle(String address, byte[] data, MessageManager messageManager) {
		if (lock.tryLock()) {
			try {
				if (manager == null) {
					manager = new MessageEventTaskManager(messageManager);
				}
			} finally {
				lock.unlock();
			}
		}

//		InnerMessage.DataPackage innerMessage;
//		try {
//			innerMessage = InnerMessage.DataPackage.parseFrom(data);
//			MessageEvent source = new MessageEvent();
//			source.setData(innerMessage.getData().toByteArray());
//			source.setPlayerId(innerMessage.getPlayerId());
//			source.setMainType(innerMessage.getMainType());
//			source.setSubType(innerMessage.getSubType());
//			logger.debug(address + "-ignite.message, playerId:"+innerMessage.getPlayerId()+" source: " + JacksonUtil.encode(source));
//			manager.get(source.getPlayerId()).messageEventProducer().onData(source);
//		} catch (InvalidProtocolBufferException e) {
//			logger.error(e.getMessage(), e);
//		}
	}
}
