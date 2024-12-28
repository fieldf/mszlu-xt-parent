package com.mszlu.xt.web.api;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.web.model.params.OrderParam;
import com.mszlu.xt.web.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
@Slf4j
public class OrderApi {

    @Autowired
    private OrderService orderService;

    @PostMapping("submitOrder")
    public CallResult submitOrder(@RequestBody OrderParam orderParam){
        return orderService.submitOrder(orderParam);
    }

    @PostMapping("wxPay")
    public CallResult wxPay(@RequestBody OrderParam orderParam) {
        return orderService.wxPay(orderParam);
    }

    /**
     * 支付成功回调接口 可以发送到mq中，mq消费不成功可以进行重复消费直到成功。解决支付回调 用户已经付钱 但回调有问题无法回滚
     * @param xmlData
     * @return
     */
    @PostMapping("notify")
    public String notifyOrder(@RequestBody String xmlData) {
        log.info("notify 数据: {}", xmlData);
        CallResult callResult = orderService.notifyOrder(xmlData);
        if (callResult.isSuccess()) {
            return WxPayNotifyResponse.success("成功");
        }
        return WxPayNotifyResponse.fail("失败");
    }
}
