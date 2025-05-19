package com.hmall.gateway.filters;

import cn.hutool.core.text.AntPathMatcher;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 登录校验的过滤器
 */
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final JwtTool jwtTool;

    //spring提供的路径匹配器
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取request
        ServerHttpRequest request = exchange.getRequest();

        //2.判断是否需要做登录拦截（不是所有路径都要做登录校验,因此需要与yaml文件中配置的文件进行匹配）
        if(isExclude(request.getPath().toString())){//如果为需要放行的路径，即不需要做登录校验的逻辑
            //放行
            return chain.filter(exchange);
        }

        //3.获取token
        String token=null;
        List<String> headers =  request.getHeaders().get("authorization");//jwt令牌默认保存到 key 为 authentication 的请求头中
        if(headers != null && !headers.isEmpty()){
            token = headers.get(0);
        }
        //4.校验并解析token
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);//parseToken会校验token是否为空，因此不需要再对token进行非空判断
        }catch (UnauthorizedException e){
            //拦截，设置响应状态码为401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);//状态为未登录
            return response.setComplete();
        }
        // 5.传递用户信息
        //在获取用户信息后保存到请求头，转发到下游微服务
        String userInfo = userId.toString();
        ServerWebExchange ex = exchange.mutate()
                .request(b -> b.header("user-info",userInfo))
                .build();
        //6.放行
        return chain.filter(ex);
    }

    private boolean isExclude(String path) {
        for(String pathPattern : authProperties.getExcludePaths()){//遍历配置类包含的可放行路径
            if(antPathMatcher.match(pathPattern,path)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
