package com.yp.gameframwrok.engine.message;

import com.google.protobuf.ByteString;

public interface IMessageHandler {
    void handle(ByteString data, int userId) throws Exception;
}
