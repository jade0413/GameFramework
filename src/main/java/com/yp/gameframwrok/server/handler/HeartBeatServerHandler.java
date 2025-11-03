package com.yp.gameframwrok.server.handler;

import com.yp.gameframwrok.server.manager.ISessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yyp
 */
@Log4j2
@Component
@ChannelHandler.Sharable
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    ISessionManager sessionManager;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("读写心跳检测: " + evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                log.info("{} 读写空闲，关闭连接", ctx.channel().remoteAddress());
                sessionManager.removeSession(ctx.channel().id().hashCode());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
