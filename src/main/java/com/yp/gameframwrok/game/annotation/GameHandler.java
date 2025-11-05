package com.yp.gameframwrok.game.annotation;

import com.yp.gameframwrok.enums.ESubType;
import com.yp.gameframwrok.enums.EMainType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yyp
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GameHandler {

    EMainType value();

    ESubType action();
}
