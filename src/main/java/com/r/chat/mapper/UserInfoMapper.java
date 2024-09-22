package com.r.chat.mapper;

import com.r.chat.entity.po.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户信息 Mapper 接口
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-21
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

}
