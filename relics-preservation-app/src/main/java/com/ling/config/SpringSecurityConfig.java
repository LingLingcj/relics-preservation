package com.ling.config;

import com.ling.types.jwt.JwtAuthenticationEntryPoint;
import com.ling.types.jwt.JwtAuthenticationFilter;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * @Author: LingRJ
 * @Description: Security配置
 * @DateTime: 2025/6/27 11:20
 **/
@Configuration
@Slf4j
public class SpringSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public static PasswordEncoder passwordEncoder(){
        log.debug("创建密码编码器");
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("配置安全过滤链");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests((authorize) -> {
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll(); // 允许所有OPTIONS请求，解决CORS预检问题
                    authorize.requestMatchers("/api/auth/**").permitAll();
                    authorize.requestMatchers("/api/alert/**").permitAll();
                    authorize.requestMatchers("/api/relics/**").permitAll();
                    authorize.requestMatchers("api/index/**").permitAll();
                    authorize.requestMatchers("/api/test/public").permitAll();
                    authorize.requestMatchers("/api/test/headers").permitAll();
                    authorize.requestMatchers("/api/knowledge/rag").permitAll();
                    authorize.requestMatchers("/api/relics/era/**").permitAll();
                    authorize.requestMatchers("/api/relics/search/**").permitAll();
                    authorize.requestMatchers("/api/sensor/analysis/**").permitAll();
                    authorize.requestMatchers("/api/sensor/**").permitAll();
                    authorize.requestMatchers("/api/sensor/data/**").permitAll();
                    authorize.requestMatchers("/api/knowledge/rag/**").permitAll();
                    authorize.requestMatchers("/api/knowledge/rag").permitAll();
                    // WebSocket端点
                    authorize.requestMatchers("/ws/**").permitAll();
                    // Swagger UI 相关路径
                    authorize.requestMatchers("/swagger-ui.html").permitAll();
                    authorize.requestMatchers("/swagger-ui/**").permitAll();
                    authorize.requestMatchers("/v3/api-docs/**").permitAll();
                    authorize.requestMatchers("/api-docs/**").permitAll();
                    authorize.requestMatchers("/doc.html").permitAll();
                    authorize.requestMatchers("/webjars/**").permitAll();

                    // 评论审核相关权限控制 - 只有专家和管理员可以访问
                    authorize.requestMatchers("/api/v1/admin/comments/**").hasAnyRole("EXPERT", "ADMIN");
                    authorize.requestMatchers("/api/admin/comments/**").hasAnyRole("EXPERT", "ADMIN");

                    // 用户交互功能 - 需要认证
                    authorize.requestMatchers("/api/interaction/**").authenticated();
                    authorize.requestMatchers("/api/favorite/**").authenticated();
                    authorize.requestMatchers("/api/comment/**").authenticated();

                    // 文物上传 - 只有专家可以访问
                    authorize.requestMatchers("/api/relics/upload").hasRole("EXPERT");

                    // 测试接口权限
                    authorize.requestMatchers("/api/test/protected").authenticated();
                    authorize.requestMatchers("/api/test/expert").hasRole("EXPERT");
                    authorize.requestMatchers("/api/test/admin").hasRole("ADMIN");

                    authorize.anyRequest().authenticated();
                    log.debug("已配置请求授权规则");
                })
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(authenticationEntryPoint);
                    log.debug("已配置异常处理");
                })
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    log.debug("已配置会话管理策略为STATELESS");
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        ;
        
        log.debug("安全过滤链配置完成");        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        log.debug("创建认证管理器");
        return configuration.getAuthenticationManager();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        // 允许所有来源
        configuration.addAllowedOriginPattern("*");
        // 允许所有方法
        configuration.addAllowedMethod("*");
        // 允许所有头部
        configuration.addAllowedHeader("*");
        // 允许携带凭证
        configuration.setAllowCredentials(true);

        // 预检请求缓存时间
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        log.debug("配置CORS");
        return source;
    }
}
