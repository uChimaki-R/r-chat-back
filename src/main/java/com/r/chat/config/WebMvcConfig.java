package com.r.chat.config;

import com.r.chat.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig extends WebMvcConfigurationSupport {
    private final TokenInterceptor tokenInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry 注册表
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("注册自定义拦截器 TokenInterceptor");
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/swagger-resources/**","/swagger-ui/**", "/v3/**", "/error")  // NoHandlerFoundException映射到的error请求、knife4j的请求
                .excludePathPatterns("/user/checkCode")  // 验证码
                .excludePathPatterns("/user/register")  // 注册
                .excludePathPatterns("/user/login");  // 登录
    }

    /**
     * 设置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html", "doc.html", "favicon.ico").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }

}
