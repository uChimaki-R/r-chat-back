package com.r.chat.websocket.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.IdPrefixEnum;
import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.notice.GroupMemberLeaveOrIsRemovedNotice;
import com.r.chat.entity.po.ChatMessage;
import com.r.chat.entity.po.UserContactApply;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatSessionUserVO;
import com.r.chat.entity.notice.WsInitNotice;
import com.r.chat.exception.ParameterErrorException;
import com.r.chat.mapper.ChatMessageMapper;
import com.r.chat.mapper.ChatSessionUserMapper;
import com.r.chat.mapper.UserContactApplyMapper;
import com.r.chat.mapper.UserInfoMapper;
import com.r.chat.properties.AppProperties;
import com.r.chat.redis.RedisUtils;
import com.r.chat.utils.JsonUtils;
import com.r.chat.utils.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelUtils {
    /**
     * userId->channel
     */
    private static final ConcurrentMap<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * userId->token
     */
    public static final ConcurrentMap<String, String> USER_TOKEN_MAP = new ConcurrentHashMap<>();

    /**
     * groupId->channelGroup
     */
    private static final ConcurrentMap<String, ChannelGroup> GROUP_CHANNEL_MAP = new ConcurrentHashMap<>();

    private final RedisUtils redisUtils;
    private final UserInfoMapper userInfoMapper;
    private final AppProperties appProperties;
    private final ChatSessionUserMapper chatSessionUserMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserContactApplyMapper userContactApplyMapper;

    /**
     * redis服务器可以作为向各spring-server服务器广播的中介，以此达成集群的通知传递
     * 比如说部署了多个server s0和s1，用户a连的ws是s0的，用户b连的ws是s1的，此时如果a想要找b聊天
     * 这种情况下a发送给b的消息在s0上是找不到b对应的channel的，因为b的channel在s1的USER_CHANNEL_MAP(内存)里
     * 解决方法是把信息发送给redisson，让他向所有server广播通知要发送的消息
     * 所有server收到通知后从通知中得到发送者和接收者和消息内容，如果接收者在自己的MAP里，则由自己发送有人发送消息的通知，从而解决集群问题
     */
    private final RedissonClient redissonClient;

    /**
     * redisson上的主题，按MQ消息队列理解
     */
    private final String TOPIC_NOTICE = "notice.topic";

    /**
     * 配置监听器，监听redis服务器广播的通知，如果符合想要的类型，则回调方法
     */
    @PostConstruct
    public void addRedissonListener() {
        // 需要给出监听的主题，和发送广播的主题一致即可
        RTopic rTopic = redissonClient.getTopic(TOPIC_NOTICE);
        rTopic.addListener(Notice.class, (charSequence, notice) -> {
            log.info("收到广播通知 {}", notice);
            // 收到广播后尝试处理这个通知（发送通知）
            sendNtc(notice);
        });
    }

    /**
     * 发送通知，实际上是将通知广播到所有的server服务器，让所有server服务器都尝试处理这个通知，以便集群部署
     */
    public void sendNotice(Notice notice) {
        RTopic rTopic = redissonClient.getTopic(TOPIC_NOTICE);
        log.info("发送广播通知 {}", notice);
        rTopic.publish(notice);
    }

    // 工具类的方法却不使用静态的原因: 需要用到其他bean对象（redisUtils、mapper等），静态注入比较麻烦，把工具类也交给IOC管理，要用再注入就行了（因为也只有netty用，不算麻烦）

    /**
     * 在有效连接建立时调用
     * 1. 双向绑定channel<->userId
     * 2. 将用户的channel加入到用户加入的群聊对应的channelGroup中
     * 3. 绑定userId->token
     * 4. 添加用户心跳缓存
     * 5. 更新用户最后登陆时间
     * 6. 获取用户所有会话消息、上次下线后的未读聊天信息、好友申请数量
     * 7. 发送ws初始化通知
     */
    public void initChannel(String userId, String token, Channel channel) {
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

        // 从redis中获取用户的联系人id列表
        List<String> contactIds = redisUtils.getContactIds(userId);
        List<String> groupContactIds;
        if (contactIds != null && !contactIds.isEmpty()) {
            // 获取群聊的id列表
            groupContactIds = contactIds.stream().filter(id -> IdPrefixEnum.GROUP.equals(IdPrefixEnum.getPrefix(id))).collect(Collectors.toList());
            // 将用户的channel加入到用户加入的群聊对应的channelGroup中
            groupContactIds.forEach(groupId -> {
                addUser2Group(userId, groupId);
            });
        } else {
            groupContactIds = new ArrayList<>();
        }
        log.info("将用户的channel加入到用户加入的群聊对应的channelGroup中 {}", groupContactIds);

        // 保存userId->token
        USER_TOKEN_MAP.put(userId, token);
        log.info("保存用户id到token的映射 token: {}", token);

        // 添加用户心跳缓存
        redisUtils.setUserHeartBeat(userId);
        log.info("添加用户心跳缓存");

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
        // 查接收者是自己或自己加入的群聊的信息
        // 上面已经查过自己加入的群聊id列表了，把自己也加入就行
        groupContactIds.add(userId);
        QueryWrapper<ChatMessage> messageQueryWrapper = new QueryWrapper<>();
        messageQueryWrapper.lambda()
                // 使用and给这两个条件打个括号
                .and(o -> {
                    o.in(ChatMessage::getContactId, groupContactIds)
                            .or().eq(ChatMessage::getSendUserId, userId); // 下线了也可能有是自己发的信息发出去了，比如说别人同意了自己的好友请求
                })
                .ge(ChatMessage::getSendTime, fromTime);  // 时间限制
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(messageQueryWrapper);
        log.info("获取用户上次下线后的未读聊天信息 {}", chatMessages);

        // 获取用户所有会话消息
        List<ChatSessionUserVO> chatSessionUserVOList = chatSessionUserMapper.selectChatSessionUserVOList(userId);
        log.info("获取所有会话消息 {}", chatSessionUserVOList);

        // 获取申请好友信息的数量
        QueryWrapper<UserContactApply> applyQueryWrapper = new QueryWrapper<>();
        applyQueryWrapper.lambda()
                .eq(UserContactApply::getReceiveUserId, userId)
                .ge(UserContactApply::getLastApplyTime, fromTime);  // 时间限制
        Long applyCount = userContactApplyMapper.selectCount(applyQueryWrapper);
        log.info("获取申请信息的数量: {}", applyCount);

        // 发送ws初始化通知
        WsInitNotice wsInitMessage = new WsInitNotice();
        wsInitMessage.setChatSessionUserList(chatSessionUserVOList);
        wsInitMessage.setChatMessageList(chatMessages);
        wsInitMessage.setApplyCount(applyCount);
        wsInitMessage.setReceiveId(userId);  // 原路返回
        log.info("发送ws初始化通知 {}", wsInitMessage);
        sendNtc(wsInitMessage);
    }

    /**
     * 连接断开或想要断开连接时调用
     * 1. 移除用户channel
     * 2. 移除用户心跳缓存
     * 3. 更新用户最后离线时间
     * 4. 移除token绑定
     * 5. 关闭channel
     */
    public void removeChannel(String userId) {
        if (StringUtils.isEmpty(userId)) {
            log.warn("userId为空");
            return;
        }
        Channel channel = USER_CHANNEL_MAP.get(userId);
        if (channel == null) {
            log.warn("{} 没有对应的channel, 用户可能未登录或登录在别的服务器, 取消removeChannel操作", userId);
            return;
        }
        USER_CHANNEL_MAP.remove(userId);
        log.info("移除channel绑定 userId: {}", userId);
        // 移除心跳缓存
        redisUtils.removeUserHeartBeat(userId);
        log.info("移除用户心跳缓存 userId: {}", userId);
        // 更新用户最后离线时间
        UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
        Long lastOffTime = System.currentTimeMillis();
        updateWrapper.lambda()
                .eq(UserInfo::getUserId, userId)
                .set(UserInfo::getLastOffTime, lastOffTime);
        userInfoMapper.update(null, updateWrapper);
        log.info("更新用户最后离线时间 lastOffTime: {}", lastOffTime);
        // 关闭channel（断开连接可能是客户端断的，也可能是服务端调用这个方法断的，如果是后者则需要close）
        String token = USER_TOKEN_MAP.get(userId);
        if (!StringUtils.isEmpty(token)) {
            USER_TOKEN_MAP.remove(userId);
            log.info("移除token绑定 token: {}", userId);
        }
        channel.close();
        log.info("已断开与 {} 的ws连接", userId);
    }

    /**
     * 将用户对应的channel从群聊对应的channelGroup中移除
     */
    public void removeChannelFromGroup(String userId, String groupId) {
        Channel channel = USER_CHANNEL_MAP.get(userId);
        if (channel == null) {
            log.warn("用户 {} 对应的channel不存在, 用户可能未登录或登录在别的服务器, 取消从channelGroup中移除channel", userId);
            return;
        }
        ChannelGroup group = GROUP_CHANNEL_MAP.get(groupId);
        if (group == null) {
            log.warn("群聊 {} 对应的channelGroup不存在, 取消从channelGroup中移除channel", groupId);
            return;
        }
        group.remove(channel);
        log.info("从channelGroup中移除channel groupId: {}, userId: {}", groupId, userId);
    }

    /**
     * 将用户的channel加入到groupId对应的channelGroup中
     */
    public void addUser2Group(String userId, String groupId) {
        // 添加进群聊channelGroup
        ChannelGroup channelGroup = GROUP_CHANNEL_MAP.get(groupId);
        if (channelGroup == null) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CHANNEL_MAP.put(groupId, channelGroup);
        }
        Channel channel = USER_CHANNEL_MAP.get(userId);
        if (channel == null) {
            log.warn("用户 {} 对应的channel不存在, 无法加入到channelGroup", userId);
            return;
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
     * 发送通知
     */
    private void sendNtc(Notice notice) {
        String receiveId = notice.getReceiveId();
        if (StringUtils.isEmpty(receiveId)) {
            log.warn("通知发送的对象id为空");
            return;
        }
        IdPrefixEnum prefix = IdPrefixEnum.getPrefix(receiveId);
        if (prefix == null) {
            log.warn("通知发送的对象id格式有误 receiveId: {}", receiveId);
            return;
        }
        MDC.put("ws", " ws: send to " + receiveId);
        switch (prefix) {
            case USER:
                sendNotice2User(notice);
                break;
            case GROUP:
                sendNotice2Group(notice);
                break;
            default:
                log.warn(Constants.IN_SWITCH_DEFAULT);
                throw new ParameterErrorException(Constants.IN_SWITCH_DEFAULT);
        }
        MDC.remove("ws");
    }

    /**
     * 发送通知给用户
     */
    private void sendNotice2User(Notice notice) {
        String receiveId = notice.getReceiveId();
        Channel channel = USER_CHANNEL_MAP.get(receiveId);
        if (channel == null) {
            log.warn("用户对应的channel不存在, 用户可能未登录或登录在别的服务器, 取消发送通知 {}", notice);
            return;
        }
        channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.obj2Json(notice)));
        log.info("发送ws通知给用户 {}", notice);
        // 某些操作如强制下线，因为集群的存在，想强制下线的人不一定在自己的服务器里登陆了，所以不能在强制下线的业务代码里调用removeChannel方法来关闭ws连接
        // 所以在接收到广播的时候，每个服务器都要判断是否接收到了强制下线的通知，都尝试从自己的服务器中关闭该ws连接
        if (NoticeTypeEnum.FORCE_OFFLINE.equals(notice.getNoticeType())) {
            log.info("接收到强制下线的通知, 尝试断开与 {} 的ws连接", notice.getReceiveId());
//            removeChannel(notice.getReceiveId());
            // 尝试找到对应的channel触发close即可，close触发后在channelInactive里会触发removeChannel方法，这里直接调用removeChannel方法的话会执行两次方法，打印的日志就不是很清晰（第二次会说没有对应的channel，实际上是在第一次里移除了而不是本身没有）
            Channel chan = USER_CHANNEL_MAP.get(notice.getReceiveId());
            if (chan == null) {
                log.warn("没有找到对应的channel, 用户可能未登录或登录在别的服务器, 取消断开ws的操作");
                return;
            }
            chan.close();
            // 强制下线需要清除用户登录缓存
            String token = USER_TOKEN_MAP.get(notice.getReceiveId());
            if (!StringUtils.isEmpty(token)) {
                redisUtils.removeUserTokenInfoByToken(token);
                log.info("清除用户登陆缓存 userId: {}, token: {}", notice.getReceiveId(), token);
            }
        }
    }

    /**
     * 发送通知到群聊
     */
    private void sendNotice2Group(Notice notice) {
        String groupId = notice.getReceiveId();
        ChannelGroup group = GROUP_CHANNEL_MAP.get(groupId);
        if (group == null) {
            log.warn("对应的channelGroup不存在, 可能是本服务器没有该群聊成员登录, 取消发送通知 {}",  notice);
            return;
        }
        group.writeAndFlush(new TextWebSocketFrame(JsonUtils.obj2Json(notice)));
        log.info("发送ws通知给群聊 {}", notice);
        // 和上面sendNotice2User的强制下线同理，群聊也有一些因为集群需要特殊处理的通知
        // 如被移出群聊，被移出的人不一定在自己的服务器登录，需要所有服务器尝试在对应的群聊channelGroup里找到这个人并断开连接
        // （因为退出群聊和被移出群聊都是走的IGroupInfoService.leaveGroup逻辑，所以也统一在这里断开连接）
        // 如解散群聊，群聊的成员分散在多个服务器的channelGroup里，需要将这些group都删除
        if (NoticeTypeEnum.GROUP_MEMBER_LEAVE_OR_IS_REMOVED.equals(notice.getNoticeType())) {
            GroupMemberLeaveOrIsRemovedNotice gNotice = (GroupMemberLeaveOrIsRemovedNotice) notice;
            String idToLeave = gNotice.getLeaveUserId();
            log.info("接收到退出群聊或被移出群聊的通知, 尝试将 {} 移出群聊 {} 对应的channelGroup", idToLeave, groupId);
            removeChannelFromGroup(idToLeave, groupId);
        } else if (NoticeTypeEnum.GROUP_DISBAND.equals(notice.getNoticeType())) {
            log.info("接收到解散群聊的通知, 尝试关闭 {} 对应的channelGroup", notice.getReceiveId());
            ChannelGroup channelGroup = GROUP_CHANNEL_MAP.get(groupId);
            if (channelGroup == null) {
                log.warn("群聊对应的channelGroup不存在, 取消关闭channelGroup");
                return;
            }
            GROUP_CHANNEL_MAP.remove(groupId);
            channelGroup.close();
            log.info("成功关闭群聊 {} 对应的channelGroup", groupId);
        }
    }
}
