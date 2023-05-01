package io.github.yeyuhl.malltiny.security.component;

import cn.hutool.core.collection.CollUtil;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Iterator;

/**
 * 动态权限决策管理器，用于判断用户是否有访问权限
 *
 * @author yeyuhl
 * @date 2023/4/28
 */
public class DynamicAccessDecisionManager implements AccessDecisionManager {
    /**
     * AccessDecisionManager是Spring Security中用于做出最终访问控制（授权）决定的接口。
     * decide方法则用于根据给定的身份验证信息、安全对象和配置属性来做出访问控制决定。
     *
     * @param authentication   表示当前用户的身份验证信息
     * @param object           安全对象，它表示需要进行访问控制的对象
     * @param configAttributes ConfigAttribute集合，它表示与安全对象相关联的配置属性。这些属性可以用来指定访问控制规则。
     * @throws AccessDeniedException               用户无权访问安全对象，抛出该异常
     * @throws InsufficientAuthenticationException 同样是权限不够无法访问抛出的异常
     */
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        // 当接口未被配置资源时直接放行
        if (CollUtil.isEmpty(configAttributes)) {
            return;
        }
        Iterator<ConfigAttribute> iterator = configAttributes.iterator();
        while (iterator.hasNext()) {
            ConfigAttribute configAttribute = iterator.next();
            // 将访问所需资源或用户拥有资源进行比对
            String needAuthority = configAttribute.getAttribute();
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (needAuthority.trim().equals(authority.getAuthority())) {
                    return;
                }
            }
        }
        throw new AccessDeniedException("抱歉，您没有访问权限");
    }

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
