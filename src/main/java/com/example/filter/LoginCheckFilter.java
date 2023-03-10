package com.example.filter;


import com.alibaba.fastjson.JSON;
import com.example.common.BaseContext;
import com.example.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns ="/*" )
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;
        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();// /backend/index.html
        log.info("拦截到的请求：{}",requestURI);

        String[] urls=new String[]{//放行的资源
          "/employee/login",
          "/employee/logout",
          "/backend/**",
          "/front/**",
          "/common/**",
          "/user/sendMsg",//移动端发送短信
          "/user/login"//移动端登陆
        };
        //2.判断本次请求是否处理
        boolean flag = check(urls, requestURI);
        //3.不需要，放行
        if(flag){
            log.info("请求{} 不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        //4-1.获取登陆状态，已登录则放行
        if (request.getSession().getAttribute("employee")!=null) {
            log.info("用户已登录，用户的id为： {}",request.getSession().getAttribute("employee"));
            Long empId= (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }
        //4-2.获取移动端登陆状态，已登录则放行
        if (request.getSession().getAttribute("user")!=null) {
            log.info("用户已登录，用户的id为： {}",request.getSession().getAttribute("user"));
            Long userId= (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }
        //5.未登录，通过输出流向客户端响应数据
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));//前端接收到这样的result，自动跳转到登陆界面
        return;
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    /**
     * 路径匹配，检查请求是否放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match) return true;
        }
        return false;
    }
}
