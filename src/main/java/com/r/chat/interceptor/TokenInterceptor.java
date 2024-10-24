package com.r.chat.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.exception.LoginTimeOutException;
import com.r.chat.redis.RedisUtils;
import com.r.chat.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

        log.info("请求uri : {}", request.getRequestURI());
        JSONObject json = new JSONObject();
        request.getParameterMap().forEach((key, value) -> {
            json.put(key, request.getParameter(key));
        });
        if (!json.isEmpty()) {
            log.info("请求参数: {}", json.toJSONString());
        }

        // 从请求头中获取token
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            log.warn("拒绝请求: 未携带token 请求uri: {}", request.getRequestURI());
            throw new LoginTimeOutException(Constants.MESSAGE_NOT_LOGIN);
        }
        if (token.startsWith("Bearer")) {
            token = token.substring(7);
        }
        log.info("获取token: {}", token);

        try {
            // 用token从redis中获取用户id
            String userId = redisUtils.getUserIdByToken(token);
            if (StringUtils.isEmpty(userId)) {
                throw new LoginTimeOutException(Constants.MESSAGE_NOT_LOGIN);
            }
            log.info("获取用户id: {}", userId);
            UserIdContext.setCurrentUserId(userId);
            // 保存自定义的日志输出标识
            MDC.put("userId", " " + userId);
            // 放行
            log.info("放行请求 匹配token成功");
            return true;
        } catch (Exception ex) {
            log.warn("拒绝请求: 无法获取该token对应的用户信息");
            throw new LoginTimeOutException(Constants.MESSAGE_NOT_LOGIN);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 释放上下文对象
        UserIdContext.removeCurrentUserId();
        // 移除自定义的日志输出标识
        MDC.remove("userId");
    }
}
