package com.ling.types.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author: LingRJ
 * @Description: 身份认证方案入口
 * @DateTime: 2025/6/27 9:42
 **/

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Response<String> errorResponse = Response.<String>builder()
                .code(ResponseCode.USER_NOT_LOGGED_IN.getCode())
                .info(ResponseCode.USER_NOT_LOGGED_IN.getInfo())
                .build();

        objectMapper.writeValue(response.getOutputStream() ,errorResponse);
    }
}
