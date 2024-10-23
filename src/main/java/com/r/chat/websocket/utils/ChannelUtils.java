package com.r.chat.websocket.utils;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.r.chat.entity.enums.IdPrefixEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.mapper.UserInfoMapper;
import com.r.chat.redis.RedisUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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

    private final RedisUtils redisUtils;
    private final UserInfoMapper userInfoMapper;
    // 工具类的方法却不使用静态的原因: 需要用到其他bean对象（redisUtils、mapper等），静态注入比较麻烦，把工具类也交给IOC管理，要用再注入就行了（因为也只有netty用，不算麻烦）

    /**
     * 在有效连接建立时调用
     * 1. 双向绑定channel<->userId
     * 2. 将用户的channel加入到用户加入的群聊对应的channelGroup中
     * 3. 更新用户最后登陆时间
     */
    public void initChannel(String userId, Channel channel) {
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
        // 将用户的channel加入到用户加入的群聊对应的channelGroup中
        // 从redis中获取用户的联系人id列表
        List<String> userContactIds = redisUtils.getUserContactIds(userId);
        for (String contactId : userContactIds) {
            UserContactTypeEnum contactType = Objects.requireNonNull(IdPrefixEnum.getByPrefix(contactId.charAt(0))).getUserContactTypeEnum();
            if (UserContactTypeEnum.GROUP.equals(contactType)) {
                // 群聊联系人
                add2Group(contactId, channel);
            }
        }
        // 更新用户最后登陆时间
        UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(UserInfo::getUserId, userId)
                .set(UserInfo::getLastLoginTime, LocalDateTime.now());
        userInfoMapper.update(null, updateWrapper);
    }

    /**
     * 将用户的channel加入到groupId对应的channelGroup中
     */
    public void add2Group(String groupId, Channel channel) {
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
    public String getUserId(Channel channel) {
        return (String) channel.attr(AttributeKey.valueOf(channel.id().asLongText())).get();
    }

    /**
     * 发送消息给用户
     */
    public void sendMessage2User(String userId, String message) {
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
    public void sendMessage2Group(String groupId, String message) {
        ChannelGroup group = GROUP_CHANNEL_MAP.get(groupId);
        if (group == null) {
            return;
        }
        log.info("发送消息到群聊 {}, message: {}", groupId, message);
        group.writeAndFlush(new TextWebSocketFrame(message));
    }
}
