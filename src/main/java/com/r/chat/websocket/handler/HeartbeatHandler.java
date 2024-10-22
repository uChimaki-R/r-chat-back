package com.r.chat.websocket.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@ChannelHandler.Sharable
@Component
public class HeartbeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:
                    log.info("客户端心跳间隔过长, 关闭连接");
                    ctx.close();
                case WRITER_IDLE:
                    log.info("到达发送心跳间隔, 向客户端发送心跳");
                    ctx.writeAndFlush("heartbeat");
                    break;
            }
        }
    }
}
