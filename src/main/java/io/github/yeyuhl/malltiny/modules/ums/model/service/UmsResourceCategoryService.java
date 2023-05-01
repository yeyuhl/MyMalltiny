package io.github.yeyuhl.malltiny.modules.ums.model.service;

import io.github.yeyuhl.malltiny.modules.ums.model.UmsResourceCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 后台资源分类管理Service
 * </p>
 *
 * @author yeyuhl
 * @since 2023-04-27
 */
public interface UmsResourceCategoryService extends IService<UmsResourceCategory> {
    /**
     * 获取所有资源分类
     */
    List<UmsResourceCategory> listAll();

    /**
     * 创建资源分类
     */
    boolean create(UmsResourceCategory umsResourceCategory);
}
