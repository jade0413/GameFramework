package com.yp.gameframwrok.engine.message;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import lombok.extern.log4j.Log4j2;

/**
 * @author yyp
 */
@Log4j2
public class MessageEventProducer {

    private final RingBuffer<MessageEvent> ringBuffer;

    public MessageEventProducer(RingBuffer<MessageEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<MessageEvent, MessageEvent> TRANSLATOR = (dest, sequence, source) -> {
        dest.setData(source.getData());
        dest.setMainType(source.getMainType());
        dest.setSubType(source.getSubType());
        dest.setPlayerId(source.getPlayerId());
    };

    public void onData(MessageEvent source) {
        ringBuffer.publishEvent(TRANSLATOR, source);
    }

}
