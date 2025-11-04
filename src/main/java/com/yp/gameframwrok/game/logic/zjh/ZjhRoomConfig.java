package com.yp.gameframwrok.game.logic.zjh;

import com.yp.gameframwrok.game.CardsDefine;
import com.yp.gameframwrok.game.model.BaseRoomConfig;
import lombok.Data;

import java.util.List;

/**
 * @author yyp
 */
@Data
public class ZjhRoomConfig extends BaseRoomConfig {

    public int maxGold = 1000000;

    public int minGold = 10000;

    public int maxBet = 1000000;

    public int maxPlayerNum = 4;

     public int minPlayerNum = 2;

     public List<Integer> cardPool ;

     public void init() {
        cardPool = CardsDefine.toList();
    }

}
