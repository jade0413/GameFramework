package com.yp.gameframwrok.engine.message;

import com.google.protobuf.ByteString;
import lombok.Data;

@Data
public class MessageEvent {
    private ByteString data;

    private long playerId;

    private int mainType;

    private int subType;
}
