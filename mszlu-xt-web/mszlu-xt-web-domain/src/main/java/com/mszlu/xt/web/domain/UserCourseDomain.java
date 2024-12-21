package com.mszlu.xt.web.domain;

import com.mszlu.xt.pojo.UserCourse;
import com.mszlu.xt.web.domain.repository.UserCourseDomainRepository;
import com.mszlu.xt.web.model.params.UserCourseParam;

import java.util.List;

public class UserCourseDomain {
    private UserCourseDomainRepository userCourseDomainRepository;
    private UserCourseParam userCourseParam;
    public UserCourseDomain(UserCourseDomainRepository userCourseDomainRepository, UserCourseParam userCourseParam) {
        this.userCourseDomainRepository = userCourseDomainRepository;
        this.userCourseParam = userCourseParam;
    }

    public UserCourse findUserCourse(Long userId, Long courseId, long currentTime) {

        return userCourseDomainRepository.findUserCourse(userId, courseId, currentTime);
    }

    public long countUserCourseByCourseId(Long id) {

        return userCourseDomainRepository.countUserCourseByCourseId(id);
    }

    public Integer countUserCourseInCourseIdList(Long userId, List<Long> courseIdList, long currentTimeMillis) {
        return userCourseDomainRepository.countUserCourseInCourseIdList(userId, courseIdList, currentTimeMillis);
    }
}
