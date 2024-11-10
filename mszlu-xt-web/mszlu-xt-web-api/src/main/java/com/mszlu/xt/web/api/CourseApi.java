package com.mszlu.xt.web.api;

import com.mszlu.xt.common.login.UserThreadLocal;
import com.mszlu.xt.common.model.CallResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("course")
public class CourseApi {

    @GetMapping(value = "login")
    public CallResult courseList(){

        return CallResult.success(UserThreadLocal.get());
    }
}
