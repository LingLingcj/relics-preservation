package com.ling.trigger.http;

import com.ling.api.dto.ChangePasswordDTO;
import com.ling.api.dto.LoginDTO;
import com.ling.api.dto.RegisterDTO;
import com.ling.domain.auth.model.valobj.ChangePasswordVO;
import com.ling.domain.auth.model.valobj.LoginVO;
import com.ling.domain.auth.model.valobj.RegisterVO;
import com.ling.domain.auth.model.valobj.UserInfoVO;
import com.ling.domain.auth.service.IUserAuthService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import com.ling.types.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LingRJ
 * @Description: 登录控制器
 * @DateTime: 2025/6/27 13:58
 **/
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUserAuthService userAuthService;

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
    public Response<Map<String, String>> register(@RequestBody RegisterDTO registerDTO) {
        // 校验密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.PASSWORD_CONFIRM_ERROR.getCode())
                    .info(ResponseCode.PASSWORD_CONFIRM_ERROR.getInfo())
                    .build();
        }

        RegisterVO registerVO = new RegisterVO();
        registerVO.setRole(registerDTO.getRole().getRole());
        registerVO.setUsername(registerDTO.getUsername());
        registerVO.setPassword(registerDTO.getPassword());
        registerVO.setConfirmPassword(registerDTO.getConfirmPassword());

        // 调用服务进行注册
        UserInfoVO userInfo = userAuthService.register(registerVO);
        
        // 处理注册结果
        if (!userInfo.isSuccess()) {
            return Response.<Map<String, String>>builder()
                    .code(getErrorCodeByMessage(userInfo.getMessage()))
                    .info(userInfo.getMessage())
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
    public Response<Map<String, String>> login(@RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(loginDTO, loginVO);
        
        // 调用服务进行登录验证
        UserInfoVO userInfo = userAuthService.login(loginVO);
        
        // 处理登录结果
        if (!userInfo.isSuccess()) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.LOGIN_ERROR.getCode())
                    .info(ResponseCode.LOGIN_ERROR.getInfo())
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
    private Response<Map<String, String>> buildTokenResponse(String token, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", message);

        return Response.<Map<String, String>>builder()
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
    public Response<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // 校验新密码和确认密码
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            return Response.<String>builder()
                    .code(ResponseCode.PASSWORD_CONFIRM_ERROR.getCode())
                    .info(ResponseCode.PASSWORD_CONFIRM_ERROR.getInfo())
                    .build();
        }

        ChangePasswordVO changePasswordVO = new ChangePasswordVO();
        BeanUtils.copyProperties(changePasswordDTO, changePasswordVO);
        
        // 调用服务修改密码
        boolean success = userAuthService.changePassword(changePasswordVO, username);
        
        if (!success) {
            return Response.<String>builder()
                    .code(ResponseCode.OLD_PASSWORD_ERROR.getCode())
                    .info(ResponseCode.OLD_PASSWORD_ERROR.getInfo())
                    .build();
        }
        
        return Response.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data("密码修改成功")
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
