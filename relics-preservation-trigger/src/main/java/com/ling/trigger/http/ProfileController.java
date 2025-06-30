package com.ling.trigger.http;

import com.ling.api.dto.request.ProfileUpdateDTO;
import com.ling.api.dto.response.UserInfoResponseDTO;
import com.ling.domain.auth.model.valobj.ProfileUpdateVO;
import com.ling.domain.auth.model.valobj.UserInfoVO;
import com.ling.domain.auth.service.IUserProfileService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: LingRJ
 * @Description: 用户身份信息修改控制器
 * @DateTime: 2025/6/27 15:57
 **/
@RestController
@RequestMapping("/api/user/profile")
@Tag(name = "用户资料管理", description = "用户个人信息查询和修改接口")
@SecurityRequirement(name = "JWT")
public class ProfileController {

    @Autowired
    private IUserProfileService userProfileService;

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    @GetMapping
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细个人信息")
    public Response<UserInfoResponseDTO> getUserProfile(
            @Parameter(description = "JWT令牌", required = true)
            @RequestHeader(value = "Authorization", required = true) String token) {
        // 从Spring Security上下文获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 获取用户信息
        UserInfoVO userInfoVO = userProfileService.getUserInfo(username);

        return responseUserInfo(userInfoVO);
    }

    /**
     * 更新用户信息
     * @param profileUpdateDTO 用户信息更新DTO
     * @return 更新结果
     */
    @PutMapping
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    public Response<UserInfoResponseDTO> updateUserProfile(
            @Parameter(description = "用户信息更新数据", required = true)
            @RequestBody ProfileUpdateDTO profileUpdateDTO,
            @Parameter(description = "JWT令牌", required = true)
            @RequestHeader(value = "Authorization", required = true) String token) {
        // 从Spring Security上下文获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

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
    private Response<UserInfoResponseDTO> responseUserInfo(UserInfoVO userInfoVO) {
        // 处理结果
        if (!userInfoVO.isSuccess()) {
            return Response.<UserInfoResponseDTO>builder()
                    .code(getErrorCodeByMessage(userInfoVO.getMessage()))
                    .info(userInfoVO.getMessage())
                    .build();
        }

        UserInfoResponseDTO userInfoResponseDTO = UserInfoResponseDTO.builder()
                .username(userInfoVO.getUsername())
                .nickname(userInfoVO.getNickname())
                .title(userInfoVO.getTitle())
                .email(userInfoVO.getEmail())
                .avatarUrl(userInfoVO.getAvatarUrl())
                .role(userInfoVO.getRole())
                .fullName(userInfoVO.getFullName())
                .phoneNumber(userInfoVO.getPhoneNumber())
                .build();

        return Response.<UserInfoResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(userInfoResponseDTO)
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
