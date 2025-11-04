package com.yp.gameframwrok.game.logic.zjh;

import com.yp.gameframwrok.game.model.BaseRoom;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yyp
 */
public class ZjhRoom extends BaseRoom<ZjhRoomConfig,ZjhPlayer> {

    /**
     * 当前 底池金额
     */
    private int pot;
    /**
     * 当前 操作玩家位置索引
     */
    private int curPosIndex;
    /**
     * 玩家列表
     */
    private List<ZjhPlayer> players ;

    private List<Integer> cardPool ;

    @Override
    public void init(ZjhRoomConfig roomConfig) {
        this.cardPool = roomConfig.getCardPool();
        this.players = new ArrayList<>(roomConfig.getMaxPlayerNum());
        this.curPosIndex = 0;
        this.pot = 0;
    }

    @Override
    public void start() {
//        this.tableStatus = ERoomStatus.START;
    }

    @Override
    public void joinPlayer() {

    }

    @Override
    public void timeOut() {

    }
}
