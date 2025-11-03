package com.yp.gameframwrok.server.handler;

import com.yp.gameframwrok.server.core.Session;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Log4j2
public class VerifyHandler{

//    @Override
//    public void handle(String data, Session connection) {
//        Channel channel = connection.getConnection();
////        Long playerId = connection.getPlayerId();
////        Session oldConnection = ConnectionMgr.get(playerId);
////        if (oldConnection != null) {
//////            MessageSender.sendErrorMsgToClient(oldConnection.getChannel(), CommonErrorCode.REPLACE_CONNECTION, CommonServiceCode.TipsPush);
////            ConnectionMgr.close(oldConnection);
////        }
//
//
//    }

    private String getIpAddress(Channel channel) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
        String ipAddress = inetSocketAddress.getAddress().getHostAddress();
        if (ipAddress.equals("127.0.0.1")) {
            // 根据网卡取本机配置的IP
            try {
                ipAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return ipAddress;
    }
}
