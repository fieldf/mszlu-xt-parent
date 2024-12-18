package com.mszlu.xt.admin.service;


import com.mszlu.xt.admin.params.SubjectParam;
import com.mszlu.xt.common.model.CallResult;

/**
 * @author Jarno
 */
public interface SubjectService {

    /**
     * 分页查询
     * @param subjectParam
     * @return
     */
    CallResult findSubjectList(SubjectParam subjectParam);

    CallResult saveSubject(SubjectParam subjectParam);

    /**
     * 根据id查询subject
     * @param subjectParam
     * @return
     */
    CallResult findSubjectById(SubjectParam subjectParam);

    /**
     * 编辑学科信息
     * @param subjectParam
     * @return
     */
    CallResult updateSubject(SubjectParam subjectParam);

    CallResult allSubjectList(SubjectParam subjectParam);
}
