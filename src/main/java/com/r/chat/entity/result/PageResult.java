package com.r.chat.entity.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.utils.CopyUtils;
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

    /**
     * 将mybatis-plus的Page对象转为PageResult对象
     */
    public static <E> PageResult<E> fromPage(Page<E> page) {
        PageResult<E> pageResult = new PageResult<>();
        pageResult.setPageNo(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotalCount(page.getTotal());
        pageResult.setPageTotal(page.getPages());
        pageResult.setData(page.getRecords());
        return pageResult;
    }

    /**
     * 将mybatis-plus的Page对象转为PageResult对象，并将其中的每个数据转换为T类
     */
    public static <E, T> PageResult<T> fromPage(Page<E> page, Class<T> clazz) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setPageNo(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotalCount(page.getTotal());
        pageResult.setPageTotal(page.getPages());
        List<T> ts = CopyUtils.copyList(page.getRecords(), clazz);
        pageResult.setData(ts);
        return pageResult;
    }
}
