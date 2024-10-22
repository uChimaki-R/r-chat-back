package com.r.chat.entity.po;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.r.chat.entity.enums.AppUpdateMethodTypeEnum;
import com.r.chat.entity.enums.AppUpdateStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * app更新信息
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("app_update")
public class AppUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 版本号
     */
    @TableField("version")
    private String version;

    /**
     * 更新描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 发布状态 0：未发布 1：灰度发布 2：全网发布
     */
    @TableField("status")
    private AppUpdateStatusEnum status;

    /**
     * 灰度发布的用户id
     */
    @TableField(value = "grayscale_ids", updateStrategy = FieldStrategy.IGNORED)  // null也可以更新覆盖，灰度发布改全网发布的时候会置为null
    private String grayscaleIds;

    /**
     * 更新手段 0：文件 1：外链
     */
    @TableField("method_type")
    private AppUpdateMethodTypeEnum methodType;

    /**
     * 外链地址
     */
    @TableField(value = "outer_link", updateStrategy = FieldStrategy.IGNORED)  // null也可以更新覆盖，外链发布改文件发布的时候会置为null
    private String outerLink;

}
