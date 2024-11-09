package com.r.chat.entity.constants;

public class Constants {
    // redis
    public static final String REDIS_KEY_PREFIX = "rchat:";
    public static final String REDIS_KEY_PREFIX_CHECK_CODE = REDIS_KEY_PREFIX + "checkcode:";  // 验证码结果保存路径前缀
    public static final String REDIS_KEY_PREFIX_WS_HEART_BEAT = REDIS_KEY_PREFIX + "ws:heartbeat:";  // ws的心跳保存路径前缀
    public static final String REDIS_KEY_PREFIX_USER_TOKEN = REDIS_KEY_PREFIX + "user:token:";  // 用户token保存路径前缀
    public static final String REDIS_KEY_SYS_SETTINGS = REDIS_KEY_PREFIX + "sys:settings";  // 系统设置保存路径
    public static final String REDIS_KEY_PREFIX_USER_CONTACT_IDS = REDIS_KEY_PREFIX + "ws:contact:";  // 用户联系人列表保存路径前缀

    // log message
    public static final String MESSAGE_CAN_NOT_SEE_THE_FRIEND = "非法操作 与 [{}] 非好友(状态非: 好友/被删除/被拉黑)";

    // exception message
    public static final String MESSAGE_SUCCESS = "请求成功";
    public static final String MESSAGE_INTERNAL_ERROR = "服务器内部错误";
    public static final String MESSAGE_NOT_FOUND = "请求地址不存在";
    public static final String MESSAGE_REQUEST_METHOD_ERROR = "请求方式错误";
    public static final String MESSAGE_ILLEGAL_OPERATION = "非法操作";
    public static final String MESSAGE_REPETITIVE_OPERATION = "重复操作";
    public static final String MESSAGE_PARAMETER_ERROR = "参数错误";
    public static final String MESSAGE_ENUM_ERROR = "枚举信息错误";
    public static final String MESSAGE_CHECK_CODE_ERROR = "验证码错误";
    public static final String MESSAGE_EMAIL_ALREADY_REGISTERED = "邮箱已注册";
    public static final String MESSAGE_BEAUTY_USER_ID_ALREADY_EXISTED = "靓号已存在";
    public static final String MESSAGE_ALREADY_HAVE_BEAUTY_ACCOUNT = "账号已经绑定过靓号了";
    public static final String MESSAGE_BEAUTY_USER_INFO_NOT_EXIST = "靓号信息不存在";
    public static final String MESSAGE_APP_UPDATE_NOT_EXIST = "app更新信息不存在";
    public static final String MESSAGE_CONTACT_APPLY_NOT_EXIST = "申请信息不存在";
    public static final String MESSAGE_CHAT_MESSAGE_NOT_EXIST = "聊天信息不存在";
    public static final String MESSAGE_USER_NOT_EXIST = "用户不存在";
    public static final String MESSAGE_PASSWORD_ERROR = "密码错误";
    public static final String MESSAGE_ACCOUNT_ALREADY_LOGIN = "账号已在别处登录";
    public static final String MESSAGE_ACCOUNT_DISABLE = "账号被锁定";
    public static final String MESSAGE_NOT_LOGIN = "登录超时";
    public static final String MESSAGE_GROUP_COUNT_LIMIT = "拥有的群聊数量达到上限: %d";
    public static final String MESSAGE_GROUP_MEMBER_COUNT_LIMIT = "群聊成员数量达到上限: %d";
    public static final String MESSAGE_MISSING_FILE = "缺少文件";
    public static final String MESSAGE_MISSING_OUTER_LINK = "缺少外链信息";
    public static final String MESSAGE_MISSING_GRAYSCALE_IDS = "缺少灰度用户列表信息";
    public static final String MESSAGE_MISSING_VERSION = "缺少版本信息";
    public static final String MESSAGE_FAILED_TO_SAVE_FILE = "文件保存失败";
    public static final String MESSAGE_FAILED_TO_CREATE_FOLDER = "创建目录失败";
    public static final String MESSAGE_FILE_NOT_EXIST = "文件不存在";
    public static final String MESSAGE_FILE_NAME_ERROR = "文件名格式错误";
    public static final String MESSAGE_FILE_SIZE_LIMIT = "文件过大";
    public static final String MESSAGE_GROUP_NOT_EXIST = "群聊不存在";
    public static final String MESSAGE_GROUP_ALREADY_DISBAND = "群聊已解散";
    public static final String MESSAGE_NOT_IN_THE_GROUP = "已不在此群聊";
    public static final String MESSAGE_NOT_THE_FRIEND = "与此用户已不是好友";
    public static final String MESSAGE_NOT_ADMIN = "非管理员账号操作";
    public static final String MESSAGE_NOT_GROUP_OWNER = "非群主账号操作";
    public static final String MESSAGE_BING_BLOCKED = "已被拉黑";
    public static final String MESSAGE_APP_ALREADY_RELEASED = "无法删除已发布的更新";
    public static final String MESSAGE_VERSION_TOO_LOW = "新增版本低于最新版本";
    public static final String MESSAGE_CANNOT_CHANGE_VERSION = "不允许修改版本";
    public static final String MESSAGE_CANNOT_REMOVE_OWNER = "不允许移除群主";
    public static final String MESSAGE_JSON_PARSE_ERROR = "json字符串解析失败";
    public static final String MESSAGE_NOT_THE_MESSAGE_SENDER = "非该聊天消息的发送者";

