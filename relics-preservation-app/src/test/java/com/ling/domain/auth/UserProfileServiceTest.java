package com.ling.domain.auth;

import com.alibaba.fastjson2.JSON;
import com.ling.domain.auth.model.valobj.ProfileUpdateVO;
import com.ling.domain.auth.model.valobj.UserInfoVO;
import com.ling.domain.auth.service.IUserAuthService;
import com.ling.domain.auth.service.IUserProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: LingRJ
 * @Description: 身份信息修改测试
 * @DateTime: 2025/6/27 16:44
 **/
@SpringBootTest
@Slf4j
public class UserProfileServiceTest {
    @Resource
    private IUserProfileService userProfileService;

    @Test
    public void profile_test() {
        UserInfoVO userInfoVO = userProfileService.getUserInfo("admin");
        log.info("username: {}; userInfoVO:{}", userInfoVO.getUsername(), JSON.toJSONString(userInfoVO));
    }

    @Test
    public void Update_profile_test() {
        ProfileUpdateVO profileUpdateVO = new ProfileUpdateVO();
        profileUpdateVO.setNickname("管理员");
        UserInfoVO userInfoVO = userProfileService.updateUserProfile(profileUpdateVO, "admin");
    }
}
