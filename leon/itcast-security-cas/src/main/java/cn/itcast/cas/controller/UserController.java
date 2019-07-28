package cn.itcast.cas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/user")
@RestController
public class UserController {
    @Autowired
    private HttpServletRequest request;

    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    @GetMapping("/getUsername")
    public String getUsername(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //第二种获取方式：request.getRemoteUser()
        System.out.println("request.getRemoteUser="+request.getRemoteUser());

        return username;
    }
}
