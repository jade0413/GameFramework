package com.yp.gameframwrok.game.enums;

import lombok.Getter;

/**
 * @author yyp
 */
public enum EGameType {
    ZJH(1001, "扎金花"),
    DN(1002,"斗牛"),
    ;

    @Getter
    private final int code;

    @Getter
    private final String name;

    EGameType(int code, String name) {
        this.code = code;
        this.name = name;
    }
    public static EGameType valueOf(int code) {
        for (EGameType gameType : values()) {
            if (gameType.getCode() == code) {
                return gameType;
            }
        }
        throw new IllegalArgumentException("No enum constant with code " + code);
    }
}
