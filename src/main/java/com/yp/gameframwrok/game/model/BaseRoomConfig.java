package com.yp.gameframwrok.game.model;

import lombok.Data;

/**
 * @author yyp
 */
@Data
public class BaseRoomConfig {
     /**
      * 房间最大人数
      */
    protected int maxPlayerNum;
     /**
      * 房间最小人数
      */
    protected int minPlayerNum;
     /**
      * 房间类型
      */
    private int roomType;

}
