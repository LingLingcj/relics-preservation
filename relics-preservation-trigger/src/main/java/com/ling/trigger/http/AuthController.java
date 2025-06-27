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
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 注册接口
     * @param registerDTO 注册信息
     * @param session HTTP会话
     * @return 注册结果
     */
    @PostMapping("/register")
    public Response<String> register(@RequestBody RegisterDTO registerDTO, HttpSession session) {
        // 检测身份（Role）选择是否正确
        if(!registerDTO.validateRole()) {
            return Response.<String>builder()
                    .code(ResponseCode.INVALID_ROLE.getCode())
                    .info(ResponseCode.INVALID_ROLE.getInfo())
                    .build();
        }
        // 校验密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Response.<String>builder()
                    .code(ResponseCode.PASSWORD_CONFIRM_ERROR.getCode())
                    .info(ResponseCode.PASSWORD_CONFIRM_ERROR.getInfo())
                    .build();
        }

        RegisterVO registerVO = new RegisterVO();
        registerVO.setUsername(registerDTO.getUsername());
        registerVO.setPassword(registerDTO.getPassword());
        registerVO.setConfirmPassword(registerDTO.getConfirmPassword());

        // 调用服务进行注册
        UserInfoVO userInfo = userAuthService.register(registerVO);
        
        // 处理注册结果
        if (!userInfo.isSuccess()) {
            return Response.<String>builder()
                    .code(getErrorCodeByMessage(userInfo.getMessage()))
                    .info(userInfo.getMessage())
                    .build();
        }
        
        // 将用户信息保存到会话
        session.setAttribute("username", userInfo.getUsername());
        
        return Response.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data("注册成功")
                .build();
    }

    /**
     * 登录接口
     * @param loginDTO 登录信息
     * @param session HTTP会话
     * @return 登录结果
     */
    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(loginDTO,loginVO);
        // 调用服务进行登录
        UserInfoVO userInfo = userAuthService.login(loginVO);
        
        // 处理登录结果
        if (!userInfo.isSuccess()) {
            return Response.<String>builder()
                    .code(ResponseCode.LOGIN_ERROR.getCode())
                    .info(ResponseCode.LOGIN_ERROR.getInfo())
                    .build();
        }
        
        // 将用户信息保存到会话
        session.setAttribute("username", userInfo.getUsername());
        
        return Response.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data("登录成功")
                .build();
    }

    /**
     * 修改密码接口
     * @param changePasswordDTO 修改密码信息
     * @param session HTTP会话
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public Response<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO, HttpSession session) {
        // 获取当前登录用户
        String username = (String) session.getAttribute("username");
        
        // 检查是否已登录
        if (username == null) {
            return Response.<String>builder()
                    .code(ResponseCode.USER_NOT_LOGGED_IN.getCode())
                    .info(ResponseCode.USER_NOT_LOGGED_IN.getInfo())
                    .build();
        }
        
        // 校验新密码和确认密码
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            return Response.<String>builder()
                    .code(ResponseCode.PASSWORD_CONFIRM_ERROR.getCode())
                    .info(ResponseCode.PASSWORD_CONFIRM_ERROR.getInfo())
                    .build();
        }

        ChangePasswordVO changePasswordVO = new ChangePasswordVO();
        BeanUtils.copyProperties(changePasswordDTO,changePasswordVO);
        
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
