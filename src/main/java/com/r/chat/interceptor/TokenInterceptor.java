package com.r.chat.interceptor;

import com.r.chat.context.UserIdContext;
import com.r.chat.context.UserInfoTokenContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.UserTokenInfoDTO;
import com.r.chat.exception.LoginTimeOutException;
import com.r.chat.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {
    private final RedisUtils redisUtils;

    /**
     * 获取token
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            // 没匹配到路径，给全局异常处理controller处理
            return true;
        }

        // 从请求头中获取token
        String token = request.getHeader("token");
        if (token == null) {
            log.warn("拒绝请求: 未携带token 请求uri: {}", request.getRequestURI());
            throw new LoginTimeOutException(Constants.MESSAGE_NOT_LOGIN);
        }
        log.info("获取token: {}", token);

        try {
            // 用token从redis中获取用户对象
            UserTokenInfoDTO userTokenInfoDTO = redisUtils.getUserTokenInfo(token);
            log.info("获取用户信息: {}", userTokenInfoDTO);
            UserInfoTokenContext.setCurrentUserInfoToken(userTokenInfoDTO);
            UserIdContext.setCurrentUserId(userTokenInfoDTO.getUserId());
            // 放行
            return true;
        } catch (Exception ex) {
            log.warn("拒绝请求: 无法获取该token对应的用户信息");
            throw new LoginTimeOutException(Constants.MESSAGE_NOT_LOGIN);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 释放上下文对象
        UserInfoTokenContext.removeCurrentUserInfoToken();
        UserIdContext.removeCurrentUserId();
    }
}
