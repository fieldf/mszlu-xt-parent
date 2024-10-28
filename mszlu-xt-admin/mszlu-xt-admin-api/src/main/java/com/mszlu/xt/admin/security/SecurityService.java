package com.mszlu.xt.admin.security;

import com.mszlu.xt.admin.model.AdminUserModel;
import com.mszlu.xt.admin.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SecurityService implements UserDetailsService {
    @Autowired
    private AdminUserService adminUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("用户{}登录了，进入了security的认证流程", username);
        // 1. username是登录的用户名
        // 2. 根据用户名查找数据库的数据 一个用户名对应一条数据
        // 3. 拿到用户的密码，组装我们的security的User对象，剩下的验证工作交给security就可以了

        AdminUserModel adminUserModel = adminUserService.findUserByUsername(username);
        if (adminUserModel == null) {
            throw new  UsernameNotFoundException("用户名不存在");
        }
        // 加密的密码
        String password = adminUserModel.getPassword();

        List<GrantedAuthority> authorities = new ArrayList<>();
        User user = new User(username, password, authorities);
        return user;
    }
}
