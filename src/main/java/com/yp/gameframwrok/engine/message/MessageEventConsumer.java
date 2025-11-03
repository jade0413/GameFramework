package com.yp.gameframwrok.engine.message;

import com.lmax.disruptor.EventHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class MessageEventConsumer implements EventHandler<MessageEvent> {

    @Autowired
    private  MessageManager messageManager;

    @Override
    public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) throws Exception {
        log.info("开始处理用户消息, playerId: {}, subType: {}", event.getPlayerId(), event.getSubType());
        IMessageHandler handler = messageManager.getHandler(event.getMainType(), event.getSubType());
        if (handler == null) {
            log.error("not handler, playerId: {}, subType: {}", event.getPlayerId(), event.getSubType());
        } else {
//            ServerStatManager.addStat(ServerStatManager.TOTAL_REQ);
            handler.handle(event.getData(), event.getPlayerId());
//            ServerStatManager.addStat(ServerStatManager.TOTAL_RES);
        }
    }
}
