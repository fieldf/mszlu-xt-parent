package com.mszlu.xt.web.api;

import com.mszlu.xt.common.annontation.NoAuth;
import com.mszlu.xt.common.login.UserThreadLocal;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.web.model.params.CourseParam;
import com.mszlu.xt.web.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("course")
public class CourseApi {
    @Autowired
    private CourseService courseService;
    @NoAuth
    @PostMapping(value = "courseList")
    public CallResult courseList(@RequestBody CourseParam courseParam){
        return courseService.courseList(courseParam);
    }
}
