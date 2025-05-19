package com.hmall.api.config;

import com.hmall.api.client.fallback.ItemClientFallback;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLogLevel(){
        //设置Feign的日志级别为FULL：记录所有请求和响应的明细，包括头信息、请求体、元数据
        return Logger.Level.FULL;
    }

    /**
     * 实现微服务之间的用户信息传递，就必须在微服务发起调用时把用户信息存入请求头。
     * 由于微服务之间调用是基于OpenFeign来实现的，故可以由OpenFeign发起的请求自动携带登录用户信息
     * 我们只需要实现助Feign中提供的一个拦截器接口：feign.RequestInterceptor，然后实现apply方法，利用RequestTemplate类来添加请求头，将用户信息保存到请求头中。
     * 这样以来，每次OpenFeign发起请求的时候都会调用该方法，传递用户信息。
     * @return
     */
    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 获取登录用户
                Long userId = UserContext.getUser();
                if(userId == null) {
                    // 如果为空则直接跳过
                    return;
                }
                // 如果不为空则放入请求头中，传递给下游微服务
                template.header("user-info", userId.toString());
            }
        };
    }

    @Bean
    public ItemClientFallback itemClientFallback(){
        return new ItemClientFallback();
    }
}
