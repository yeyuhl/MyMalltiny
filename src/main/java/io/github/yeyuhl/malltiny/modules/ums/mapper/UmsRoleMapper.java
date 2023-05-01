package io.github.yeyuhl.malltiny.modules.ums.mapper;

import io.github.yeyuhl.malltiny.modules.ums.model.UmsRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 后台用户角色表 Mapper 接口
 * </p>
 *
 * @author yeyuhl
 * @since 2023-04-24
 */
@Mapper
public interface UmsRoleMapper extends BaseMapper<UmsRole> {
    /**
     * 获取用户所有角色
     */
    List<UmsRole> getRoleList(@Param("adminId") Long adminId);

}
