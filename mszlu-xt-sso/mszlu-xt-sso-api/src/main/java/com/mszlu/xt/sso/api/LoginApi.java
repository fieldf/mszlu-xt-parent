package com.mszlu.xt.sso.api;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.sso.model.params.LoginParam;
import com.mszlu.xt.sso.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    // redirect_uri
    @GetMapping("wxLoginCallBack")
    public String wxLoginCallBack(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String code, String state) {
        // 为了service层统一，所有的api层的参数处理，都放入loginParams中
        LoginParam loginParam = new LoginParam();
        loginParam.setCode(code);
        loginParam.setState(state);
        loginParam.setRequest(request);
        // 后续 登录成功之后，要生成token，提供给前端，把token放入对应的cookie当中
        // response.addCookie
        loginParam.setResponse(response);

        CallResult callResult = loginService.wxLoginCallBack(loginParam);
        if (callResult.isSuccess()) {
            return "redirect:http://www.mszlu.com/course";
        }
        // 跳转首页
        return "redirect:http://www.mszlu.com";

    }
}
