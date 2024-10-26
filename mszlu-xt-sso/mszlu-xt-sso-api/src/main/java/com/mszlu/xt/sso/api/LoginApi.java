package com.mszlu.xt.sso.api;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.sso.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// /api/sso/login/getQRCodeUrl
// controller注解默认是跳转页面的
@Controller
@RequestMapping("login")
public class LoginApi {
    @Autowired
    private LoginService loginService;

    @PostMapping("getQRCodeUrl")
    @ResponseBody
    public CallResult getQRCodeUrl() {
        // controller职责 接收参数 处理结果
        return loginService.getQRCodeUrl();
    }
}
