package com.yp.gameframwrok.game.model;

import lombok.Data;

/**
 * @author yyp
 */
@Data
public abstract class BasePlayer {
    protected int playerId;

    protected int playerLevel;
    /**
     * 玩家位置索引
     */
    protected int posIndex;

    /**
     * 玩家进入时金币数
     */
    protected int enterGold;

    /**
     * 当前玩家金币数
     */
    protected int curGold;

    /**
     * 金币流水
     */
    protected int flowGold;

    protected System nickName;

    protected String loginId;

    protected String headImgUrl;

    protected boolean isRobot;

    public BasePlayer(int playerId,int enterGold) {
        this.playerId = playerId;
        this.enterGold = enterGold;
        this.curGold = enterGold;
        this.flowGold = 0;
    }

}
