package com.mszlu.xt.web.service.impl;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.service.AbstractTemplateAction;
import com.mszlu.xt.web.domain.BillDomain;
import com.mszlu.xt.web.domain.repository.BillDomainRepository;
import com.mszlu.xt.web.model.params.BillParam;
import com.mszlu.xt.web.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BillServiceImpl extends AbstractService implements BillService {
    @Autowired
    private BillDomainRepository billDomainRepository;

    @Override
    public CallResult gen(BillParam billParam) {
        BillDomain billDomain = this.billDomainRepository.createDomain(billParam);
        return billDomain.gen();
    }

    @Override
    public CallResult all(BillParam billParam) {
        BillDomain billDomain = this.billDomainRepository.createDomain(billParam);
        return this.serviceTemplate.executeQuery(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return billDomain.findAllBillModelList();
            }
        });
    }
}
