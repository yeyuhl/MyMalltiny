package io.github.yeyuhl.malltiny.modules.ums.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.yeyuhl.malltiny.modules.ums.model.UmsAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 后台用户表 Mapper 接口
 * </p>
 *
 * @author yeyuhl
 * @since 2023-04-24
 */
@Mapper
public interface UmsAdminMapper extends BaseMapper<UmsAdmin> {
    /**
     * 获取资源相关用户ID列表
     */
    List<Long> getAdminIdList(@Param("resourceId") Long resourceId);

}
