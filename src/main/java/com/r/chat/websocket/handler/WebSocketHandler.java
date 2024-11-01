package com.r.chat.websocket.handler;

import com.r.chat.entity.dto.UserTokenInfoDTO;
import com.r.chat.redis.RedisUtils;
import com.r.chat.utils.StringUtils;
import com.r.chat.websocket.utils.ChannelUtils;
import com.r.chat.websocket.utils.URLUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@ChannelHandler.Sharable
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final RedisUtils redisUtils;
    private final ChannelUtils channelUtils;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String userId = channelUtils.getUserId(channelHandlerContext.channel());
        // 更新保存用户的心跳
        redisUtils.setUserHeartBeat(userId);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("断开连接");
        String userId = channelUtils.getUserId(ctx.channel());
        MDC.put("ws", " ws: disconnect " + userId);
        channelUtils.removeChannel(userId);
        MDC.remove("ws");
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
            if (StringUtils.isEmpty(token)) {
                log.warn("ws断开连接: 未携带token");
                ctx.close();
                return;
            }
            // 获取用户信息
            UserTokenInfoDTO userTokenInfo = redisUtils.getUserTokenInfoByToken(token);
            if (userTokenInfo == null) {
                log.warn("ws断开: token不合法");
                ctx.close();
                return;
            }
            log.info("ws连接成功 {}", userTokenInfo);
            // 保存自定义的日志输出标识
            MDC.put("ws", " ws: connect " + userTokenInfo.getUserId());
            // 将userId和channel绑定
            channelUtils.initChannel(userTokenInfo.getUserId(), ctx.channel());
            MDC.remove("ws");
        }
    }
}
