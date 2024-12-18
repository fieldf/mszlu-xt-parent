package com.mszlu.xt.web.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.common.enums.Status;
import com.mszlu.xt.pojo.Course;
import com.mszlu.xt.web.dao.CourseMapper;
import com.mszlu.xt.web.domain.CourseDomain;
import com.mszlu.xt.web.domain.SubjectDomain;
import com.mszlu.xt.web.domain.UserCourseDomain;
import com.mszlu.xt.web.model.params.CourseParam;
import com.mszlu.xt.web.model.params.SubjectParam;
import com.mszlu.xt.web.model.params.UserCourseParam;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class CourseDomainRepository {
    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserCourseDomainRepository userCourseDomainRepository;

    @Resource
    private SubjectDomainRepository subjectDomainRepository;
    public CourseDomain createDomain(CourseParam courseParam) {
        return new CourseDomain(this, courseParam);
    }

    public Page<Course> findCourseByGrade(int currentPage, int pageSize, String subjectGrade) {
        Page<Course> page = new Page<>(currentPage, pageSize);
        return courseMapper.findCourseByGrade(page, subjectGrade);
    }

    public Page<Course> findAllCourse(int currentPage, int pageSize) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCourseStatus, Status.NORMAL.getCode());
        Page<Course> page = new Page<>(currentPage, pageSize);
        Page<Course> courseIPage = courseMapper.selectPage(page, queryWrapper);
        return courseIPage;
    }


    public UserCourseDomain createUserCourseDomain(UserCourseParam userCourseParam) {
        return userCourseDomainRepository.createDomain(userCourseParam);
    }

    public SubjectDomain createSubjectDomain(SubjectParam subjectParam) {
        return subjectDomainRepository.createDomain(subjectParam);
    }

    public Course findCourseById(Long courseId) {
        return courseMapper.selectById(courseId);
    }
}
