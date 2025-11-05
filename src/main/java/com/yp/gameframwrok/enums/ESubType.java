package com.yp.gameframwrok.enums;

import lombok.Getter;

/**
 * @author yyp
 */
public enum ESubType {

    VERIFY(1000,"校验"),

     /**
      * 操作
      */
     OPERATION(1001, "操作"),
     /**
      * 加入房间
      */
     JOIN_ROOM(1002, "加入房间"),
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
