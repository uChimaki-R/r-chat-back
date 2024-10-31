package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum NoticeTypeEnum {
    WS_INIT(0, "初始化ws连接"),

    USER_ADD_ACCEPT(1, "同意了对方的好友申请，成为了好友"),
    USER_ADD_BY_OTHERS(2, "对方同意了自己的申请，成为了好友"),

    GROUP_CREATED(3, "群聊创建成功"),
    GROUP_ADD_ACCEPTED(4, "群聊加入申请被通过"),
//    GROUP_USER_LEAVE(6, "退出群聊"),
//    GROUP_USER_IS_REMOVED(7, "被群主移出了群聊"),
//    GROUP_DISSOLUTION(8, "解散群聊"),

    CONTACT_RENAME(8, "更新联系人名称"),  // 可以是群聊名称修改了，也可以是用户名称修改了，都要通知
    CONTACT_APPLY(9, "好友申请"),

    //    FILE_UPLOAD_COMPLETED(10, "", "文件上传完毕"),

    FORCE_OFF_LINE(11, "强制下线");

    @EnumValue
    private final Integer value;
    private final String desc;

    NoticeTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
