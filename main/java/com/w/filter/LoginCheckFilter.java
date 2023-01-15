package com.w.filter;


import com.alibaba.fastjson.JSON;
import com.w.common.BaseContext;
import com.w.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //路径匹配器
    public static final AntPathMatcher ANT_PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {


        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        //1.获取当前请求路径
        String requestURI = request.getRequestURI();

        log.info("拦截到请求：{}",requestURI);

        //2.所有放行的静态资源以及登录功能
        String[] URIs=new String[]{"/employee/login","/employee/logout","/backend/**","/front/**","/user/sendMsg","/user/login"};

        //3.路径匹配器匹配
        boolean check = check(URIs, requestURI);

        //如果能匹配  直接放行
        if(check){
            filterChain.doFilter(request,response);
            return;
        }

        //如果不能匹配 获取Session 查看是否有employee
        if(request.getSession().getAttribute("employee")!=null){
            //将登录id放到ThreadLocal中
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);
            return;
        }

        //如果不能匹配 获取Session 查看是否有user  ——>移动端
        if(request.getSession().getAttribute("user")!=null){
            //将登录id放到ThreadLocal中
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("user"));
            filterChain.doFilter(request,response);
            return;
        }


        //都匹配失败 向前端相应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

    }



    /*
    *  路径匹配器匹配
    *
    * */
    public boolean check(String[] URIs,String requestURI){

        //遍历uris 看当前请求路径是否是直接放行资源
        for (String uri : URIs) {
            boolean match = ANT_PATH_MATCHER.match(uri, requestURI);
            if(match) return true;
        }

        return false;
    }


}
