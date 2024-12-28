package com.mszlu.xt.web.api;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.web.model.params.BillParam;
import com.mszlu.xt.web.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("i")
public class InviteApi {

    @Autowired
    private BillService billService;
    @RequestMapping("gen")
    @ResponseBody
    public CallResult gen(@RequestBody BillParam billParam){
        return billService.gen(billParam);
    }


    @RequestMapping("all")
    @ResponseBody
    public CallResult  all(){
        return billService.all(new BillParam());
    }

    @RequestMapping("u/{billType}/{id}")
    public String url(HttpServletRequest request, HttpServletResponse response, @PathVariable("billType") String billType, @PathVariable("id") String id){
        if (id != null){
            try {
                //key需要设置一个用户无法识别的，这样更好的隐藏意图
                Cookie cookies = new Cookie("_i_ga_b_"+billType,id);
                //设置时长为3天
                cookies.setMaxAge(3*24*60*60);
                cookies.setPath("/");
                response.addCookie(cookies);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        String ua = request.getHeader("user-agent").toLowerCase();
        if (ua.indexOf("micromessenger") > 0){
            //微信浏览器 跳转微信登录
            return "redirect:/api/sso/login/authorize";
        }
        return "redirect:http://www.mszlu.com";
    }
}
