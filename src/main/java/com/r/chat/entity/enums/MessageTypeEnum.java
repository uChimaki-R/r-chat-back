package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    WS_INIT(0, "", "连接ws"),
    CHAT(1, "", "普通聊天消息"),
    USER_ADD(2, "", "添加好友打招呼"),
    USER_LEAVE_GROUP(3, "%s退出了群聊", "退出群聊"),
    USER_IS_REMOVED_GROUP(4, "%s被群主移出了群聊", "被群主移出了群聊"),
    GROUP_CREATE(5, "群聊已经创建好，可以和好友一起畅聊了", "群创建成功"),
    GROUP_ADD(6, "%s加入了群聊", "加入群聊"),
    GROUP_RENAME(7, "", "更新群聊名"),
    GROUP_DISSOLUTION(8, "群聊已解散", "解散群聊"),
    CONTACT_APPLY(9, "", "好友申请"),
    FILE_UPLOAD(10, "", "媒体文件"),
    FILE_UPLOAD_COMPLETED(11, "", "文件上传完毕"),
    FORCE_OFF_LINE(12, "", "强制下线");

    @EnumValue
    private final Integer value;
    private final String message;
    private final String desc;

    MessageTypeEnum(Integer value, String message, String desc) {
        this.value = value;
        this.message = message;
        this.desc = desc;
    }
}
