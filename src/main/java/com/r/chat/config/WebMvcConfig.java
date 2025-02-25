package com.r.chat.config;

import com.r.chat.interceptor.AdminInterceptor;
import com.r.chat.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig extends WebMvcConfigurationSupport {
    private final TokenInterceptor tokenInterceptor;
    private final AdminInterceptor adminInterceptor;

    /**
     * 解决Spring不解析PUT请求的x-www-form-urlencoded参数的问题
     */
    @Bean
    public FormContentFilter formContentFilter() {
        return new FormContentFilter();
    }

    /**
     * 注册自定义拦截器
     *
     * @param registry 注册表
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("注册自定义拦截器 TokenInterceptor");
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/checkCode")  // 验证码
                .excludePathPatterns("/user/register")  // 注册
                .excludePathPatterns("/user/login");  // 登录
        log.info("注册自定义拦截器 AdminInterceptor");
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }

    /**
     * Ollama的controller接口返回Flux类时中文会变成问号，这里将所有String类转换器的默认编码修改为UTF-8以解决这个问题
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                // 过滤出StringHttpMessageConverter类型实例
                .filter(StringHttpMessageConverter.class::isInstance)
                .map(c -> (StringHttpMessageConverter) c)
                // 这里将转换器的默认编码设置为utf-8
                .forEach(c -> c.setDefaultCharset(StandardCharsets.UTF_8));
    }

    /**
     * Performing asynchronous handling through the default Spring MVC SimpleAsyncTaskExecutor.
     * This executor is not suitable for production use under load.
     * Please, configure an AsyncTaskExecutor through the WebMvc config.<br>
     * 使用了Flux返回数据，需要配置异步线程池
     */
    @Override
    public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(60 * 1000L);
        configurer.registerCallableInterceptors(timeoutInterceptor());
        configurer.setTaskExecutor(threadPoolTaskExecutor());
    }

    @Bean
    public TimeoutCallableProcessingInterceptor timeoutInterceptor() {
        return new TimeoutCallableProcessingInterceptor();
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(10);
        t.setMaxPoolSize(50);
        t.setQueueCapacity(10);
        t.setThreadNamePrefix("WEB-Thread-");
        return t;
    }
}
