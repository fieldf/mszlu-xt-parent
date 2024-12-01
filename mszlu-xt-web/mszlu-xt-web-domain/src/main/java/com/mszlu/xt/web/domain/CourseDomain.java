package com.mszlu.xt.web.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.common.login.UserThreadLocal;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.model.ListPageModel;
import com.mszlu.xt.pojo.Course;
import com.mszlu.xt.pojo.UserCourse;
import com.mszlu.xt.web.domain.repository.CourseDomainRepository;
import com.mszlu.xt.web.model.CourseViewModel;
import com.mszlu.xt.web.model.SubjectModel;
import com.mszlu.xt.web.model.params.CourseParam;
import com.mszlu.xt.web.model.params.SubjectParam;
import com.mszlu.xt.web.model.params.UserCourseParam;
import org.apache.catalina.LifecycleState;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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

        Page<Course> coursePage;
        if (StringUtils.isNotBlank(subjectGrade)) {
            coursePage = this.courseDomainRepository.findCourseByGrade(page,pageSize,subjectGrade);
        } else {
            coursePage = this.courseDomainRepository.findAllCourse(page,pageSize);
        }
        List<Course> courseList = coursePage.getRecords();
        List<CourseViewModel> courseViewModels = new ArrayList<>();
        for (Course course : courseList) {
            CourseViewModel courseViewModel = new CourseViewModel();
            BeanUtils.copyProperties(course, courseViewModel);
            // 购买的数量
            long studyCount = this.courseDomainRepository.createUserCourseDomain(new UserCourseParam()).countUserCourseByCourseId(course.getId());
            courseViewModel.setStudyCount((int) studyCount);

            Long userId = UserThreadLocal.get();
            if (userId != null) {
                // 代表用户已登录
                UserCourse userCourse = this.courseDomainRepository.createUserCourseDomain(new UserCourseParam()).findUserCourse(userId, course.getId(), System.currentTimeMillis());
                if (userCourse == null) {
                    courseViewModel.setBuy(0);
                } else {
                    courseViewModel.setBuy(1);
                    courseViewModel.setExpireTime(new DateTime(userCourse.getExpireTime()).toString("yyyy-MM-dd"));
                }
            }
            // 科目信息，根据课程id查找对应的科目信息
            List<SubjectModel> subjectModelList = this.courseDomainRepository.createSubjectDomain(new SubjectParam()).findSubjectListByCourseId(course.getId());
            courseViewModel.setSubjectList(subjectModelList);

            courseViewModel.setSubjectInfo(createSubjectModel(subjectModelList));
            courseViewModels.add(courseViewModel);


        }
        ListPageModel<CourseViewModel> listPageModel = new ListPageModel<>();
        listPageModel.setSize(coursePage.getTotal());
        listPageModel.setPageCount(coursePage.getPages());
        listPageModel.setPage(page);
        listPageModel.setPageSize(pageSize);
        listPageModel.setList(courseViewModels);
        return CallResult.success(listPageModel);
    }

    private SubjectModel createSubjectModel(List<SubjectModel> subjectModelList) {
        SubjectModel subjectModel = new SubjectModel();
        StringBuilder nameBuilder = new StringBuilder();
        StringBuilder termBuilder = new StringBuilder();
        for (SubjectModel model : subjectModelList) {
            if (!nameBuilder.toString().contains(model.getSubjectName())) {
                nameBuilder.append(model.getSubjectName()).append(",");
            }
            if (!termBuilder.toString().contains(model.getSubjectTerm())) {
                termBuilder.append(model.getSubjectTerm()).append(",");
            }
        }
        String name = nameBuilder.toString();
        subjectModel.setSubjectName(name.substring(0, name.lastIndexOf(",")));
        subjectModel.setSubjectGrade(subjectModelList.get(0).getSubjectGrade());
        String term = termBuilder.toString();
        subjectModel.setSubjectTerm(term.substring(0, term.lastIndexOf(",")));

        return subjectModel;
    }
}