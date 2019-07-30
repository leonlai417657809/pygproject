package com.pinyougou.cart.service.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {
    /**
     * 实现动态认证；根据username到数据库中查询用户
     * 与页面中输入的用户名、密码与数据库中的对比；如果一致则认证成功，否则失败返回null
     * @param username 在页面中输入的用户名
     * @return 用户信息
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //角色权限列表
        //用户认证的工作已经交由 cas 认证，当前类只作查询用户权限
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        //用户信息（用户名、密码、角色）
        return new User(username,"",authorities);
    }
}
