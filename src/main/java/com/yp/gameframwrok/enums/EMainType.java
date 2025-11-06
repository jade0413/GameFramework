package com.yp.gameframwrok.enums;

import lombok.Getter;

/**
 * @author yyp
 */
public enum EMainType {

    BASE(1000,"基础服务"),
    ZJH(1001, "扎金花"),
    DN(1002,"斗牛"),
    ;

    @Getter
    private final int code;

    @Getter
    private final String name;

    EMainType(int code, String name) {
        this.code = code;
        this.name = name;
    }
    public static EMainType valueOf(int code) {
        for (EMainType gameType : values()) {
            if (gameType.getCode() == code) {
                return gameType;
            }
        }
        throw new IllegalArgumentException("No enum constant with code " + code);
    }
}
