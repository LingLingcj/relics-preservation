package com.ling.trigger.http;

import com.ling.api.dto.ProfileUpdateDTO;
import com.ling.domain.auth.model.valobj.ProfileUpdateVO;
import com.ling.domain.auth.model.valobj.UserInfoVO;
import com.ling.domain.auth.service.IUserProfileService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: LingRJ
 * @Description: 用户身份信息修改控制器
 * @DateTime: 2025/6/27 15:57
 **/
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    @Autowired
    private IUserProfileService userProfileService;
    
    /**
     * 获取当前用户信息
     * @param session HTTP会话
     * @return 用户信息
     */
    @GetMapping
    public Response<UserInfoVO> getUserProfile(HttpSession session) {
        // 获取当前登录用户
        String username = (String) session.getAttribute("username");
        
        // 检查是否已登录
        if (username == null) {
            return Response.<UserInfoVO>builder()
                    .code(ResponseCode.USER_NOT_LOGGED_IN.getCode())
                    .info(ResponseCode.USER_NOT_LOGGED_IN.getInfo())
                    .build();
        }
        
        // 获取用户信息
        UserInfoVO userInfoVO = userProfileService.getUserInfo(username);
        
        return responseUserInfo(userInfoVO);
    }
    
    /**
     * 更新用户信息
     * @param profileUpdateDTO 用户信息更新DTO
     * @param session HTTP会话
     * @return 更新结果
     */
    @PutMapping
    public Response<UserInfoVO> updateUserProfile(@RequestBody ProfileUpdateDTO profileUpdateDTO, HttpSession session) {
        // 获取当前登录用户
        String username = (String) session.getAttribute("username");
        
        // 检查是否已登录
        if (username == null) {
            return Response.<UserInfoVO>builder()
                    .code(ResponseCode.USER_NOT_LOGGED_IN.getCode())
                    .info(ResponseCode.USER_NOT_LOGGED_IN.getInfo())
                    .build();
        }
        
        // 转换DTO到VO
        ProfileUpdateVO profileUpdateVO = new ProfileUpdateVO();
        BeanUtils.copyProperties(profileUpdateDTO, profileUpdateVO);
        
        // 更新用户信息
        UserInfoVO updatedUserInfo = userProfileService.updateUserProfile(profileUpdateVO, username);
        
        return responseUserInfo(updatedUserInfo);
    }

    /**
     * 提取验证过程作为辅助方法
     * @param userInfoVO 用户信息
     * @return 响应信息
     */
    private Response<UserInfoVO> responseUserInfo(UserInfoVO userInfoVO) {
        // 处理结果
        if (!userInfoVO.isSuccess()) {
            return Response.<UserInfoVO>builder()
                    .code(getErrorCodeByMessage(userInfoVO.getMessage()))
                    .info(userInfoVO.getMessage())
                    .build();
        }

        // 敏感信息处理，清除密码字段
        userInfoVO.setPassword(null);

        return Response.<UserInfoVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(userInfoVO)
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
