package com.r.chat.interceptor;

import com.r.chat.context.AdminContext;
import com.r.chat.context.UserTokenInfoContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.exception.IllegalOperationException;
import com.r.chat.properties.AppProperties;
import com.r.chat.utils.CollUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {
    private final AppProperties appProperties;

    /**
     * 获取token
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            // 没匹配到路径，给全局异常处理controller处理
            return true;
        }
        // 前一个Token拦截器已经初始化了上下文对象，从上下文对象中获取用户id
        String userId = UserTokenInfoContext.getCurrentUserId();
        // 判断是否管理员账号
        if (!CollUtils.contains(appProperties.getAdminUserIds(), userId)) {
            log.warn("拒绝请求: 非管理员账号");
            throw new IllegalOperationException(Constants.MESSAGE_NOT_ADMIN);
        }
        // 保存自定义的日志输出标识
        MDC.put("admin", " [Admin]");
        // 保存是管理员的信息
        AdminContext.setAdmin(true);
        // 放行
        log.info("放行请求 匹配管理员账号成功");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除自定义的日志输出标识
        MDC.remove("admin");
        // 释放上下文对象
        AdminContext.remove();
    }
}
