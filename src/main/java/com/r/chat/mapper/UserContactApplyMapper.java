package com.r.chat.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.po.UserContactApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.r.chat.entity.vo.ContactApplyVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 联系人申请 Mapper 接口
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Mapper
public interface UserContactApplyMapper extends BaseMapper<UserContactApply> {

    /**
     * 根据用户id分页查询该用户收到的申请信息
     */
    Page<ContactApplyVO> selectApplyInfoPage(Page<ContactApplyVO> page, String userId);
}
