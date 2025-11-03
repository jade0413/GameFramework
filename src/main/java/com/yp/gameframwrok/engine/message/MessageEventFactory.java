package com.yp.gameframwrok.engine.message;

import com.lmax.disruptor.EventFactory;
/**
 * @author yyp
 */
public class MessageEventFactory implements EventFactory<MessageEvent> {

	@Override
	public MessageEvent newInstance() {
		return new MessageEvent();
	}
}
