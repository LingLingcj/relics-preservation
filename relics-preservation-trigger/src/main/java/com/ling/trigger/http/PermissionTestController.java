package com.ling.trigger.http;

import com.ling.domain.interaction.security.CommentReviewPermission;
import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.service.IUserManagementService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 权限测试控制器
 * @Author: LingRJ
 * @Description: 用于测试不同角色的权限控制
 * @DateTime: 2025/7/11
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "权限测试", description = "测试不同角色权限的接口")
@Slf4j
public class PermissionTestController {
    
    @Autowired
    private IUserManagementService userManagementService;
    
    @GetMapping("/public")
    @Operation(summary = "公开接口", description = "无需认证即可访问")
    public Response<String> publicEndpoint() {
        return Response.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("公开接口访问成功")
                .data("任何人都可以访问此接口")
                .build();
    }
    
    @GetMapping("/protected")
    @Operation(summary = "受保护接口", description = "需要认证才能访问")
    public Response<Map<String, Object>> protectedEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> data = new HashMap<>();
        data.put("username", auth.getName());
        data.put("authorities", auth.getAuthorities());
        data.put("message", "认证用户可以访问此接口");
        
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("受保护接口访问成功")
                .data(data)
                .build();
    }
    
    @GetMapping("/expert")
    @Operation(summary = "专家接口", description = "只有专家角色可以访问")
    @PreAuthorize("hasRole('EXPERT')")
    public Response<Map<String, Object>> expertEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Optional<User> userOpt = userManagementService.getUserInfo(auth.getName());
        
        Map<String, Object> data = new HashMap<>();
        data.put("username", auth.getName());
        data.put("role", userOpt.map(user -> user.getRole().getCode()).orElse("UNKNOWN"));
        data.put("message", "只有专家可以访问此接口");
        
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("专家接口访问成功")
                .data(data)
                .build();
    }
    
    @GetMapping("/admin")
    @Operation(summary = "管理员接口", description = "只有管理员角色可以访问")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<Map<String, Object>> adminEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Optional<User> userOpt = userManagementService.getUserInfo(auth.getName());
        
        Map<String, Object> data = new HashMap<>();
        data.put("username", auth.getName());
        data.put("role", userOpt.map(user -> user.getRole().getCode()).orElse("UNKNOWN"));
        data.put("message", "只有管理员可以访问此接口");
        
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("管理员接口访问成功")
                .data(data)
                .build();
    }
    
    @GetMapping("/comment-review")
    @Operation(summary = "评论审核权限测试", description = "测试评论审核权限（专家或管理员）")
    @CommentReviewPermission("评论审核权限测试")
    public Response<Map<String, Object>> commentReviewEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Optional<User> userOpt = userManagementService.getUserInfo(auth.getName());
        
        Map<String, Object> data = new HashMap<>();
        data.put("username", auth.getName());
        data.put("role", userOpt.map(user -> user.getRole().getCode()).orElse("UNKNOWN"));
        data.put("permissions", userOpt.map(user -> user.getRole().getPermissions()).orElse(null));
        data.put("message", "具有评论审核权限的用户可以访问此接口");
        
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("评论审核权限测试成功")
                .data(data)
                .build();
    }
    
    @GetMapping("/user-info")
    @Operation(summary = "获取当前用户信息", description = "获取当前认证用户的详细信息")
    public Response<Map<String, Object>> getCurrentUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("用户未认证")
                    .build();
        }
        
        String username = auth.getName();
        Optional<User> userOpt = userManagementService.getUserInfo(username);
        
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("authorities", auth.getAuthorities());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            data.put("role", user.getRole().getCode());
            data.put("roleDescription", user.getRole().getDescription());
            data.put("permissions", user.getRole().getPermissions());
            data.put("nickname", user.getNickname());
            data.put("status", user.getStatus().getCode());
        } else {
            data.put("error", "用户信息不存在");
        }
        
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取用户信息成功")
                .data(data)
                .build();
    }
}
