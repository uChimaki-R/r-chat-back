package com.r.chat.entity.constants;

public class Constants {
    // redis
    public static final String REDIS_KEY_PREFIX = "rchat:";
    public static final String REDIS_KEY_PREFIX_CHECK_CODE = REDIS_KEY_PREFIX + "checkcode:";  // 验证码结果保存路径前缀
    public static final String REDIS_KEY_PREFIX_WS_HEART_BEAT = REDIS_KEY_PREFIX + "ws:heartbeat:";  // ws的心跳保存路径前缀
    public static final String REDIS_KEY_PREFIX_USER_TOKEN = REDIS_KEY_PREFIX + "user:token:";  // 用户token保存路径前缀
    public static final String REDIS_KEY_SYS_SETTINGS = REDIS_KEY_PREFIX + "sys:settings";  // 系统设置保存路径

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
    public static final String MESSAGE_CAN_NOT_SEE_THE_FRIEND = "非法操作 与用户 [{}] 非好友(状态非: 好友/被删除/被拉黑)";
    public static final String MESSAGE_REPETITIVE_OPERATION = "重复操作";
    public static final String MESSAGE_PARAMETER_ERROR = "参数错误";
    public static final String MESSAGE_CHECK_CODE_ERROR = "验证码错误";
    public static final String MESSAGE_EMAIL_ALREADY_REGISTERED = "邮箱已注册";
    public static final String MESSAGE_USER_NOT_EXIST = "用户不存在";
    public static final String MESSAGE_PASSWORD_ERROR = "密码错误";
    public static final String MESSAGE_ACCOUNT_ALREADY_LOGIN = "账号已在别处登录";
    public static final String MESSAGE_ACCOUNT_DISABLE = "账号被锁定";
    public static final String MESSAGE_NOT_LOGIN = "登录超时";
    public static final String MESSAGE_GROUP_COUNT_LIMIT = "拥有的群聊数量达到上限: %d";
    public static final String MESSAGE_GROUP_MEMBER_COUNT_LIMIT = "群聊成员数量达到上限: %d";
    public static final String MESSAGE_MISSING_AVATAR_FILE = "缺少头像文件";
    public static final String MESSAGE_FAILED_TO_SAVE_AVATAR_FILE = "头像保存失败";
    public static final String MESSAGE_GROUP_NOT_EXIST = "群聊不存在";
    public static final String MESSAGE_GROUP_ALREADY_DISBAND = "群聊已解散";
    public static final String MESSAGE_NOT_IN_THE_GROUP = "不在此群聊";
    public static final String MESSAGE_BING_BLOCKED = "已被拉黑";
    public static final String MESSAGE_NOT_ADMIN = "非管理员账号操作";

    // validate failure message
    public static final String VALIDATE_EMPTY_CHECK_CODE = "验证码不能为空";
    public static final String VALIDATE_EMPTY_CHECK_CODE_KEY = "验证码唯一标识key不能为空";
    public static final String VALIDATE_EMPTY_EMAIL = "邮箱不能为空";
    public static final String VALIDATE_EMPTY_NICKNAME = "用户名不能为空";
    public static final String VALIDATE_EMPTY_PASSWORD = "密码不能为空";
    public static final String VALIDATE_EMPTY_CONTACT_ID = "联系人id不能为空";
    public static final String VALIDATE_EMPTY_GROUP_ID = "群聊id不能为空";
    public static final String VALIDATE_EMPTY_APPLY_ID = "申请id不能为空";
    public static final String VALIDATE_EMPTY_CONTACT_TYPE = "联系人类型不能为空";
    public static final String VALIDATE_EMPTY_CONTACT_APPLY_STATUS = "联系人申请状态不能为空";
    public static final String VALIDATE_ILLEGAL_GENDER = "性别信息有误";
    public static final String VALIDATE_ILLEGAL_NICK_NAME = "用户名信息有误";
    public static final String VALIDATE_ILLEGAL_EMAIL = "邮箱信息有误";
    public static final String VALIDATE_ILLEGAL_PASSWORD = "密码信息有误";

    // regex
    public static final String REGEX_NICK_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_-]{6,12}$";
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    public static final String REGEX_PASSWORD = "^(?![a-zA-Z]+$)(?!\\d+$)(?![^\\da-zA-Z\\s]+$).{8,}$";
    public static final String REGEX_MD5 = "^[a-fA-F0-9]{32}$";

    // file
    public static final String FILE_FOLDER_AVATAR = "avatar";
    public static final String FILE_SUFFIX_AVATAR = ".png";
    public static final String FILE_SUFFIX_COVER = "_cover.png";

    // other
    public static final String IN_SWITCH_DEFAULT = "进入default分支";
}
