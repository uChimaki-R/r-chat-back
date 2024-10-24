package com.r.chat.websocket.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.r.chat.entity.enums.IdPrefixEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.result.Message;
import com.r.chat.entity.vo.ChatSessionUserVO;
import com.r.chat.entity.message.WsInitMessage;
import com.r.chat.mapper.ChatSessionUserMapper;
import com.r.chat.mapper.UserInfoMapper;
import com.r.chat.properties.AppProperties;
import com.r.chat.redis.RedisUtils;
import com.r.chat.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
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
     * groupId->channelGroup
     */
    private static final ConcurrentMap<String, ChannelGroup> GROUP_CHANNEL_MAP = new ConcurrentHashMap<>();

    private final RedisUtils redisUtils;
    private final UserInfoMapper userInfoMapper;
    private final AppProperties appProperties;
    private final ChatSessionUserMapper chatSessionUserMapper;
    // 工具类的方法却不使用静态的原因: 需要用到其他bean对象（redisUtils、mapper等），静态注入比较麻烦，把工具类也交给IOC管理，要用再注入就行了（因为也只有netty用，不算麻烦）

    /**
     * 在有效连接建立时调用
     * 1. 双向绑定channel<->userId
     * 2. 将用户的channel加入到用户加入的群聊对应的channelGroup中
     * 3. 添加用户心跳缓存
     * 4. 更新用户最后登陆时间
     * 5. 获取用户所有会话消息、上次下线后的未读聊天信息、好友申请数量
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
        log.info("绑定channel {}", channel);
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

        // 添加用户心跳缓存
        redisUtils.setUserHeartBeat(userId);

        // 更新用户最后登陆时间
        UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
        Long lastLoginTime = System.currentTimeMillis();
        updateWrapper.lambda()
                .eq(UserInfo::getUserId, userId)
                .set(UserInfo::getLastLoginTime, lastLoginTime);
        userInfoMapper.update(null, updateWrapper);
        log.info("更新用户最后登陆时间 lastLoginTime: {}", lastLoginTime);

        // 获取用户上次下线后的未读聊天信息
        // 从配置里找能够从服务端获取的最长未读聊天信息的天数，如果上次离线时间超过了这个时间，也只能获取配置的最大天数前的信息
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserInfo::getUserId, userId);
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
        Long fromTime = userInfo.getLastOffTime();
        long now = System.currentTimeMillis();
        if (now - fromTime >= Duration.ofDays(appProperties.getMaxUnreadChatFetchDays()).toMillis()) {
            fromTime = now - Duration.ofDays(appProperties.getMaxUnreadChatFetchDays()).toMillis();
        }

        // 获取用户所有会话消息
        List<ChatSessionUserVO> chatSessionUserVOList = chatSessionUserMapper.selectChatSessionUserVOList(userId);
        log.info("获取所有会话消息 {}", chatSessionUserVOList);

        // 发送ws初始化消息
        WsInitMessage wsInitMessage = new WsInitMessage();
        wsInitMessage.setChatSessionUserList(chatSessionUserVOList);
        log.info("发送ws初始化消息 {}", wsInitMessage);
        sendMessage2User(userId, userId, wsInitMessage);
    }

    /**
     * 连接断开时调用
     * 1. 移除用户channel
     * 2. 移除用户心跳缓存
     * 3. 更新用户最后离线时间
     */
    public void removeChannel(Channel channel) {
        // 移除用户channel
        String userId = getUserId(channel);
        USER_CHANNEL_MAP.remove(userId);
        log.info("移除绑定 channel: {}", channel);
        // 移除心跳缓存
        redisUtils.removeUserHeartBeat(userId);
        // 更新用户最后离线时间
        UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
        Long lastOffTime = System.currentTimeMillis();
        updateWrapper.lambda()
                .eq(UserInfo::getUserId, userId)
                .set(UserInfo::getLastOffTime, lastOffTime);
        userInfoMapper.update(null, updateWrapper);
        log.info("更新用户最后离线时间 lastOffTime: {}", lastOffTime);
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
        log.info("添加channel到groupId对应的channelGroup groupId: {}, channelGroup: {}", groupId, channelGroup);
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
    public void sendMessage2User(String from, String to, Message message) {
        message.setSendUserId(from);
        message.setContactId(to);
        message.setSendTime(System.currentTimeMillis());
        Channel channel = USER_CHANNEL_MAP.get(to);
        if (channel == null) {
            return;
        }
        channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.obj2Json(message)));
        log.info("{} 发送消息给 {}, message: {}", from, to, message);
    }

    /**
     * 发送消息到群聊
     */
    public void sendMessage2Group(String groupId, String message) {
        ChannelGroup group = GROUP_CHANNEL_MAP.get(groupId);
        if (group == null) {
            return;
        }
        group.writeAndFlush(new TextWebSocketFrame(message));
        log.info("发送消息到群聊 {}, message: {}", groupId, message);
    }
}
