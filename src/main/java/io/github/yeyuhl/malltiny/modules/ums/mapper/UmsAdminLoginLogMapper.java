package io.github.yeyuhl.malltiny.modules.ums.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.yeyuhl.malltiny.modules.ums.model.UmsAdminLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 后台用户登录日志表 Mapper 接口
 * </p>
 *
 * @author yeyuhl
 * @since 2023-04-24
 */
@Mapper
public interface UmsAdminLoginLogMapper extends BaseMapper<UmsAdminLoginLog> {

}
