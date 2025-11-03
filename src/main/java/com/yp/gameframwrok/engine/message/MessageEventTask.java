package com.yp.gameframwrok.engine.message;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import java.io.Serializable;
import java.util.concurrent.ThreadFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yyp
 */
@Log4j2
@Component
public class MessageEventTask implements Serializable {
    private static final long serialVersionUID = 1L;

    //指定事件工厂
    private final MessageEventFactory eventFactory = new MessageEventFactory();

    //指定ringbuffer字节大小，必须为2的N次方（能将求模运算转为位运算提高效率），否则将影响效
    private final int ringBufferSize = 1024;
    private final ThreadFactory threadFactory = Thread::new;

    private final Disruptor<MessageEvent> disruptor = new Disruptor<>(eventFactory, ringBufferSize, threadFactory);
    private final MessageEventProducer producer;

    @Autowired
    public MessageEventTask(MessageEventConsumer consumer) {
        //设置事件业务处理器---消费者
        disruptor.handleEventsWith(consumer);
        // 启动disruptor线程
        disruptor.start();
        //获取ringbuffer环，用于接取生产者生产的事件
        RingBuffer<MessageEvent> ringBuffer = disruptor.getRingBuffer();
        producer = new MessageEventProducer(ringBuffer);
    }

    public MessageEventProducer messageEventProducer() {
        return producer;
    }

    public void shutdown() {
        disruptor.shutdown();
    }
}
