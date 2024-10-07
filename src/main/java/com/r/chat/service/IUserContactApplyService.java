package com.r.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.po.UserContactApply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.r.chat.entity.vo.ContactApplyVO;

/**
 * <p>
 * 联系人申请 服务类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
public interface IUserContactApplyService extends IService<UserContactApply> {

    /**
     * 分页查询当前用户接收到的申请信息
     * 因为需要查出申请人/群聊名称，需要多表联查
     */
    Page<ContactApplyVO> getApplyInfoPage(Page<ContactApplyVO> page);
}
