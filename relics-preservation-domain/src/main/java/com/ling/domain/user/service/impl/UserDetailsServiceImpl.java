package com.ling.domain.user.service.impl;

import com.ling.domain.user.adapter.IUserRepository;
import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.Username;
import com.ling.domain.user.model.valobj.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * 新的 UserDetailsService 实现
 * @Author: LingRJ
 * @Description: 基于新的 User 聚合根实现 Spring Security 的 UserDetailsService
 * @DateTime: 2025/7/11
 */
@Service("newUserDetailsService")
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private IUserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        try {
            User user = findUserByUsernameOrEmail(usernameOrEmail);
            
            if (user == null) {
                log.warn("用户不存在: {}", usernameOrEmail);
                throw new UsernameNotFoundException("用户不存在: " + usernameOrEmail);
            }
            
            if (!user.isEnabled()) {
                log.warn("用户已被禁用: {}", usernameOrEmail);
                throw new UsernameNotFoundException("用户已被禁用: " + usernameOrEmail);
            }
            
            // 构建权限
            Set<GrantedAuthority> authorities = Set.of(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().getCode())
            );
            
            log.debug("成功加载用户: {}, 角色: {}", user.getUsername().getValue(), user.getRole().getCode());
            
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername().getValue(),
                    user.getPassword().getEncodedValue(),
                    user.isEnabled(),
                    true, // accountNonExpired
                    true, // credentialsNonExpired
                    !user.getStatus().isLocked(), // accountNonLocked
                    authorities
            );
            
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载用户详情时发生异常: {} - {}", usernameOrEmail, e.getMessage(), e);
            throw new UsernameNotFoundException("加载用户详情失败: " + usernameOrEmail, e);
        }
    }
    
    /**
     * 根据用户名或邮箱查找用户
     * @param usernameOrEmail 用户名或邮箱
     * @return 用户实体
     */
    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = usernameOrEmail.trim();
        
        // 首先尝试作为用户名查找
        if (Username.isValid(trimmed)) {
            Optional<User> userOpt = userRepository.findByUsername(Username.of(trimmed));
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }
        
        // 然后尝试作为邮箱查找
        if (Email.isValid(trimmed)) {
            Optional<User> userOpt = userRepository.findByEmail(Email.of(trimmed));
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }
        
        return null;
    }
}
