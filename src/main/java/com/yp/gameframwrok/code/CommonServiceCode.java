package com.yp.gameframwrok.code;

public class CommonServiceCode {
    /**
     * 牌桌玩家状态推送
     */
    public static final int TablePlayerStatusPush = 50001;

    /**
     * 房间配置加载
     */
    public static final int RoomConfigLoad = 50002;

    /**
     * 房间配置的回复
     */
    public static final int RoomConfigRes = 50003;

    /**
     * 重连操作
     */
    public static final int ReConnect = 20001;

    /**
     * 弹框推送
     */
    public static final int TipsPush = 20002;

    /**
     * 退出匹配
     */
    public static final int QuitMatch = 20003;

    /**
     *
     */
    public static final int QuitMatchRes = 20005;

    /**
     * 获取玩家金币
     */
    public static final int GetGold = 20004;

    /**
     * 踢出玩家到大厅
     */
    public static final int KickOut = 20010;

    /**
     * 配置更新，根据不同类型更新不同配置
     */
    public static final int CONFIG_UPDATE = 60001;

    /****************gate service code  需要通过网关发送的信息**************/
    //大厅信息
    public static final int LobbyInfo = 10000;
    //离线操作
    public static final int OFFLINE = 10001;
    //金币变动`
    public static final int GoldPush = 10002;
    //用户操作(昵称,头像)
    public static final int PlayerModify = 10003;
    //推荐游戏
    public static final int RecommendGame = 10004;
    //跑马灯
    public static final int AnnouncementNotify = 10005;
    //拉取游戏记录
    public static final int GameRecordInfo = 10006;
    //更新房间房间配置
    public static final int UPDATE_GAME_CONFIG = 10007;
    /**
     * 游戏
     */
    public static final int MatchTableReq = 100010;
}
