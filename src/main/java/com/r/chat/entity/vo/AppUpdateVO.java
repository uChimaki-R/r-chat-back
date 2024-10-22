package com.r.chat.entity.vo;

import com.r.chat.entity.enums.AppUpdateMethodTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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
     * 更新描述转换的列表
     */
    private List<String> updateList;

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
