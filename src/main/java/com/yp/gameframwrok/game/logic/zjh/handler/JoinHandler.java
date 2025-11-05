package com.yp.gameframwrok.game.logic.zjh.handler;

import com.google.protobuf.ByteString;
import com.yp.gameframwrok.engine.core.UserCacheManger;
import com.yp.gameframwrok.engine.message.IMessageHandler;
import com.yp.gameframwrok.game.annotation.GameHandler;
import com.yp.gameframwrok.enums.ESubType;
import com.yp.gameframwrok.enums.EMainType;
import com.yp.gameframwrok.model.cache.UserCache;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@GameHandler(value = EMainType.ZJH, action = ESubType.JOIN_ROOM)
public class JoinHandler implements IMessageHandler {


    @Autowired
    private UserCacheManger userCacheManger;

    @Override
    public void handle(ByteString data, int userId) {
        log.info("ZjhJoinHandler handle data: {}", data);
        // 获取用户信息
        UserCache userCache = userCacheManger.getUser(userId);
        if (userCache == null) {
            throw new RuntimeException("用户不存在");
        }

    }
}
