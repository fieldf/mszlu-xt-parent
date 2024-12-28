package com.mszlu.xt.web.domain.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mszlu.xt.pojo.Bill;
import com.mszlu.xt.web.dao.BillMapper;
import com.mszlu.xt.web.domain.BillDomain;
import com.mszlu.xt.web.model.params.BillParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class BillDomainRepository {


    @Value("invite.url")
    public String inviteUrl;

    public BillDomain createDomain(BillParam billParam){
        return new BillDomain(this,billParam);
    }
    @Resource
    private BillMapper billMapper;

    public Bill findBill(Long id) {
        return billMapper.selectById(id);
    }

    public List<Bill> findBillList() {
        return billMapper.selectList(Wrappers.lambdaQuery());
    }
}
