package com.r.chat.websocket;

import com.r.chat.properties.AppProperties;
import com.r.chat.websocket.handler.HeartbeatHandler;
import com.r.chat.websocket.handler.WebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class NettyWebSocketStarter implements Runnable {
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final HeartbeatHandler heartbeatHandler;
    private final WebSocketHandler webSocketHandler;

    private final AppProperties appProperties;

    @PostConstruct
    public void init() {
        new Thread(this).start();
    }

    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void run() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline()
                                // http协议编码解码
                                .addLast(new HttpServerCodec())
                                // 聚合解码 保证http接收完整性
                                .addLast(new HttpObjectAggregator(64 * 1024))
                                // 心跳机制
                                .addLast(new IdleStateHandler(appProperties.getHeartbeatInterval(), 0, 0, TimeUnit.SECONDS))
                                .addLast(heartbeatHandler)
                                // ws设置
                                .addLast(new WebSocketServerProtocolHandler("/ws", null, true, 64 * 1024, true, true, 10000L))
                                .addLast(webSocketHandler);
                    }
                });
        try {
            ChannelFuture channelFuture = bootstrap.bind(appProperties.getWsPort()).sync();
            log.info("netty启动成功 绑定端口: {}", appProperties.getWsPort());
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("netty异常: {}", e.getCause().getMessage());
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
