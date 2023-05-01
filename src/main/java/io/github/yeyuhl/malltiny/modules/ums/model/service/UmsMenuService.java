package io.github.yeyuhl.malltiny.modules.ums.model.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yeyuhl.malltiny.modules.ums.dto.UmsMenuNode;
import io.github.yeyuhl.malltiny.modules.ums.model.UmsMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 后台菜单管理Service
 * </p>
 *
 * @author yeyuhl
 * @since 2023-04-27
 */
public interface UmsMenuService extends IService<UmsMenu> {
    /**
     * 创建后台菜单
     */
    boolean create(UmsMenu umsMenu);

    /**
     * 修改后台菜单
     */
    boolean update(Long id,UmsMenu umsMenu);

    /**
     * 分页查询后台菜单
     */
    Page<UmsMenu> list(Long parentId, Integer pageSize, Integer pageNum);

    /**
     * 树形结构返回所有菜单列表
     */
    List<UmsMenuNode> treeList();

    /**
     * 修改菜单显示状态
     */
    boolean updateHidden(Long id,Integer hidden);
}
