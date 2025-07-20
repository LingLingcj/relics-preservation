package com.ling.types.jwt;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @Author: LingRJ
 * @Description: 拦截器和验证器
 * @DateTime: 2025/6/27 10:19
 **/

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;

    @Autowired
    private ApplicationContext applicationContext;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService){
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        log.debug("JWT过滤器处理请求: {}", request.getRequestURI());

        if (StringUtils.isNotBlank(token) && isValidAccessToken(token)){
            try {
                // 从token中获取名称
                String username = tokenProvider.getUsername(token);
                log.debug("JWT令牌中的用户名: {}", username);

                // 获取用户有关信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.debug("成功加载用户详细信息: {}", username);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("已设置认证信息到SecurityContextHolder");
            } catch (Exception e) {
                log.error("无法加载用户详细信息: {}", e.getMessage());
                // 清理可能的部分认证状态
                SecurityContextHolder.clearContext();
            }
        } else if (token != null) {
            log.debug("JWT令牌验证失败");
        } else {
            log.debug("未找到JWT令牌");
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        log.debug("Authorization头存在: {}", bearerToken != null);

        if(StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * 验证是否为有效的Access Token
     * 支持双Token模式和向后兼容
     */
    private boolean isValidAccessToken(String token) {
        // 首先验证token的基本有效性和版本
        if (!tokenProvider.validateTokenWithVersion(token)) {
            return false;
        }

        // 如果启用了双Token模式，需要验证token类型
        if (tokenProvider.isDualTokenEnabled()) {
            return tokenProvider.validateAccessToken(token);
        }

        // 向后兼容：如果未启用双Token模式，任何有效token都可以用于访问
        return true;
    }
}
