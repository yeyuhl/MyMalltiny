package io.github.yeyuhl.malltiny.security.component;

import cn.hutool.json.JSONUtil;
import io.github.yeyuhl.malltiny.common.response.CommonResult;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义返回结果：未登录或者登录过期
 * AuthenticationEntryPoint是一个接口，它被ExceptionTranslationFilter用来作为认证方案的入口
 * 当用户请求处理过程中遇见认证异常时，它被异常处理器（ExceptionTranslationFilter）用来开启特定的认证流程
 *
 * @author yeyuhl
 * @date 2023/4/28
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * AuthenticationEntryPoint中唯一的方法，用来开启认证方案
     *
     * @param request       认证异常的用户请求
     * @param response      返回给用户的响应
     * @param authException 请求过程中的认证异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().println(JSONUtil.parse(CommonResult.unauthorized(authException.getMessage())));
        response.getWriter().flush();
    }
}
