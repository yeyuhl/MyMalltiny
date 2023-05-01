package io.github.yeyuhl.malltiny.modules.ums.model.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yeyuhl.malltiny.domain.AdminUserDetails;
import io.github.yeyuhl.malltiny.common.exception.Asserts;
import io.github.yeyuhl.malltiny.modules.ums.dto.UmsAdminParam;
import io.github.yeyuhl.malltiny.modules.ums.dto.UpdateAdminPasswordParam;
import io.github.yeyuhl.malltiny.modules.ums.mapper.UmsAdminLoginLogMapper;
import io.github.yeyuhl.malltiny.modules.ums.mapper.UmsResourceMapper;
import io.github.yeyuhl.malltiny.modules.ums.mapper.UmsRoleMapper;
import io.github.yeyuhl.malltiny.modules.ums.model.*;
import io.github.yeyuhl.malltiny.modules.ums.mapper.UmsAdminMapper;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsAdminCacheService;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsAdminRoleRelationService;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.yeyuhl.malltiny.security.util.JwtTokenUtil;
import io.github.yeyuhl.malltiny.security.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 后台管理员管理Service实现类
 * </p>
 *
 * @author yeyuhl
 * @since 2023-04-27
 */
@Service
public class UmsAdminServiceImpl extends ServiceImpl<UmsAdminMapper, UmsAdmin> implements UmsAdminService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsAdminServiceImpl.class);
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UmsAdminLoginLogMapper loginLogMapper;
    @Autowired
    private UmsAdminRoleRelationService adminRoleRelationService;
    @Autowired
    private UmsRoleMapper roleMapper;
    @Autowired
    private UmsResourceMapper resourceMapper;

    @Override
    public UmsAdmin getAdminByUsername(String username) {
        UmsAdmin admin = getCacheService().getAdmin(username);
        // 如果缓存有就直接从缓存中获取
        if (admin != null) return admin;
        // 缓存没有就从数据库中取
        QueryWrapper<UmsAdmin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UmsAdmin::getUsername, username);
        List<UmsAdmin> adminList = list(queryWrapper);
        if (adminList != null && adminList.size() > 0) {
            admin = adminList.get(0);
            // 如果数据库中确实存在该Admin，那么查询完后放入缓存中
            getCacheService().setAdmin(admin);
            return admin;
        }
        return null;
    }

    @Override
    public UmsAdmin register(UmsAdminParam umsAdminParam) {
        UmsAdmin umsAdmin = new UmsAdmin();
        BeanUtils.copyProperties(umsAdminParam, umsAdmin);
        umsAdmin.setCreateTime(new Date());
        umsAdmin.setStatus(1);
        // 查询是否有相同用户名的用户
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdmin::getUsername, umsAdmin.getUsername());
        List<UmsAdmin> umsAdminList = list(wrapper);
        // 如果用户名存在重复则返回null
        if (umsAdminList.size() > 0) {
            return null;
        }
        // 如果无重复用户则对密码进行加密操作，然后将注册好的用户存进数据库
        String encodePassword = passwordEncoder.encode(umsAdmin.getPassword());
        umsAdmin.setPassword(encodePassword);
        // 使用baseMapper会将数据存到对应实体类的表中
        baseMapper.insert(umsAdmin);
        return umsAdmin;
    }

    @Override
    public String login(String username, String password) {
        String token = null;
        // 密码需要客户端加密后传递
        try {
            UserDetails userDetails = loadUserByUsername(username);
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                Asserts.fail("密码不正确");
            }
            if (!userDetails.isEnabled()) {
                Asserts.fail("帐号已被禁用");
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
            insertLoginLog(username);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;
    }

    /**
     * 添加登录记录
     */
    private void insertLoginLog(String username) {
        UmsAdmin admin = getAdminByUsername(username);
        // 不存在该admin则直接返回
        if (admin == null) return;
        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
        loginLog.setAdminId(admin.getId());
        loginLog.setCreateTime(new Date());
        // 获取当前请求的HttpServletRequest对象，并从中获取客户端的IP地址
        // 它使用setIp方法将IP地址设置为登录记录的一部分
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        loginLog.setIp(request.getRemoteAddr());
        loginLogMapper.insert(loginLog);
    }

    /**
     * 根据用户修改登录时间
     */
    private void updateLoginTimeByUsername(String username) {
        UmsAdmin record = new UmsAdmin();
        record.setLoginTime(new Date());
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdmin::getUsername, username);
        update(record, wrapper);
    }

    @Override
    public String refreshToken(String oldToken) {
        return jwtTokenUtil.refreshHeadToken(oldToken);
    }

    @Override
    public Page<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum) {
        Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
        QueryWrapper<UmsAdmin> queryWrapper = new QueryWrapper<>();
        // LambdaQueryWrapper对象允许我们使用lambda表达式来定义查询条件
        LambdaQueryWrapper<UmsAdmin> lambdaQueryWrapper = queryWrapper.lambda();
        if (StrUtil.isNotEmpty(keyword)) {
            // 使用like方法在username字段上添加一个模糊查询条件
            // 并使用or和like方法在nickName字段上添加另一个模糊查询条件
            // or方法用于在查询条件中添加一个or连接符，表示接下来的查询条件与前面的查询条件之间是or关系
            lambdaQueryWrapper.like(UmsAdmin::getUsername, keyword);
            lambdaQueryWrapper.or().like(UmsAdmin::getNickName, keyword);
        }
        // 调用page方法执行分页查询，并返回查询结果
        return page(page, queryWrapper);
    }

    @Override
    public boolean update(Long id, UmsAdmin admin) {
        admin.setId(id);
        UmsAdmin rawAdmin = getById(id);
        if (rawAdmin.getPassword().equals(admin.getPassword())) {
            // 与原加密密码相同的不需要修改
            admin.setPassword(null);
        } else {
            // 与原加密密码不同的需要加密修改
            if (StrUtil.isEmpty(admin.getPassword())) {
                admin.setPassword(null);
            } else {
                admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            }
        }
        boolean success = updateById(admin);
        // 删除缓存里的admin，保持一致性
        getCacheService().delAdmin(id);
        return success;
    }

    @Override
    public boolean delete(Long id) {
        getCacheService().delAdmin(id);
        boolean success = removeById(id);
        getCacheService().delResourceList(id);
        return success;
    }

    @Override
    public int updateRole(Long adminId, List<Long> roleIds) {
        int count = roleIds == null ? 0 : roleIds.size();
        // 先删除原来的关系
        QueryWrapper<UmsAdminRoleRelation> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdminRoleRelation::getAdminId, adminId);
        adminRoleRelationService.remove(wrapper);
        // 建立新关系
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<UmsAdminRoleRelation> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                UmsAdminRoleRelation relation = new UmsAdminRoleRelation();
                relation.setAdminId(adminId);
                relation.setRoleId(roleId);
                list.add(relation);
            }
            adminRoleRelationService.saveBatch(list);
        }
        getCacheService().delResourceList(adminId);
        return count;
    }

    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        return roleMapper.getRoleList(adminId);
    }

    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        List<UmsResource> resourceList = getCacheService().getResourceList(adminId);
        if (CollUtil.isNotEmpty(resourceList)) {
            return resourceList;
        }
        // 如果缓存没有
        resourceList = resourceMapper.getResourceList(adminId);
        if (CollUtil.isNotEmpty(resourceList)) {
            // 更新缓存
            getCacheService().setResourceList(adminId, resourceList);
        }
        return resourceList;
    }

    /**
     * 更新密码，如果密码为空返回-1，如果要更新的用户为空返回-2，如果输入的旧密码和当前用户的密码不同（即改密码要求你输入自己当前密码和之前密码做比较），则返回-3
     * 成功更新返回1
     */
    @Override
    public int updatePassword(UpdateAdminPasswordParam updatePasswordParam) {
        if (StrUtil.isEmpty(updatePasswordParam.getUsername())
                || StrUtil.isEmpty(updatePasswordParam.getOldPassword())
                || StrUtil.isEmpty(updatePasswordParam.getNewPassword())) {
            return -1;
        }
        QueryWrapper<UmsAdmin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UmsAdmin::getUsername, updatePasswordParam.getUsername());
        List<UmsAdmin> adminList = list(queryWrapper);
        if (CollUtil.isEmpty(adminList)) {
            return -2;
        }
        UmsAdmin umsAdmin = adminList.get(0);
        if (!passwordEncoder.matches(updatePasswordParam.getOldPassword(), umsAdmin.getPassword())) {
            return -3;
        }
        umsAdmin.setPassword(passwordEncoder.encode(updatePasswordParam.getNewPassword()));
        updateById(umsAdmin);
        // 删除缓存
        getCacheService().delAdmin(umsAdmin.getId());
        return 1;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        //获取用户信息
        UmsAdmin admin = getAdminByUsername(username);
        if (admin != null) {
            List<UmsResource> resourceList = getResourceList(admin.getId());
            return new AdminUserDetails(admin, resourceList);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }

    @Override
    public UmsAdminCacheService getCacheService() {
        return SpringUtil.getBean(UmsAdminCacheService.class);
    }
}