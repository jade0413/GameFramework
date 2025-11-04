package com.yp.gameframwrok.game.model;

import com.yp.gameframwrok.game.enums.ERoomStatus;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author yyp
 */
@Data
public abstract class BaseRoom<RC extends BaseRoomConfig,BP extends GamePlayer>  {
    protected int roomId;

    protected RC roomConfig;

    protected List<BP> playerList;

    protected Date startTime;

    protected List<Integer> cardList;
    /**
     * 房间全局状态
     */
    protected ERoomStatus tableStatus;

    protected int curPlayerPosIndex;

    protected int curRound;

    protected long timeout; // 超时操作时间

    public void init(RC roomConfig){
        this.roomConfig = roomConfig;
        this.curPlayerPosIndex = 0;
    }

    public abstract void start();

    public abstract void joinPlayer();

    /**
     * 是否超时
     */
    public boolean isTimeOut(long cur) {
        return timeout < cur;
    }

    /**
     * 超时业务处理
     */
    public abstract void timeOut();

}
