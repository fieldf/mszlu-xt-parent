package com.mszlu.xt.admin.service.impl;

import com.mszlu.xt.admin.domain.SubjectDomain;
import com.mszlu.xt.admin.domain.repository.SubjectDomainRepository;
import com.mszlu.xt.admin.params.SubjectParam;
import com.mszlu.xt.admin.service.SubjectService;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.service.AbstractTemplateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubjectServiceImpl extends AbstractService implements SubjectService {
    @Autowired
    private SubjectDomainRepository subjectDomainRepository;

    @Override
    public CallResult findSubjectList(SubjectParam subjectParam) {
        SubjectDomain subjectDomain = this.subjectDomainRepository.createDomain(subjectParam);
        return this.serviceTemplate.executeQuery(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return subjectDomain.findSubjectList();
            }
        });
    }
}
