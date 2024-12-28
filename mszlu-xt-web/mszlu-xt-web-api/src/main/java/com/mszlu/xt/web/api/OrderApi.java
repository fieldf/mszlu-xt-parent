package com.mszlu.xt.web.api;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.web.model.params.OrderParam;
import com.mszlu.xt.web.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
public class OrderApi {

    @Autowired
    private OrderService orderService;

    @PostMapping("submitOrder")
    public CallResult submitOrder(@RequestBody OrderParam orderParam){
        return orderService.submitOrder(orderParam);
    }
}
