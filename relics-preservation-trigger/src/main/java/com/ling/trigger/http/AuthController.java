package com.ling.trigger.http;

import com.ling.api.dto.request.ChangePasswordDTO;
import com.ling.api.dto.request.LoginDTO;
import com.ling.api.dto.request.RegisterDTO;
import com.ling.api.dto.response.AuthResponseDTO;
import com.ling.api.dto.response.MessageResponseDTO;

import com.ling.domain.user.service.IUserAuthenticationService;
import com.ling.domain.user.service.IUserRegistrationService;
import com.ling.domain.user.service.IUserManagementService;
import com.ling.domain.user.model.entity.User;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import com.ling.types.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.ServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: LingRJ
 * @Description: 登录控制器
 * @DateTime: 2025/6/27 13:58
 **/
@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户登录、注册和密码管理接口")
public class AuthController {

    @Autowired
    private IUserAuthenticationService userAuthenticationService;

    @Autowired
    private IUserRegistrationService userRegistrationService;

    @Autowired
    private IUserManagementService userManagementService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    /**
     * 注册接口
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户并返回JWT令牌")
    public Response<AuthResponseDTO> register(
            @Parameter(description = "注册信息", required = true)
            @RequestBody RegisterDTO registerDTO) {
        // 校验密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Response.<AuthResponseDTO>builder()
                    .code(ResponseCode.PASSWORD_CONFIRM_ERROR.getCode())
                    .info(ResponseCode.PASSWORD_CONFIRM_ERROR.getInfo())
                    .build();
        }

        // 调用新的注册服务
        IUserRegistrationService.RegistrationResult result = userRegistrationService.register(
                registerDTO.getUsername(),
                registerDTO.getPassword(),
                registerDTO.getConfirmPassword(),
                registerDTO.getRole().toString()
        );

        // 处理注册结果
        if (!result.isSuccess()) {
            return Response.<AuthResponseDTO>builder()
                    .code(getErrorCodeByMessage(result.getMessage()))
                    .info(result.getMessage())
                    .build();
        }
        
        // 注册成功后，登录并生成令牌
        Authentication authentication = authenticateUser(registerDTO.getUsername(), registerDTO.getPassword());
        String token = jwtTokenProvider.generateToken(authentication);


        return buildTokenResponse(token, "注册成功");
    }

    /**
     * 登录接口
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "验证用户凭据并返回JWT令牌")
    public Response<AuthResponseDTO> login(
            @Parameter(description = "登录信息", required = true)
            @RequestBody LoginDTO loginDTO) {
        // 调用新的认证服务
        IUserAuthenticationService.AuthenticationResult result = userAuthenticationService.authenticate(
                loginDTO.getUsername(),
                loginDTO.getPassword()
        );

        // 处理登录结果
        if (!result.isSuccess()) {
            return Response.<AuthResponseDTO>builder()
                    .code(ResponseCode.LOGIN_ERROR.getCode())
                    .info(ResponseCode.LOGIN_ERROR.getInfo())
                    .build();
        }

        // 检查角色匹配
        User user = result.getUser();
        if (!user.getRole().getCode().equals(loginDTO.getRole().toString())) {
            return Response.<AuthResponseDTO>builder()
                    .code(ResponseCode.WRONG_ROLE.getCode())
                    .info(ResponseCode.WRONG_ROLE.getInfo())
                    .build();
        }
        
        // 登录成功，生成JWT令牌
        Authentication authentication = authenticateUser(loginDTO.getUsername(), loginDTO.getPassword());
        String token = jwtTokenProvider.generateToken(authentication);

        return buildTokenResponse(token, "登录成功");
    }

    private Authentication authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    // 令牌响应
    private Response<AuthResponseDTO> buildTokenResponse(String token, String message) {
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(token)
                .message(message)
                .build();

        return Response.<AuthResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    /**
     * 修改密码接口
     * @param changePasswordDTO 修改密码信息
     * @return 修改结果
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @SecurityRequirement(name = "JWT")
    public Response<MessageResponseDTO> changePassword(
            @Parameter(description = "密码修改信息", required = true)
            @RequestBody ChangePasswordDTO changePasswordDTO) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // 校验新密码和确认密码
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            return Response.<MessageResponseDTO>builder()
                    .code(ResponseCode.PASSWORD_CONFIRM_ERROR.getCode())
                    .info(ResponseCode.PASSWORD_CONFIRM_ERROR.getInfo())
                    .build();
        }

        // 调用新的用户管理服务修改密码
        IUserManagementService.PasswordChangeResult result = userManagementService.changePassword(
                username,
                changePasswordDTO.getOldPassword(),
                changePasswordDTO.getNewPassword()
        );

        if (!result.isSuccess()) {
            return Response.<MessageResponseDTO>builder()
                    .code(ResponseCode.OLD_PASSWORD_ERROR.getCode())
                    .info(result.getMessage())
                    .build();
        }
        
        MessageResponseDTO messageResponseDTO = MessageResponseDTO.builder()
                .message("密码修改成功")
                .build();
                
        return Response.<MessageResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(messageResponseDTO)
                .build();
    }
    
    /**
     * 根据错误信息获取对应的错误码
     * @param message 错误信息
     * @return 错误码
     */
    private String getErrorCodeByMessage(String message) {
        for (ResponseCode code : ResponseCode.values()) {
            if (code.getInfo().equals(message)) {
                return code.getCode();
            }
        }
        return ResponseCode.SYSTEM_ERROR.getCode();
    }
}
