package com.yp.gameframwrok.game.enums;

public enum ERoomStatus {
    Normal(0),
    Playing(1),
    End(2),
    Close(3),
    //异常状态，强行关闭牌桌
    ERROR(88),
    //特殊情况需要锁住牌桌的时候使用
    Lock(99);

    private final int type;

    ERoomStatus(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}
