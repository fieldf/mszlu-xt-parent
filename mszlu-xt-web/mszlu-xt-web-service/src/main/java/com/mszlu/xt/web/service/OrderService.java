package com.mszlu.xt.web.service;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.web.model.params.OrderParam;

public interface OrderService {

    CallResult submitOrder(OrderParam orderParam);

    /**
     * 根据订单id和订单类型生成支付二维码
     * @param orderParam
     * @return
     */
    CallResult wxPay(OrderParam orderParam);

    CallResult notifyOrder(String xmlData);
}
