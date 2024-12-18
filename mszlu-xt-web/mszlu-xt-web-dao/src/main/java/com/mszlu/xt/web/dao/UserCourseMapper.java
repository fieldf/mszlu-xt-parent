package com.mszlu.xt.web.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.pojo.Course;
import com.mszlu.xt.pojo.UserCourse;

public interface UserCourseMapper extends BaseMapper<UserCourse> {
    /**
     * 根据年级进行课程的列表查询
     * @param subjectGrade
     * @return
     */
    Page<Course> findCourseByGrade(Page<Course> page, String subjectGrade);
}
