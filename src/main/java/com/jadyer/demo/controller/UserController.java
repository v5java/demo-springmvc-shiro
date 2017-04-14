package com.jadyer.demo.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * SpringMVC-3.2.4整合Shiro-1.2.2
 * Created by 玄玉<https://jadyer.github.io/> on 2013/09/30 23:37.
 */
@Controller
@RequestMapping("mydemo")
public class UserController {
    @RequestMapping("/logout")
    public String logout(HttpSession session){
        String currentUser = (String)session.getAttribute("currentUser");
        System.out.println("用户[" + currentUser + "]准备登出");
        SecurityUtils.getSubject().logout();
        System.out.println("用户[" + currentUser + "]已登出");
        return InternalResourceViewResolver.REDIRECT_URL_PREFIX + "/";
    }

    @RequestMapping(value="/login", method=RequestMethod.POST)
    public String login(String username, String password, HttpServletRequest request){
        System.out.println("-------------------------------------------------------");
        String rand = (String)request.getSession().getAttribute("rand");
        String captcha = WebUtils.getCleanParam(request, "captcha");
        System.out.println("用户["+username+"]登录时输入的验证码为["+captcha+"]，HttpSession中的验证码为["+rand+"]");
        if(!StringUtils.equals(rand, captcha)){
            request.setAttribute("message_login", "验证码不正确");
            return InternalResourceViewResolver.FORWARD_URL_PREFIX + "/";
        }
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        token.setRememberMe(true);
        System.out.print("为验证登录用户而封装的Token：");
        System.out.println(ReflectionToStringBuilder.toString(token, ToStringStyle.MULTI_LINE_STYLE));
        //获取当前的Subject
        Subject currentUser = SecurityUtils.getSubject();
        try {
            //在调用了login方法后，SecurityManager会收到AuthenticationToken，并将其发送给已配置的Realm执行必须的认证检查
            //每个Realm都能在必要时对提交的AuthenticationTokens作出反应
            //所以这一步在调用login(token)方法时，它会走到MyRealm.doGetAuthenticationInfo()方法中，具体验证方式详见此方法
            System.out.println("对用户[" + username + "]进行登录验证...验证开始");
            currentUser.login(token);
            System.out.println("对用户[" + username + "]进行登录验证...验证通过");
        }catch(UnknownAccountException uae){
            System.out.println("对用户[" + username + "]进行登录验证...验证未通过，未知账户");
            request.setAttribute("message_login", "未知账户");
        }catch(IncorrectCredentialsException ice){
            System.out.println("对用户[" + username + "]进行登录验证...验证未通过，错误的凭证");
            request.setAttribute("message_login", "密码不正确");
        }catch(LockedAccountException lae){
            System.out.println("对用户[" + username + "]进行登录验证...验证未通过，账户已锁定");
            request.setAttribute("message_login", "账户已锁定");
        }catch(ExcessiveAttemptsException eae){
            System.out.println("对用户[" + username + "]进行登录验证...验证未通过，错误次数过多");
            request.setAttribute("message_login", "用户名或密码错误次数过多");
        }catch(AuthenticationException ae){
            //通过处理Shiro的运行时AuthenticationException就可以控制用户登录失败或密码错误时的情景
            System.out.println("对用户[" + username + "]进行登录验证...验证未通过，堆栈轨迹如下");
            ae.printStackTrace();
            request.setAttribute("message_login", "用户名或密码不正确");
        }
        //验证是否登录成功
        if(currentUser.isAuthenticated()){
            System.out.println("用户[" + username + "]登录认证通过（这里可进行一些认证通过后的系统参数初始化操作）");
            return "main";
        }else{
            token.clear();
            return InternalResourceViewResolver.FORWARD_URL_PREFIX + "/";
        }
    }
}