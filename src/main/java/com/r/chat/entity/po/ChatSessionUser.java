package com.r.chat.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
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
    @TableId(value = "user_id", type = IdType.AUTO)
    private String userId;

    /**
     * 联系人id
     */
    @TableField("contact_id")
    private String contactId;

    /**
     * 会话id
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 联系人名称
     */
    @TableField("contact_name")
    private String contactName;


}
