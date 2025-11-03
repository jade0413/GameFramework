package com.yp.gameframwrok.code;

// TODO:后续将message的各种errorCode考虑整合进来这里
public enum CommonErrorCode {
    SUCCESS(0), // 成功
    FAILED(1), // 失败
    ErrorMessageType(2), //错误消息类型 = 2;
    GoldNotEnough(5), //金币不足 = 5;
    StatusError(6), //状态错误 = 8;

    DataFormatError(101), //数据格式错误
    RepeatNickName(102), //昵称重复
    ErrorNameLen(103), //昵称长度错误
    IllegalityCoed(104), //非法字符
    WITHDRAW_PASSWORD_ERROR(105), // 提现密码错误
    WITHDRAW_GOLD_ERROR(106), // 提现金额不对
    WITHDRAW_GOLD_WATER_NOT_ENOUGH(107), // 提现流水不足
    WITHDRAW_REPEATED(108), // 不能重复提现，上一条还在处理中

    KickOutToHall(500), //踢回大厅，房间已经关闭

    ;
    private final int value;

    CommonErrorCode(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    /**
     * 有其他玩家登录
     */
    public static final int REPLACE_CONNECTION = 1000001;
}
