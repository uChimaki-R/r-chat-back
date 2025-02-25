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
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class NettyWebSocketStarter implements CommandLineRunner {
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final HeartbeatHandler heartbeatHandler;
    private final WebSocketHandler webSocketHandler;

    private final AppProperties appProperties;

    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public void start() {
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
            // 为了在本地开启多个服务器实例，springboot的server端口可以通过-Dserver.port修改，这是因为spring自己做了判断虚拟机参数是否存在的逻辑
            // 项目里还要用到ws端口，每个服务器实例在一台机子上需要是不同的端口，可以模仿spring的操作，通过判断是否配置了虚拟机参数来修改ws的端口
            Integer wsPort = appProperties.getWsPort();  // 默认从配置文件中获取端口号
            String vWsPort = System.getProperty("ws.port");  // 从传递的虚拟机参数中获取（虚拟机参数写如-Dws.port=7070）
            if (vWsPort != null) {
                // 设置了虚拟机参数就优先使用虚拟机参数
                wsPort = Integer.parseInt(vWsPort);
            }
            ChannelFuture channelFuture = bootstrap.bind(wsPort).sync();
            log.info("netty启动成功 绑定端口: {}", wsPort);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("netty异常: {}", e.getCause().getMessage());
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        this.start();
    }
}
