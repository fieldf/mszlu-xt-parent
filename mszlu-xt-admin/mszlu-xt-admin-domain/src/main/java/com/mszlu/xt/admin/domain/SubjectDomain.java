package com.mszlu.xt.admin.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.admin.domain.repository.SubjectDomainRepository;
import com.mszlu.xt.admin.model.NewsModel;
import com.mszlu.xt.admin.model.SubjectModel;
import com.mszlu.xt.admin.params.SubjectParam;
import com.mszlu.xt.common.enums.Status;
import com.mszlu.xt.common.model.BusinessCodeEnum;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.model.ListPageModel;
import com.mszlu.xt.pojo.News;
import com.mszlu.xt.pojo.Subject;
import jdk.nashorn.internal.codegen.CompilerConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubjectDomain {
    private SubjectDomainRepository subjectDomainRepository;
    private SubjectParam subjectParam;
    public SubjectDomain(SubjectDomainRepository subjectDomainRepository, SubjectParam subjectParam) {
        this.subjectDomainRepository = subjectDomainRepository;
        this.subjectParam = subjectParam;
    }

    public CallResult<Object> findSubjectList() {
        int currentPage = this.subjectParam.getCurrentPage();
        int pageSize = this.subjectParam.getPageSize();
        String queryString = this.subjectParam.getQueryString();
        Page<Subject> subjectPage = subjectDomainRepository.findSubjectListPage(currentPage, pageSize, queryString);

        ListPageModel<SubjectModel> listPageModel = new ListPageModel<>();
        List<Subject> records = subjectPage.getRecords();

        List<SubjectModel> list = copyList(records);
        list.forEach(SubjectModel::fillSubjectName);
        listPageModel.setList(list);
        listPageModel.setSize(subjectPage.getTotal());
        return CallResult.success(listPageModel);

    }
    public List<SubjectModel> copyList(List<Subject> subjectList){
        List<SubjectModel> subjectModels = new ArrayList<>();
        for (Subject subject : subjectList){
            SubjectModel subjectModel = new SubjectModel();
            BeanUtils.copyProperties(subject, subjectModel);
            subjectModels.add(subjectModel);
        }
        return subjectModels;
    }

    public CallResult<Object> saveSubject() {
        // 学科添加肯定不能重复
        Subject subject = new Subject();
        BeanUtils.copyProperties(this.subjectParam, subject);
        subject.setStatus(Status.NORMAL.getCode());
        this.subjectDomainRepository.save(subject);
        List<Integer> subjectUnits = this.subjectParam.getSubjectUnits();
        for (Integer subjectUnit : subjectUnits) {
            this.subjectDomainRepository.saveSubjectUnit(subject.getId(), subjectUnit);
        }
        return CallResult.success();
    }

    public CallResult<Object> checkSaveSubjectBiz() {
        // 判断是否重复
        String subjectName = this.subjectParam.getSubjectName();
        String subjectGrade = this.subjectParam.getSubjectGrade();
        String subjectTerm = this.subjectParam.getSubjectTerm();
        Subject subject = this.subjectDomainRepository.findSubjectByCondition(subjectName, subjectGrade, subjectTerm);
        if (subject != null) {
            return CallResult.fail(BusinessCodeEnum.CHECK_BIZ_NO_RESULT.getCode(), "不能重复进行添加");
        }

        return CallResult.success();
    }

    public CallResult<Object> checkSaveSubjectParam() {
        String subjectName = this.subjectParam.getSubjectName();
        String subjectGrade = this.subjectParam.getSubjectGrade();
        String subjectTerm = this.subjectParam.getSubjectTerm();
        if (StringUtils.isBlank(subjectName)
                || StringUtils.isBlank(subjectGrade)
                || StringUtils.isBlank(subjectTerm)) {
            return CallResult.fail(BusinessCodeEnum.CHECK_PARAM_NO_RESULT.getCode(), "参数不能为空");
        }
        return CallResult.success();
    }

    public CallResult<Object> findSubjectById() {
        /**
         * 1. 查询subject表
         * 2. 查询对应的单元表
         */
        Subject subject = this.subjectDomainRepository.findSubjectById(this.subjectParam.getId());
        SubjectModel subjectModel = copy(subject);
        List<Integer> subjectUnits = this.subjectDomainRepository.findSubjectUnitsById(subject.getId());
        subjectModel.setSubjectUnits(subjectUnits);

        return CallResult.success(subjectModel);
    }

    private SubjectModel copy(Subject subject) {
        SubjectModel subjectModel = new SubjectModel();
        BeanUtils.copyProperties(subject, subjectModel);
        return subjectModel;
    }

    public CallResult<Object> updateSubject() {
        /**
         * 1. 更新subject表
         * 2. 更新 单元表,先删除原有的关联关系
         */
        List<Integer> subjectUnits = this.subjectParam.getSubjectUnits();
        Subject subject = new Subject();
        BeanUtils.copyProperties(this.subjectParam, subject);
        // 如果想要判断重复，如何判断
        String subjectName = this.subjectParam.getSubjectName();
        String subjectGrade = this.subjectParam.getSubjectGrade();
        String subjectTerm = this.subjectParam.getSubjectTerm();
        Subject newSubject = this.subjectDomainRepository.findSubjectByCondition(subjectName, subjectGrade, subjectTerm);
        if (newSubject != null && !newSubject.getId().equals(subject.getId())) {
            return CallResult.fail(BusinessCodeEnum.CHECK_BIZ_NO_RESULT.getCode(), "不能添加重复的数据");
        }
        // TODO 差一个逻辑 学科和课程是有关联的 如果课程使用了学科，是不能将学科进行删除的
        this.subjectDomainRepository.update(subject);

        // 单元表 先删 后新增
        this.subjectDomainRepository.deleteUnitBySubjectId(this.subjectParam.getId());
        for (Integer subjectUnit : subjectUnits) {
            this.subjectDomainRepository.saveSubjectUnit(subject.getId(), subjectUnit);
        }
        return CallResult.success();
    }

    public CallResult<Object> allSubjectList() {
        List<Subject> subjectList = subjectDomainRepository.findAll();
        List<SubjectModel> result = copyList(subjectList);
        result.forEach(SubjectModel::fillSubjectName);
        return CallResult.success(result);
    }

    public List<Subject> findAllSubjectList() {
        return subjectDomainRepository.findAll();
    }

    public List<SubjectModel> findAllSubjectModelList() {
        return copyList(subjectDomainRepository.findAll());
    }

    public List<Subject> findSubjectListByCourseId(Long courseId) {

        return this.subjectDomainRepository.findSubjectListByCourseId(courseId);
    }

    public List<String> allSubjectIdList() {
        List<Subject> subjectList = subjectDomainRepository.findAll();
        return subjectList.stream().map(subject -> subject.getId().toString()).collect(Collectors.toList());
    }
}
