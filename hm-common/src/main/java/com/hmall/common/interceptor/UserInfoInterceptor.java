package com.hmall.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.hmall.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 将用户信息存入ThreadLocal中或移除
 * 写在hm-common：由于每个微服务都有获取登录用户的需求，因此拦截器我们直接写在hm-common中，并写好自动装配。
 * 这样微服务只需要引入hm-common就可以直接具备拦截器功能，无需重复编写。
 */
public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取请求头中的用户信息
        String userInfo = request.getHeader("user-info");
        //2.判断是否为空
        if(StrUtil.isNotBlank(userInfo)){
            //若不为空，则保存到ThreadLocal中
            UserContext.setUser(Long.valueOf(userInfo));
        }
        //3.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除用户
        UserContext.removeUser();
    }
}
