package com.r.chat.websocket.handler;

import com.r.chat.redis.RedisUtils;
import com.r.chat.websocket.utils.ChannelUtils;
import com.r.chat.websocket.utils.URLUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@ChannelHandler.Sharable
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final RedisUtils redisUtils;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String userId = ChannelUtils.getUserId(channelHandlerContext.channel());
        log.info("收到来自 {} 的消息: {}", userId, textWebSocketFrame.text());
        // 保存用户的心跳
        redisUtils.setUserHeartBeat(userId);
        // 测试发送到群聊
        ChannelUtils.sendMessage2Group("111", textWebSocketFrame.text());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有连接断开");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // ws连接成功的时候需要将userId和channel绑定（设置到附件中）
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            // 握手结束后
            WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            // 获取连接uri, 从uri中获取token信息
            String uri = handshakeComplete.requestUri();
            log.info("建立ws连接 uri: {}", uri);
            String token = URLUtils.getParamsByKey(uri, "token");
            if (token == null) {
                log.warn("ws断开连接: 未携带token");
                ctx.close();
                return;
            }
            // 获取userId
            String userId = redisUtils.getUserIdByToken(token);
            if (userId == null) {
                log.warn("ws断开: token不合法");
                ctx.close();
                return;
            }
            log.info("ws连接成功 userId: {}", userId);
            // 将userId和channel绑定
            ChannelUtils.addContext(userId, "111", ctx.channel());
        }
    }
}
