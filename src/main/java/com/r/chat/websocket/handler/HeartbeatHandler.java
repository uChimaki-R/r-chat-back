package com.r.chat.websocket.handler;

import com.r.chat.websocket.utils.ChannelUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@ChannelHandler.Sharable
@Component
@RequiredArgsConstructor
public class HeartbeatHandler extends ChannelDuplexHandler {
    private final ChannelUtils channelUtils;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:
                    log.debug("{} 心跳间隔过长, 关闭连接", channelUtils.getUserId(ctx.channel()));
                    ctx.close();
                case WRITER_IDLE:
                    log.debug("到达发送心跳间隔, 向 {} 发送心跳", channelUtils.getUserId(ctx.channel()));
                    ctx.writeAndFlush("heartbeat");
                    break;
            }
        }
    }
}
