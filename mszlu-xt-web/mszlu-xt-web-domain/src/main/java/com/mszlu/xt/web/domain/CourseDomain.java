package com.mszlu.xt.web.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.pojo.Course;
import com.mszlu.xt.web.domain.repository.CourseDomainRepository;
import com.mszlu.xt.web.model.CourseViewModel;
import com.mszlu.xt.web.model.params.CourseParam;
import org.apache.catalina.LifecycleState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class CourseDomain {
    private CourseDomainRepository courseDomainRepository;
    private CourseParam courseParam;
    public CourseDomain(CourseDomainRepository courseDomainRepository, CourseParam courseParam) {
        this.courseDomainRepository = courseDomainRepository;
        this.courseParam = courseParam;
    }


    public CallResult<Object> courseList() {
        /**
         * 1. 如果根据年级进行查询，需要先找到年级对应的科目列表，根据科目列表去查询课程列表
         * 2. 如果年级为空 查询全部的课程即可
         * 3. 用户购买课程的信息 课程中科目的名称信息
         * 4. 判断用户是否登录 如果登录 去user_course表中查询相关的信息 进行展示
         * 5. 根据课程id 去查询对应的科目名称
         */
        int page = this.courseParam.getPage();
        int pageSize = this.courseParam.getPageSize();
        String subjectGrade = this.courseParam.getSubjectGrade();
        List<Course> courseList = new ArrayList<>();
        if (StringUtils.isNotBlank(subjectGrade)) {
            Page<Course> coursePage = this.courseDomainRepository.findCourseByGrade(page,pageSize,subjectGrade);
            courseList = coursePage.getRecords();
        } else {
            Page<Course> coursePage = this.courseDomainRepository.findAllCourse(page,pageSize);
            courseList = coursePage.getRecords();
        }
        List<CourseViewModel> courseViewModels = new ArrayList<>();
        for (Course course : courseList) {
            CourseViewModel courseViewModel = new CourseViewModel();
            BeanUtils.copyProperties(course, courseViewModel);
            courseViewModels.add(courseViewModel);
        }
        return CallResult.success(courseViewModels);
    }
}
