package com.mszlu.xt.web.service;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.web.model.params.CourseParam;

public interface CourseService {
    CallResult courseList(CourseParam courseParam);

    /**
     * 根据课程id查询学科详细信息
     * @param courseParam
     * @return
     */
    CallResult subjectInfo(CourseParam courseParam);

    CallResult courseDetail(CourseParam courseParam);
}
