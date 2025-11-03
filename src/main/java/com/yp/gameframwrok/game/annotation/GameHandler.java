package com.yp.gameframwrok.game.annotation;

import com.yp.gameframwrok.game.enums.EGameAction;
import com.yp.gameframwrok.game.enums.EGameType;

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

    EGameType value();

     EGameAction action();
}
