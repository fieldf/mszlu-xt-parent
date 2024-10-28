package com.mszlu.xt.admin.service.impl;

import com.mszlu.xt.admin.domain.NewsDomain;
import com.mszlu.xt.admin.domain.repository.NewsDomainRepository;
import com.mszlu.xt.admin.params.NewsParam;
import com.mszlu.xt.admin.service.NewsService;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.service.AbstractTemplateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NewsServiceImpl extends AbstractService implements NewsService {
    @Autowired
    private NewsDomainRepository newsDomainRepository;

    @Override
    public CallResult findPage(NewsParam newsParam) {

        NewsDomain newsDomain = newsDomainRepository.createDomain(newsParam);
        return this.serviceTemplate.executeQuery(new AbstractTemplateAction<Object>() {

            @Override
            public CallResult<Object> doAction() {
                return newsDomain.findNewsPage();
            }
        });
    }
}
