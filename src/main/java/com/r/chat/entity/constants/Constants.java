package com.r.chat.entity.constants;

public class Constants {
    // redis
    public static final String REDIS_KEY_PREFIX_CHECK_CODE = "rchat:checkcode:";  // 验证码结果保存路径前缀
    public static final String REDIS_KEY_PREFIX_WS_HEART_BEAT = "rchat:ws:heartbeat:";  // ws的心跳保存路径前缀
    public static final String REDIS_KEY_PREFIX_USER_TOKEN = "rchat:user:token:";  // 用户token保存路径前缀
    public static final String REDIS_KEY_PREFIX_USER_ID = "rchat:user:id:";  // 用户id保存路径前缀
    public static final String REDIS_KEY_SYS_SETTINGS = "rchat:sys:settings";  // 系统设置保存路径

    // length
    public static final Integer LENGTH_ID = 11;  // 用户id及群聊id的长度
    public static final Integer LENGTH_TOKEN_RANDOM_CHARS = 20;  // token中拼接的随机字符串的长度
    // size
    public static final Integer SIZE_DEFAULT_PAGE_SIZE = 15;

    // exception message
    public static final String MESSAGE_SUCCESS = "请求成功";
    public static final String MESSAGE_INTERNAL_ERROR = "服务器内部错误";
    public static final String MESSAGE_NOT_FOUND = "请求地址不存在";
    public static final String MESSAGE_ILLEGAL_OPERATION = "非法操作";
    public static final String MESSAGE_PARAMETER_ERROR = "参数错误";
    public static final String MESSAGE_CHECK_CODE_ERROR = "验证码错误";
    public static final String MESSAGE_EMAIL_ALREADY_REGISTERED = "邮箱已注册";
    public static final String MESSAGE_USER_NOT_EXIST = "用户不存在";
    public static final String MESSAGE_PASSWORD_ERROR = "密码错误";
    public static final String MESSAGE_ACCOUNT_ALREADY_LOGIN = "账号已在别处登录";
    public static final String MESSAGE_ACCOUNT_DISABLE = "账号被锁定";
    public static final String MESSAGE_NOT_LOGIN = "登录超时";
    public static final String MESSAGE_GROUP_COUNT_LIMIT = "拥有的群聊数量达到上限: %d";
    public static final String MESSAGE_MISSING_AVATAR_FILE = "缺少头像文件";
    public static final String MESSAGE_FAILED_TO_SAVE_AVATAR_FILE = "头像保存失败";
    public static final String MESSAGE_GROUP_NOT_EXIST = "群聊不存在";
    public static final String MESSAGE_GROUP_ALREADY_DISBAND = "群聊已解散";
    public static final String MESSAGE_NOT_IN_THE_GROUP = "不在此群聊";
    public static final String MESSAGE_BING_BLOCKED = "已被拉黑";

    // file
    public static final String FILE_FOLDER_AVATAR = "avatar";
    public static final String FILE_SUFFIX_AVATAR = ".png";
    public static final String FILE_SUFFIX_COVER = "_cover.png";

    // other
    public static final String IN_SWITCH_DEFAULT = "进入default分支";
}
