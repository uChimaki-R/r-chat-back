package com.r.chat.websocket.utils;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelUtils {
    /**
     * userId->channel
     */
    private static final ConcurrentMap<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * groupId->channel
     */
    private static final ConcurrentMap<String, ChannelGroup> GROUP_CHANNEL_MAP = new ConcurrentHashMap<>();


    /**
     * 双向绑定channel<->userId
     */
    public static void addContext(String userId, Channel channel) {
        // 由于channel序列化之后后续无法使用，所以无法保存到redis中，只能直接保存到内存中
        // 这里使用附件绑定userId（channel->userId），使用线程安全的map通过userId找到channel（userId->channel）
        // （理论上channel->userId也可以用一个channelId到userId的map来保存，但是不够优雅）
        // 获取一个名为channelId的key
        String channelId = channel.id().asLongText();
        AttributeKey<String> key;
        // 为了线程安全的操作，需要先判断是否创建过该key
        if (!AttributeKey.exists(channelId)) {
            key = AttributeKey.newInstance(channelId);
        } else {
            key = AttributeKey.valueOf(channelId);
        }
        // 将这个名为channelId的key对应的value设置为userId（channel->userId）
        channel.attr(key).set(userId);
        // 保存到map（userId->channel）
        USER_CHANNEL_MAP.put(userId, channel);
    }

    /**
     * 双向绑定channel<->userId，并将用户的channel加入到groupId对应的channelGroup中
     */
    public static void addContext(String userId, String groupId, Channel channel) {
        addContext(userId, channel);
        // 添加进群聊channelGroup
        ChannelGroup channelGroup = GROUP_CHANNEL_MAP.get(groupId);
        if (channelGroup == null) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CHANNEL_MAP.put(groupId, channelGroup);
        }
        // 将用户的channel加入到群聊channelGroup中
        channelGroup.add(channel);
    }

    /**
     * 获取channel绑定的userId
     */
    public static String getUserId(Channel channel) {
        return (String) channel.attr(AttributeKey.valueOf(channel.id().asLongText())).get();
    }

    /**
     * 发送消息给用户
     */
    public static void sendMessage2User(String userId, String message) {
        Channel channel = USER_CHANNEL_MAP.get(userId);
        if (channel == null) {
            return;
        }
        log.info("发送消息给用户 {}, message: {}", userId, message);
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    /**
     * 发送消息到群聊
     */
    public static void sendMessage2Group(String groupId, String message) {
        ChannelGroup group = GROUP_CHANNEL_MAP.get(groupId);
        if (group == null) {
            return;
        }
        log.info("发送消息到群聊 {}, message: {}", groupId, message);
        group.writeAndFlush(new TextWebSocketFrame(message));
    }
}
