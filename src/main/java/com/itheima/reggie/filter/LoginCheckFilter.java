package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否以及完全登录
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取本次请求URI
        String uri = request.getRequestURI();
        long id = Thread.currentThread().getId();
        log.info("线程 {}",id);
        //2.判断本次请求是否需要处理
        //定义不需要检查的路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/upload",
                "/common/download",
                "/user/sendMsg", //移动端发送短信
                "/user/login"//移动端登录

        };

        boolean check = check(urls, uri);
        //3.如不需要处理，直接放行
        if(check){
            filterChain.doFilter(request,response);
            return;
        }

        //4-1.判断登录状态，若登录则放行
        if(request.getSession().getAttribute("employee") != null){
            Long employee = (Long) request.getSession().getAttribute("employee");
            log.info("存入的id值为 {}",employee);
            BaseContext.set(employee);
            filterChain.doFilter(request,response);
            return;
        }
        //4-2.判断登录状态，若登录则放行
        if(request.getSession().getAttribute("user") != null){
            Long userId = (Long) request.getSession().getAttribute("user");
            log.info("存入的id值为 {}",userId);
            BaseContext.set(userId);
            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登录");
        //5.如果未登录，通过输出流方式向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("拦截到请求{}",uri);
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }


}
