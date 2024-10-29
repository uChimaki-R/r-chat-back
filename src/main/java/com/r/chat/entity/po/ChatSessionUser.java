package com.r.chat.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.github.jeffreyning.mybatisplus.anno.MppMultiId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 会话用户（多对多）
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_session_user")
public class ChatSessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @MppMultiId
    @TableField("user_id")
    private String userId;

    /**
     * 联系人id
     */
    @TableField("contact_id")
    private String contactId;

    /**
     * 会话id
     */
    @MppMultiId
    @TableField("session_id")
    private String sessionId;

    /**
     * 联系人名称
     */
    @TableField("contact_name")
    private String contactName;


}
