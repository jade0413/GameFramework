package com.yp.gameframwrok.game.logic.dn;

import com.yp.gameframwrok.game.model.GamePlayer;

/**
 * @author yyp
 */
public class DhPlayer extends GamePlayer {

    // 玩家当前手牌
    private final int[] cards = new int[5];

    /**
     * 当前玩家金币数
     */
    private int curGold;
    /**
     * 金币流水
     */
    private int flowGold;
    /**
     * 玩家状态
     */
    private int status;
    /**
     * 玩家是否可以操作
     */
    private int canAction;

    /**
     * 玩家操作类型
     */
    private int actionType;


    public DhPlayer(int playerId, int enterGold) {
        super(playerId, enterGold);
    }
}
