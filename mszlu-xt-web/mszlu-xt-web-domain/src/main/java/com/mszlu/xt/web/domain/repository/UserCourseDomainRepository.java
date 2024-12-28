package com.mszlu.xt.web.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mszlu.xt.pojo.UserCourse;
import com.mszlu.xt.web.dao.UserCourseMapper;
import com.mszlu.xt.web.domain.UserCourseDomain;
import com.mszlu.xt.web.model.params.UserCourseParam;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class UserCourseDomainRepository {
    @Resource
    private UserCourseMapper userCourseMapper;
    public UserCourseDomain createDomain(UserCourseParam userCourseParam) {
        return new UserCourseDomain(this,userCourseParam);
    }

    public UserCourse findUserCourse(Long userId, Long courseId, long currentTime) {
        LambdaQueryWrapper<UserCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCourse::getCourseId, courseId);
        queryWrapper.eq(UserCourse::getUserId, userId);
        queryWrapper.ge(UserCourse::getExpireTime, currentTime);
        return userCourseMapper.selectOne(queryWrapper);
    }

    public long countUserCourseByCourseId(Long courseId) {
        LambdaQueryWrapper<UserCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCourse::getCourseId, courseId);
        return userCourseMapper.selectCount(queryWrapper);
    }

    public Integer countUserCourseInCourseIdList(Long userId, List<Long> courseIdList, long currentTimeMillis) {
        LambdaQueryWrapper<UserCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCourse::getUserId, userId);
        queryWrapper.in(UserCourse::getCourseId, courseIdList);
        queryWrapper.ge(UserCourse::getExpireTime, currentTimeMillis);
        return userCourseMapper.selectCount(queryWrapper);

    }

    public Integer countUserCourseByUserId(Long userId, List<Long> courseIdList, long currentTime) {
        LambdaQueryWrapper<UserCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserCourse::getCourseId,courseIdList);
        queryWrapper.eq(UserCourse::getUserId,userId);
        queryWrapper.gt(UserCourse::getExpireTime,currentTime);
        return this.userCourseMapper.selectCount(queryWrapper);
    }

    public void saveUserCourse(UserCourse course) {
        this.userCourseMapper.insert(course);
    }

    public void updateUserCourse(UserCourse course) {
        this.userCourseMapper.updateById(course);
    }

    public UserCourse findUserCourseByUserIdAndCourseId(Long userId, Long courseId) {
        LambdaQueryWrapper<UserCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCourse::getCourseId, courseId);
        queryWrapper.eq(UserCourse::getUserId, userId);
        queryWrapper.last("limit 1");
        return userCourseMapper.selectOne(queryWrapper);
    }
}
