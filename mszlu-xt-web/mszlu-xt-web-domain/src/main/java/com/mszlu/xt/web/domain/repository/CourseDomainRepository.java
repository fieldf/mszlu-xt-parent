package com.mszlu.xt.web.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.common.enums.Status;
import com.mszlu.xt.pojo.Course;
import com.mszlu.xt.pojo.CourseSubject;
import com.mszlu.xt.web.dao.CourseMapper;
import com.mszlu.xt.web.dao.CourseSubjectMapper;
import com.mszlu.xt.web.domain.*;
import com.mszlu.xt.web.model.params.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseDomainRepository {
    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CourseSubjectMapper courseSubjectMapper;

    @Resource
    private UserCourseDomainRepository userCourseDomainRepository;

    @Resource
    private SubjectDomainRepository subjectDomainRepository;

    @Autowired
    private UserHistoryDomainRepository userHistoryDomainRepository;

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

    public UserHistoryDomain createUserHistoryDomain(UserHistoryParam userHistoryParam) {
        return userHistoryDomainRepository.createDomain(userHistoryParam);
    }

    @Autowired
    private CouponDomainRepository couponDomainRepository;

    public CouponDomain createCouponDomain(CouponParam couponParam) {
        return this.couponDomainRepository.createDomain(couponParam);
    }

    public List<Long> findCourseIdListBySubjectId(Long subjectId) {
        LambdaQueryWrapper<CourseSubject> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(CourseSubject::getSubjectId, subjectId);
        List<CourseSubject> courseSubjects = this.courseSubjectMapper.selectList(queryWrapper);
        return courseSubjects.stream().map(CourseSubject::getCourseId).collect(Collectors.toList());
    }


}
