package com.yp.gameframwrok.enums;

import lombok.Getter;

/**
 * @author yyp
 */
public enum ESubType {

     VERIFY(1000,"校验"),
     JOIN_ROOM(1001, "加入房间"),
     OPERATION(1002, "游戏操作"),
     ROOM(1003, "房间操作,(退出 换房间)"),
     OFFLINE(1004, "离线操作"),
     RECONNECT(1005, "重连操作"),
     ;

     @Getter
     private final int code;

     @Getter
     private final String name;

     ESubType(int code, String name) {
         this.code = code;
         this.name = name;
     }

     public static ESubType valueOf(int code) {
        for (ESubType gameAction : values()) {
            if (gameAction.getCode() == code) {
                return gameAction;
            }
        }
        throw new IllegalArgumentException("No enum constant with code " + code);
    }
}
