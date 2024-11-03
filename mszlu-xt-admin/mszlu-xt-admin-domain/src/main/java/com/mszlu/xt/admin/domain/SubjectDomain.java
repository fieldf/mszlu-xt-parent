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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

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
}
