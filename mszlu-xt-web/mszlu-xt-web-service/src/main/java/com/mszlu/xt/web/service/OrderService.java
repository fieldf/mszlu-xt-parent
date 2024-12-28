package com.mszlu.xt.web.service;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.web.model.params.OrderParam;

public interface OrderService {

    CallResult submitOrder(OrderParam orderParam);
}
