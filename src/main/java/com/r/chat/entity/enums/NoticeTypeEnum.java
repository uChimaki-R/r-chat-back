package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum NoticeTypeEnum {
    WS_INIT(0, "", "初始化ws连接"),

    USER_ADD_ACCEPT(1, "", "同意了对方的好友申请，成为了好友"),
    USER_ADD_BY_OTHERS(2, "", "对方同意了自己的申请，成为了好友"),

    CONTACT_APPLY(3, "", "好友申请"),

    //    USER_LEAVE_GROUP(4, "%s退出了群聊", "退出群聊"),
//    USER_IS_REMOVED_FROM_GROUP(5, "%s被群主移出了群聊", "被群主移出了群聊"),
//    GROUP_CREATE(6, "群聊已经创建好，可以和好友一起畅聊了", "群创建成功"),
//    GROUP_ADD(7, "%s加入了群聊", "加入群聊"),
//    GROUP_RENAME(8, "", "更新群聊名"),
//    GROUP_DISSOLUTION(9, "群聊已解散", "解散群聊"),
//    FILE_UPLOAD_COMPLETED(12, "", "文件上传完毕"),
    FORCE_OFF_LINE(13, "", "强制下线");

    @EnumValue
    private final Integer value;
    private final String message;
    private final String desc;

    NoticeTypeEnum(Integer value, String message, String desc) {
        this.value = value;
        this.message = message;
        this.desc = desc;
    }
}
