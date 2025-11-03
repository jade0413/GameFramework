package com.yp.gameframwrok.game.logic.zjh.handler;

import com.google.protobuf.ByteString;
import com.yp.gameframwrok.engine.message.IMessageHandler;
import com.yp.gameframwrok.game.annotation.GameHandler;
import com.yp.gameframwrok.game.enums.EGameAction;
import com.yp.gameframwrok.game.enums.EGameType;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@GameHandler(value = EGameType.ZJH, action = EGameAction.JOIN_ROOM)
public class JoinHandler implements IMessageHandler {

    @Override
    public void handle(ByteString data, long playerId) {
        log.info("ZjhJoinHandler handle data: {}", data);

    }
}
