package io.github.yeyuhl.malltiny.config;

import io.github.yeyuhl.malltiny.modules.ums.model.UmsResource;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsAdminService;
import io.github.yeyuhl.malltiny.modules.ums.model.service.UmsResourceService;
import io.github.yeyuhl.malltiny.security.component.DynamicSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mall-security模块相关配置
 * 自定义配置，用于配置如何获取用户信息及动态权限
 *
 * @author yeyuhl
 * @date 2023/4/30
 */
@Configuration
public class MallSecurityConfig {
    @Autowired
    private UmsAdminService adminService;
    @Autowired
    private UmsResourceService resourceService;

    @Bean
    public UserDetailsService userDetailsService() {
        //获取登录用户信息
        return username -> adminService.loadUserByUsername(username);
    }

    @Bean
    public DynamicSecurityService dynamicSecurityService() {
        return () -> {
            // ConcurrentHashMap是一个线程安全的哈希表，它允许多个线程同时对其进行读写操作
            // 它通过使用锁分段技术来实现高并发性能，即将哈希表分成多个段，每个段都有自己的锁
            // 这样，在进行写操作时，只需要锁定特定的段，而不是整个哈希表
            Map<String, ConfigAttribute> map = new ConcurrentHashMap<>();
            List<UmsResource> resourceList = resourceService.list();
            for (UmsResource resource : resourceList) {
                map.put(resource.getUrl(), new org.springframework.security.access.SecurityConfig(resource.getId() + ":" + resource.getName()));
            }
            return map;
        };
    }

}
