package com.mszlu.xt.web.domain.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mszlu.xt.pojo.Order;
import com.mszlu.xt.web.domain.repository.OrderDomainRepository;
import com.mszlu.xt.web.model.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RocketMQMessageListener(topic = "create_order_delay",consumerGroup = "create_order_delay_group")
@Slf4j
public class OrderCreateMQConsumer implements RocketMQListener<String> {

    @Autowired
    private OrderDomainRepository orderDomainRepository;

    @Override
    public void onMessage(String message) {
        log.info("订单延迟消费的时间:{}，消息:{}",new DateTime(),message);
        //判断订单是否支付 如果没有 则取消
        if (StringUtils.isNotBlank(message)){
            Map<String, String> messageMap = JSON.parseObject(message,new TypeReference<Map<String,String>>(){});
            String orderId = messageMap.get("orderId");
            String time = messageMap.get("time");
            Integer delayTime = Integer.parseInt(time);
            Order order  = orderDomainRepository.findOrderByOrderId(orderId);
            if (order != null){
                Integer orderStatus = order.getOrderStatus();
                if (OrderStatus.INIT.getCode() == orderStatus
                        || OrderStatus.COMMIT.getCode() == orderStatus){
                    //订单不是 已付款 取消 退款的 证明 此订单无效 需要取消
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - order.getCreateTime() >= delayTime * 1000){
                        //满足条件 需要取消
                        boolean isUpdate = this.orderDomainRepository.updateOrderStatus(order,OrderStatus.CANCEL.getCode());
                        if (!isUpdate){
                            throw  new RuntimeException("订单状态可能被修改，不能修改");
                        }
                        log.info("订单已经被取消");
                    }else{
                        throw  new RuntimeException("订单时间不满足,不应该取消");
                    }
                }
                log.info("订单不满足条件~~");
            }
        }
    }
}
