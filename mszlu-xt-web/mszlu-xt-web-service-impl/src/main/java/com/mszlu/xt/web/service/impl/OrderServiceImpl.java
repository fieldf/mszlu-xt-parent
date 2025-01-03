package com.mszlu.xt.web.service.impl;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.service.AbstractTemplateAction;
import com.mszlu.xt.web.domain.OrderDomain;
import com.mszlu.xt.web.domain.repository.OrderDomainRepository;
import com.mszlu.xt.web.model.params.OrderParam;
import com.mszlu.xt.web.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl extends AbstractService implements OrderService {

    @Autowired
    private OrderDomainRepository orderDomainRepository;

    @Override
    public CallResult submitOrder(OrderParam orderParam) {
        OrderDomain orderDomain = orderDomainRepository.createDomain(orderParam);
        return this.serviceTemplate.execute(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return orderDomain.submitOrder();
            }
        });
    }

    @Override
    public CallResult wxPay(OrderParam orderParam) {
        OrderDomain orderDomain = orderDomainRepository.createDomain(orderParam);
        return this.serviceTemplate.execute(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return orderDomain.wxPay();
            }
        });
    }

    @Override
    public CallResult notifyOrder(String xmlData) {
        OrderDomain orderDomain = orderDomainRepository.createDomain(null);
        return this.serviceTemplate.execute(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return orderDomain.notifyOrder(xmlData);
            }
        });
    }

    @Override
    public CallResult findOrder(OrderParam orderParam) {
        OrderDomain orderDomain = orderDomainRepository.createDomain(orderParam);
        return this.serviceTemplate.executeQuery(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return orderDomain.findOrder();
            }
        });
    }

    @Override
    public CallResult orderList(OrderParam orderParam) {
        OrderDomain orderDomain = this.orderDomainRepository.createDomain(orderParam);
        return this.serviceTemplate.executeQuery(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return orderDomain.orderList();
            }
        });
    }
}
