package com.r.chat.entity.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 当前页数
     */
    private Long pageNo;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 总页数
     */
    private Long pageTotal;

    /**
     * 数据总数
     */
    private Long totalCount;

    /**
     * 数据
     */
    private List<T> data;
}
