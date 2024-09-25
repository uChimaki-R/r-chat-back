package com.r.chat.entity.constants;

public class Constants {
    public static final String REDIS_KEY_CHECK_CODE_PREFIX = "rchat:checkcode:";  // 验证码结果保存前缀
    public static final String REDIS_KEY_WS_HEART_BEAT_PREFIX = "rchat:ws:heartbeat:";  // ws的心跳保存前缀
    public static final String REDIS_KEY_USER_TOKEN_PREFIX = "rchat:user:token:";  // 用户token保存前缀
    public static final String REDIS_KEY_USER_ID_PREFIX = "rchat:user:id:";  // 用户id保存前缀

    public static final Integer LENGTH_ID = 11;  // 用户id及群组id的长度
    public static final Integer LENGTH_TOKEN_RANDOM_CHARS = 20;  // token中拼接的随机字符串的长度
}
