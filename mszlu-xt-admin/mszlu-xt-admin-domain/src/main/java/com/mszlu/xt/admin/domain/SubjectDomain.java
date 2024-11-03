package com.mszlu.xt.admin.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.admin.domain.repository.SubjectDomainRepository;
import com.mszlu.xt.admin.model.NewsModel;
import com.mszlu.xt.admin.model.SubjectModel;
import com.mszlu.xt.admin.params.SubjectParam;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.model.ListPageModel;
import com.mszlu.xt.pojo.News;
import com.mszlu.xt.pojo.Subject;
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
        listPageModel.setSize(subjectPage.getSize());
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

}