    // chat message
    public static final String MESSAGE_GROUP_CREATED = "群聊已创建，可以和好友一起畅聊了";
    public static final String MESSAGE_FRIEND_ADD = "一起聊天吧";
    public static final String MESSAGE_JOIN_GROUP = "%s 加入了群聊";
    public static final String MESSAGE_GROUP_DISBAND = "群聊已解散";
    public static final String MESSAGE_LEAVE_GROUP = "%s 退出了群聊";
    public static final String MESSAGE_REMOVED_FROM_GROUP = "%s 被移出了群聊";

    // validate failure message
    public static final String VALIDATE_EMPTY_CHECK_CODE = "验证码不能为空";
    public static final String VALIDATE_EMPTY_CHECK_CODE_KEY = "验证码唯一标识key不能为空";
    public static final String VALIDATE_EMPTY_EMAIL = "邮箱不能为空";
    public static final String VALIDATE_EMPTY_NICKNAME = "用户名不能为空";
    public static final String VALIDATE_EMPTY_PASSWORD = "密码不能为空";
    public static final String VALIDATE_EMPTY_CONTACT_ID = "联系人id不能为空";
    public static final String VALIDATE_EMPTY_CONTACT_NAME = "联系人名称不能为空";
    public static final String VALIDATE_EMPTY_USER_ID = "用户id不能为空";
    public static final String VALIDATE_EMPTY_GROUP_ID = "群聊id不能为空";
    public static final String VALIDATE_EMPTY_GROUP_NAME = "群聊名不能为空";
    public static final String VALIDATE_EMPTY_APPLY_ID = "申请id不能为空";
    public static final String VALIDATE_EMPTY_SESSION_ID = "会话id不能为空";
    public static final String VALIDATE_EMPTY_MESSAGE_ID = "消息id不能为空";
    public static final String VALIDATE_EMPTY_APP_UPDATE_ID = "应用更新id不能为空";
    public static final String VALIDATE_EMPTY_BEAUTY_USER_INFO_ID = "靓号信息id不能为空";
    public static final String VALIDATE_EMPTY_STATUS = "状态信息不能为空";
    public static final String VALIDATE_EMPTY_DESCRIPTION = "描述信息不能为空";
    public static final String VALIDATE_EMPTY_VERSION = "版本信息不能为空";
    public static final String VALIDATE_EMPTY_METHOD_TYPE = "更新方法不能为空";
    public static final String VALIDATE_EMPTY_MESSAGE_CONTENT = "消息内容不能为空";
    public static final String VALIDATE_EMPTY_MULTIPART_FILE = "文件不能为空";
    public static final String VALIDATE_EMPTY_FILE_NAME = "文件名不能为空";
    public static final String VALIDATE_EMPTY_OPERATE_TYPE = "操作类型不能为空";
    public static final String VALIDATE_MESSAGE_CONTENT_TOO_LONG = "消息内容过长";
    public static final String VALIDATE_ILLEGAL_GENDER = "性别信息有误";
    public static final String VALIDATE_ILLEGAL_NICK_NAME = "用户名信息格式有误";
    public static final String VALIDATE_ILLEGAL_EMAIL = "邮箱信息格式有误";
    public static final String VALIDATE_ILLEGAL_PASSWORD = "密码信息格式有误";
    public static final String VALIDATE_ILLEGAL_VERSION = "版本信息格式有误";

    // regex
    public static final String REGEX_NICK_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_-]{6,12}$";
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    public static final String REGEX_PASSWORD = "^(?![a-zA-Z]+$)(?!\\d+$)(?![^\\da-zA-Z\\s]+$).{8,}$";
    public static final String REGEX_MD5 = "^[a-fA-F0-9]{32}$";
    public static final String REGEX_VERSION = "^([1-9]\\d|[1-9])(.([1-9]\\d|\\d)){2}$";
    public static final String REGEX_NUMBER = "^[0-9]+$";

    // file
    public static final String FILE_FOLDER_AVATAR = "avatar";
    public static final String FILE_FOLDER_EXE = "app";
    public static final String FILE_FOLDER_CHAT = "chat";
    public static final String FILE_SUFFIX_AVATAR = ".png";
    public static final String FILE_SUFFIX_COVER = "_cover";
    public static final String FILE_SUFFIX_EXE = ".exe";
    public static final Long MB_TO_BYTE = 1024 * 1024L;
    public static final String[] IMAGE_SUFFIXES = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    public static final String[] VIDEO_SUFFIXES = {".mp4", ".avi", ".rmvb", ".mkv", ".mov"};

    // format
    public static final String FORMAT_DATE_yyyyMM = "yyyyMM";

    // other
    public static final String IN_SWITCH_DEFAULT = "进入default分支";
}
