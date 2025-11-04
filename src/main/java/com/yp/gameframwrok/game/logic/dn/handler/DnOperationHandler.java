package com.yp.gameframwrok.game.logic.dn.handler;

import com.google.protobuf.ByteString;
import com.yp.gameframwrok.engine.message.IMessageHandler;
import com.yp.gameframwrok.game.annotation.GameHandler;
import com.yp.gameframwrok.game.enums.EGameAction;
import com.yp.gameframwrok.game.enums.EGameType;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@GameHandler(value = EGameType.DN, action = EGameAction.OPERATION)
public class DnOperationHandler implements IMessageHandler {

    @Override
    public void handle(ByteString data, long userId) {
        log.info("ZjhOperationHandler handle data: {}", data);
    }
}
