package com.yp.gameframwrok.game.logic.zjh.handler;

import com.google.protobuf.ByteString;
import com.yp.gameframwrok.engine.message.IMessageHandler;
import com.yp.gameframwrok.game.annotation.GameHandler;
import com.yp.gameframwrok.enums.ESubType;
import com.yp.gameframwrok.enums.EMainType;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@GameHandler(value = EMainType.ZJH, action = ESubType.OPERATION)
public class OperationHandler implements IMessageHandler {

    @Override
    public void handle(ByteString data, int userId) {
        log.info("ZjhOperationHandler handle data: {}", data);
    }
}
