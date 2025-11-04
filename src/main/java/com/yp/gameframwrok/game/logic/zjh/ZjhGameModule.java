package com.yp.gameframwrok.game.logic.zjh;

import com.yp.gameframwrok.game.manager.GameModule;

/**
 * @author yyp
 */
public class ZjhGameModule extends GameModule<ZjhRoomConfig,ZjhRoom> {

    public void createRoom() {
        ZjhRoomConfig roomConfig = new ZjhRoomConfig();
        roomConfig.init();
    }

    public void joinRoom(int roomId, int userId) {

    }

}
