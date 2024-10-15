package com.r.chat.interceptor;

import com.r.chat.entity.constants.Constants;
import com.r.chat.exception.IllegalOperationException;
import com.r.chat.properties.AppProperties;
import com.r.chat.redis.RedisUtils;
import com.r.chat.utils.CollUtils;
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
public class AdminInterceptor implements HandlerInterceptor {
    private final RedisUtils redisUtils;
    private final AppProperties appProperties;

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
        // 用token从redis中获取用户id
        String userId = redisUtils.getUserIdByToken(token);
        // 判断是否管理员账号
        if (!CollUtils.contains(appProperties.getAdminUserIds(), userId)) {
            log.warn("拒绝请求: 非管理员账号");
            throw new IllegalOperationException(Constants.MESSAGE_NOT_ADMIN);
        }
        // 保存自定义的日志输出标识
        MDC.put("admin", " [Admin]");
        // 放行
        log.info("放行请求 匹配管理员账号成功");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除自定义的日志输出标识
        MDC.remove("admin");
    }
}
