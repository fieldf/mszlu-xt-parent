package com.mszlu.xt.web.api;

import com.mszlu.xt.common.annontation.NoAuth;
import com.mszlu.xt.common.cache.Cache;
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
//    @Cache(name = "web_courseList", time = 5 * 60 *1000, hasUser = true)
    public CallResult courseList(@RequestBody CourseParam courseParam){
        return courseService.courseList(courseParam);
    }


    @PostMapping("subjectInfo")
    public CallResult subjectInfo(@RequestBody CourseParam courseParam){
        return courseService.subjectInfo(courseParam);
    }

    @PostMapping(value = "courseDetail")
    public CallResult courseDetail(@RequestBody CourseParam courseParam){
        return courseService.courseDetail(courseParam);
    }

    @PostMapping(value = "myCoupon")
    public CallResult myCoupon(@RequestBody CourseParam courseParam){
        return courseService.myCoupon(courseParam);
    }
}
