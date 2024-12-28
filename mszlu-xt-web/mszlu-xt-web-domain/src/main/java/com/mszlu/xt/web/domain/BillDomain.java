package com.mszlu.xt.web.domain;

import com.mszlu.xt.common.login.UserThreadLocal;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.utils.AESUtils;
import com.mszlu.xt.pojo.Bill;
import com.mszlu.xt.web.domain.repository.BillDomainRepository;
import com.mszlu.xt.web.model.BillModel;
import com.mszlu.xt.web.model.params.BillParam;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class BillDomain {
    private BillDomainRepository billDomainRepository;
    private BillParam billParam;

    public BillDomain(BillDomainRepository billDomainRepository, BillParam billParam) {
        this.billDomainRepository = billDomainRepository;
        this.billParam = billParam;
    }

    public CallResult gen() {
        Long id = this.billParam.getId();
        //根据传递的参数 获取海报信息
        Bill bill = this.billDomainRepository.findBill(id);
        if (bill == null){
            return CallResult.fail(-999,"id 不存在");
        }
        Long userId = UserThreadLocal.get();
        //将用户id做加密处理，AES对称加密算法，可以解密
        String userIdStr = AESUtils.encrypt(String.valueOf(userId));

        return CallResult.success(this.billDomainRepository.inviteUrl +bill.getBillType()+"/"+userIdStr);
    }

    public CallResult<Object> findAllBillModelList() {
        List<Bill> billList = this.billDomainRepository.findBillList();
        List<BillModel> billModelList = new ArrayList<>();
        BeanUtils.copyProperties(billList,billModelList);
        return CallResult.success(billModelList);
    }
}
