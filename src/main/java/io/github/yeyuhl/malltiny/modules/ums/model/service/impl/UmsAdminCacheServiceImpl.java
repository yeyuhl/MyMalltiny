package io.github.yeyuhl.malltiny.modules.ums.model.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.yeyuhl.malltiny.common.service.RedisService;
import io.github.yeyuhl.malltiny.modules.ums.mapper.UmsAdminMapper;
import io.github.yeyuhl.malltiny.modules.ums.model.UmsAdmin;
import io.github.yeyuhl.malltiny.modules.ums.model.UmsAdminRoleRelation;
import io.github.yeyuhl.malltiny.modules.ums.model.UmsResource;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsAdminCacheService;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsAdminRoleRelationService;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台用户缓存管理Service实现类
 *
 * @author yeyuhl
 * @date 2023/4/26
 */
@Service
public class UmsAdminCacheServiceImpl implements UmsAdminCacheService {
    @Autowired
    private UmsAdminService adminService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UmsAdminMapper adminMapper;
    @Autowired
    private UmsAdminRoleRelationService adminRoleRelationService;
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    @Value("${redis.expire.common}")
    private Long REDIS_EXPIRE;
    @Value("${redis.key.admin}")
    private String REDIS_KEY_ADMIN;
    @Value("${redis.key.resourceList}")
    private String REDIS_KEY_RESOURCE_LIST;

    @Override
    public void delAdmin(Long adminId) {
        // 之所以可以用getById是因为mybatis-plus自动生成时让UmsAdminService继承了UmsAdmin
        UmsAdmin admin = adminService.getById(adminId);
        if (admin != null) {
            String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + admin.getUsername();
            redisService.del(key);
        }
    }

    @Override
    public void delResourceList(Long adminId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + adminId;
        redisService.del(key);
    }


    @Override
    public void delResourceListByRole(Long roleId) {
        // QueryWrapper是mybatis plus中实现查询的对象封装操作类
        QueryWrapper<UmsAdminRoleRelation> queryWrapper = new QueryWrapper<>();
        // 查询UmsAdminRoleRelation中roleId等于传入的参数roleId的UmsAdminRoleRelation
        queryWrapper.lambda().eq(UmsAdminRoleRelation::getRoleId, roleId);
        List<UmsAdminRoleRelation> relationList = adminRoleRelationService.list(queryWrapper);
        if (CollUtil.isEmpty(relationList)) {
            String keyPrefix = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":";
            // List.stream().map().collect()的用处是将一个List里的元素应用map里的函数后保存到另一个List中
            List<String> keys = relationList.stream().map(relation -> keyPrefix + relation.getAdminId()).collect(Collectors.toList());
            redisService.del(keys);
        }
    }

    @Override
    public void delResourceListByRoleIds(List<Long> roleIds) {
        QueryWrapper<UmsAdminRoleRelation> queryWrapper = new QueryWrapper<>();
        // in相当于sql语句中的in字段，可以在where子句中规定多个值，WHERE value IN (value1, value2, ...)
        queryWrapper.lambda().in(UmsAdminRoleRelation::getRoleId, roleIds);
        List<UmsAdminRoleRelation> relationList = adminRoleRelationService.list(queryWrapper);
        if (CollUtil.isNotEmpty(relationList)) {
            String keyPrefix = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":";
            List<String> keys = relationList.stream().map(relation -> keyPrefix + relation.getAdminId()).collect(Collectors.toList());
            redisService.del(keys);
        }
    }

    @Override
    public void delResourceListByResource(Long resourceId) {
        List<Long> adminIdList = adminMapper.getAdminIdList(resourceId);
        if (CollUtil.isNotEmpty(adminIdList)) {
            String keyPrefix = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":";
            List<String> keys = adminIdList.stream().map(adminId -> keyPrefix + adminId).collect(Collectors.toList());
            redisService.del(keys);
        }
    }

    @Override
    public UmsAdmin getAdmin(String username) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + username;
        return (UmsAdmin) redisService.get(key);
    }

    @Override
    public void setAdmin(UmsAdmin admin) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + admin.getUsername();
        redisService.set(key, admin, REDIS_EXPIRE);
    }

    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + adminId;
        return (List<UmsResource>) redisService.get(key);
    }

    @Override
    public void setResourceList(Long adminId, List<UmsResource> resourceList) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + adminId;
        redisService.set(key, resourceList, REDIS_EXPIRE);
    }
}
