package io.github.yeyuhl.malltiny.modules.ums.model.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yeyuhl.malltiny.modules.ums.mapper.UmsMenuMapper;
import io.github.yeyuhl.malltiny.modules.ums.mapper.UmsResourceMapper;
import io.github.yeyuhl.malltiny.modules.ums.model.*;
import io.github.yeyuhl.malltiny.modules.ums.mapper.UmsRoleMapper;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsAdminCacheService;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsRoleMenuRelationService;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsRoleResourceRelationService;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 后台角色管理Service实现类
 * </p>
 *
 * @author yeyuhl
 * @since 2023-04-30
 */
@Service
public class UmsRoleServiceImpl extends ServiceImpl<UmsRoleMapper, UmsRole> implements UmsRoleService {
    @Autowired
    private UmsAdminCacheService umsAdminCacheService;
    @Autowired
    private UmsRoleMenuRelationService umsAdminRoleMenuRelationService;
    @Autowired
    private UmsRoleResourceRelationService umsRoleResourceRelationService;
    @Autowired
    private UmsMenuMapper umsMenuMapper;
    @Autowired
    private UmsResourceMapper umsResourceMapper;

    @Override
    public boolean create(UmsRole role) {
        role.setCreateTime(new Date());
        role.setAdminCount(0);
        role.setSort(0);
        return save(role);
    }

    @Override
    public boolean delete(List<Long> ids) {
        boolean success = removeByIds(ids);
        umsAdminCacheService.delResourceListByRoleIds(ids);
        return success;
    }

    @Override
    public Page<UmsRole> list(String keyWord, Integer pageSize, Integer pageNum) {
        Page<UmsRole> page = new Page<>(pageNum, pageSize);
        QueryWrapper<UmsRole> wrapper = new QueryWrapper<>();
        LambdaQueryWrapper<UmsRole> lambda = wrapper.lambda();
        if (StrUtil.isNotEmpty(keyWord)) {
            lambda.like(UmsRole::getName, keyWord);
        }
        return page(page, wrapper);
    }

    @Override
    public List<UmsMenu> getMenuList(long adminId) {
        return umsMenuMapper.getMenuList(adminId);
    }

    @Override
    public List<UmsMenu> listMenu(Long roleId) {
        return umsMenuMapper.getMenuListByRoleId(roleId);
    }

    @Override
    public List<UmsResource> listResource(Long roleId) {
        return umsResourceMapper.getResourceListByRoleId(roleId);
    }

    @Override
    public int allocMenu(Long roleId, List<Long> menuIds) {
        // 先删除原有关系
        QueryWrapper<UmsRoleMenuRelation> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsRoleMenuRelation::getRoleId, roleId);
        umsAdminRoleMenuRelationService.remove(wrapper);
        // 批量插入新关系
        List<UmsRoleMenuRelation> relationList = new ArrayList<>();
        for (Long menuId : menuIds) {
            UmsRoleMenuRelation relation = new UmsRoleMenuRelation();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            relationList.add(relation);
        }
        umsAdminRoleMenuRelationService.saveBatch(relationList);
        return menuIds.size();
    }

    @Override
    public int allocResource(Long roleId, List<Long> resourceIds) {
        //先删除原有关系
        QueryWrapper<UmsRoleResourceRelation> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsRoleResourceRelation::getRoleId, roleId);
        umsRoleResourceRelationService.remove(wrapper);
        //批量插入新关系
        List<UmsRoleResourceRelation> relationList = new ArrayList<>();
        for (Long resourceId : resourceIds) {
            UmsRoleResourceRelation relation = new UmsRoleResourceRelation();
            relation.setRoleId(roleId);
            relation.setResourceId(resourceId);
            relationList.add(relation);
        }
        umsRoleResourceRelationService.saveBatch(relationList);
        umsAdminCacheService.delResourceListByRole(roleId);
        return resourceIds.size();
    }


}
