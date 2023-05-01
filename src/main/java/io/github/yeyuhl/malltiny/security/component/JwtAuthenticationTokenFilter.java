package io.github.yeyuhl.malltiny.security.component;

import io.github.yeyuhl.malltiny.security.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT登录授权过滤器
 * OncePerRequestFilter是Spring中的一个特殊类型的过滤器，它确保对于给定的请求，该过滤器只执行一次
 * 这是通过在请求属性中存储一个标记来实现的，该标记指示该过滤器是否已经执行过。如果该过滤器已经执行过，则不会再次执行
 *
 * @author yeyuhl
 * @date 2023/4/28
 */
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(this.tokenHeader);
        if (authHeader != null && authHeader.startsWith(this.tokenHead)) {
            // 获取"Bearer "后面的部分
            String authToken = authHeader.substring(this.tokenHead.length());
            // 获取用户名
            String username = jwtTokenUtil.getUserNameFromToken(authToken);
            LOGGER.info("checking username:{}", username);
            // 查询用户权限，如果该用户在SecurityContextHolder中无法获取其验证信息，那么对其验证（如果有说明验证过了）
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                // 查看authToken是否有效
                if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                    // 创建UsernamePasswordAuthenticationToken对象，使用用户详细信息、空密码和用户权限集来初始化它
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    // 构建验证的详细信息
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    LOGGER.info("authenticated user:{}", username);
                    // 最后将验证信息存到SecurityContextHolder中
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        // 无论token是否有效，都传递给下一个过滤器或目标资源
        filterChain.doFilter(request, response);
    }
}
