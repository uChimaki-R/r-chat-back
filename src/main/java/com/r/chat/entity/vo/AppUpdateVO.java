package com.r.chat.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.r.chat.entity.enums.AppUpdateMethodTypeEnum;
import com.r.chat.entity.enums.AppUpdateStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppUpdateVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 更新描述
     */
    private String description;

    /**
     * 更新描述转换的列表
     */
    private List<String> descriptionList;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime releaseTime;

    /**
     * 发布状态 0：未发布 1：灰度发布 2：全网发布
     */
    private AppUpdateStatusEnum status;

    /**
     * 灰度发布的用户id（使用逗号分割）
     */
    private String grayscaleIds;

    /**
     * 灰度发布的用户id组成的列表
     */
    private List<String> grayscaleIdList;

    /**
     * 更新手段 0：文件 1：外链
     */
    private AppUpdateMethodTypeEnum methodType;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 外链地址
     */
    private String outerLink;
}
