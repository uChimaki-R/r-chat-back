package com.r.chat.entity.constants;

public class Constants {
    // redis
    public static final String REDIS_KEY_PREFIX_CHECK_CODE = "rchat:checkcode:";  // 验证码结果保存路径前缀
    public static final String REDIS_KEY_PREFIX_WS_HEART_BEAT = "rchat:ws:heartbeat:";  // ws的心跳保存路径前缀
    public static final String REDIS_KEY_PREFIX_USER_TOKEN = "rchat:user:token:";  // 用户token保存路径前缀
    public static final String REDIS_KEY_PREFIX_USER_ID = "rchat:user:id:";  // 用户id保存路径前缀
    public static final String REDIS_KEY_SYS_SETTINGS = "rchat:sys:settings";  // 系统设置保存路径

    // length
    public static final Integer LENGTH_ID = 11;  // 用户id及群组id的长度
    public static final Integer LENGTH_TOKEN_RANDOM_CHARS = 20;  // token中拼接的随机字符串的长度

    // exception message
    public static final String MESSAGE_SUCCESS = "请求成功";
    public static final String MESSAGE_INTERNAL_ERROR = "服务器内部错误";
    public static final String MESSAGE_NOT_FOUND = "请求地址不存在";
    public static final String MESSAGE_CHECK_CODE_ERROR = "验证码错误";
    public static final String MESSAGE_EMAIL_ALREADY_REGISTERED = "邮箱已注册";
    public static final String MESSAGE_ACCOUNT_NOT_EXIST = "账号不存在";
    public static final String MESSAGE_PASSWORD_ERROR = "密码错误";
    public static final String MESSAGE_ACCOUNT_ALREADY_LOGIN = "账号已在别处登录";
    public static final String MESSAGE_ACCOUNT_DISABLE = "账号被锁定";
    public static final String MESSAGE_NOT_LOGIN = "登录超时";
    public static final String MESSAGE_GROUP_COUNT_LIMIT = "拥有的群组数量达到上限";
    public static final String MESSAGE_MISSING_AVATAR_FILE = "缺少头像文件";
}
