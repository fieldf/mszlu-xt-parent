package com.mszlu.xt.web.domain;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayNativeOrderResult;
import com.github.binarywang.wxpay.bean.request.BaseWxPayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.mszlu.xt.common.login.UserThreadLocal;
import com.mszlu.xt.common.model.BusinessCodeEnum;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.model.ListPageModel;
import com.mszlu.xt.common.utils.AESUtils;
import com.mszlu.xt.common.utils.CommonUtils;
import com.mszlu.xt.pojo.*;
import com.mszlu.xt.sso.model.enums.InviteType;
import com.mszlu.xt.web.domain.pay.WxPayDomain;
import com.mszlu.xt.web.domain.repository.OrderDomainRepository;
import com.mszlu.xt.web.model.CourseViewModel;
import com.mszlu.xt.web.model.OrderDisplayModel;
import com.mszlu.xt.web.model.OrderViewModel;
import com.mszlu.xt.web.model.SubjectModel;
import com.mszlu.xt.web.model.enums.OrderStatus;
import com.mszlu.xt.web.model.enums.PayStatus;
import com.mszlu.xt.web.model.enums.PayType;
import com.mszlu.xt.web.model.params.OrderParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class OrderDomain {
    private OrderDomainRepository orderDomainRepository;
    private OrderParam orderParam;

    public OrderDomain(OrderDomainRepository orderDomainRepository, OrderParam orderParam) {
        this.orderDomainRepository = orderDomainRepository;
        this.orderParam = orderParam;
    }

    public CallResult<Object> submitOrder() {
        Long userId = UserThreadLocal.get();
        Long courseId = this.orderParam.getCourseId();
        Course course = this.orderDomainRepository.createCourseDomain(null).findCourseById(courseId);
        if (course == null) {
            return CallResult.fail(BusinessCodeEnum.COURSE_NOT_EXIST.getCode(),"course not exist");
        }
        Long couponId = this.orderParam.getCouponId();
        BigDecimal couponPrice = new BigDecimal(0);
        if (couponId != null){
            Coupon coupon = this.orderDomainRepository.createCouponDomain(null).findCouponById(couponId);
            if (coupon != null){
                couponPrice = checkCoupon(userId,coupon);
            }
        }else{
            couponId = -1L;
        }
        Order order = new Order();
        order.setCourseId(courseId);
        order.setCouponId(couponId);
        long createTime = System.currentTimeMillis();
        order.setCreateTime(createTime);
        order.setExpireTime(course.getOrderTime());
//        order.setOrderAmount(course.getCourseZhePrice().subtract(couponPrice));
        //为了测试 价格定为1分
        order.setOrderAmount(BigDecimal.valueOf(0.01));
        String orderId = createTime + String.valueOf(CommonUtils.random5Num()) + userId;
        order.setOrderId(orderId);
        order.setOrderStatus(OrderStatus.INIT.getCode());
        order.setPayType(PayType.WX.getCode());//默认微信支付
        order.setPayStatus(PayStatus.NO_PAY.getCode());
        order.setUserId(userId);
        order.setPayOrderId(orderId);
        order.setPayTime(0L);
        this.orderDomainRepository.saveOrder(order);

        List<SubjectModel> subjectList = this.orderDomainRepository.createSubjectDomain(null).findSubjectListByCourseId(courseId);
        OrderDisplayModel orderDisplayModel = new OrderDisplayModel();
        orderDisplayModel.setAmount(order.getOrderAmount());
        orderDisplayModel.setCourseName(course.getCourseName());
        orderDisplayModel.setOrderId(orderId);
        StringBuilder subject = new StringBuilder();
        for (SubjectModel subjectModel : subjectList){
            subject.append(subjectModel.getSubjectName()).append(",");
        }
        if (subject.toString().length() > 0){
            subject = new StringBuilder(subject.substring(0,subject.toString().length() - 1));
        }
        orderDisplayModel.setSubject(subject.toString());

        //16代表30分钟 延迟30m执行消费 3代表10秒  30分钟后延迟消费 未支付取消订单
        Map<String,String> map = new HashMap<>();
        map.put("orderId",order.getOrderId());
        map.put("time","1800");
        this.orderDomainRepository.mqService.sendDelayedMessage("create_order_delay",map,3);

        // 邀请信息的判断
        fillInvite(userId, order);
        return CallResult.success(orderDisplayModel);
    }

    private void fillInvite(Long userId, Order order) {
        HttpServletRequest request = this.orderParam.getRequest();
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return;
        }
        List<Map<String,String>> billTypeList = new ArrayList<>();
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            String[] inviteCookie = name.split("_i_ga_b_");
            if (inviteCookie.length == 2){
                Map<String,String> map = new HashMap<>();
                map.put("billType",inviteCookie[1]);
                map.put("userId",cookie.getValue());
                billTypeList.add(map);
            }
        }
        for (Map<String,String> inviteMap : billTypeList) {
            //有推荐信息，构建邀请信息
            Invite invite = new Invite();
            invite.setInviteInfo(order.getOrderId());
            invite.setInviteStatus(0);
            invite.setInviteTime(System.currentTimeMillis());
            invite.setInviteType(InviteType.LOGIN.getCode());
            invite.setInviteUserId(userId);
            invite.setUserId(Long.parseLong(AESUtils.decrypt(inviteMap.get("userId"))));
            invite.setBillType(inviteMap.get("billType"));
            invite.setCreateTime(System.currentTimeMillis());
            this.orderDomainRepository.createInviteDomain(null).save(invite);
        }
    }

    private BigDecimal checkCoupon(Long userId,Coupon coupon) {
        //检查优惠券是否可用，并返回优惠券的价格，订单生成的时候 需要将价格考虑进去
        //并且暂时标记 优惠券被使用，如果订单取消 可以将优惠券才还回去
        Long couponId = coupon.getId();
        CouponDomain couponDomain = this.orderDomainRepository.createCouponDomain(null);
        UserCoupon userCoupon = couponDomain.findUserCouponByUserId(userId,couponId);
        if (userCoupon == null){
            //条件不符合
            return BigDecimal.ZERO;
        }
        Long startTime = userCoupon.getStartTime();
        Long expireTime = userCoupon.getExpireTime();
        long currentTimeMillis = System.currentTimeMillis();
        if (expireTime != -1 && currentTimeMillis > expireTime){
            //过期了
            return BigDecimal.ZERO;
        }
        if (startTime != -1 && currentTimeMillis < startTime){
            //未到使用时间
            return BigDecimal.ZERO;
        }
        //标记为 已使用 未消费
        userCoupon.setStatus(4);
        couponDomain.updateCouponStatus(userCoupon);
        return coupon.getPrice();
    }

    public CallResult<Object> wxPay() {
        /**
         * 1. 获取到登录用户
         * 2. 根据订单号 查询订单 检查订单状态和支付状态
         * 3. 根据课程id 查询课程 确保课程的状态正常
         * 4. 组装微信支付需要的参数 发起微信的调用 微信会给我们返回对应的二维码链接
         * 5. 更改订单状态 为已提交
         */
        Long userId = UserThreadLocal.get();
        String orderId = this.orderParam.getOrderId();
        Order order = this.orderDomainRepository.findOrderByOrderId(orderId);
        if (order == null){
            return CallResult.fail(BusinessCodeEnum.ORDER_NOT_EXIST.getCode(),"订单不存在");
        }
        if (order.getOrderStatus().equals(OrderStatus.PAYED.getCode())){
            return CallResult.fail(BusinessCodeEnum.ORDER_AREADY_PAYED.getCode(),"订单已付款");
        }
        if (order.getPayStatus().equals(PayStatus.PAYED.getCode())){
            return CallResult.fail(BusinessCodeEnum.ORDER_AREADY_PAYED.getCode(),"订单已付款");
        }
        Integer payType = this.orderParam.getPayType();
        //用于测试
        order.setOrderAmount(BigDecimal.valueOf(0.01));
        order.setPayType(payType);
        Long courseId = order.getCourseId();
        Course course = this.orderDomainRepository.createCourseDomain(null).findCourseById(courseId);
        if (course == null) {
            return CallResult.fail(-999,"课程已被删除");
        }
        String payOrderId = System.currentTimeMillis() + String.valueOf(CommonUtils.random5Num()) + userId%10000;
        order.setPayOrderId(payOrderId);
        this.orderDomainRepository.updatePayOrderId(order);

        WxPayDomain wxPayDomain = new WxPayDomain(this.orderDomainRepository.wxPayConfiguration);
        WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
        orderRequest.setNotifyUrl(this.orderDomainRepository.wxPayConfiguration.wxNotifyUrl);
        orderRequest.setBody(course.getCourseName());
        orderRequest.setOutTradeNo(payOrderId);
        orderRequest.setProductId(String.valueOf(courseId));
        orderRequest.setTotalFee(BaseWxPayRequest.yuanToFen(String.valueOf(order.getOrderAmount().doubleValue())));//元转成分
        orderRequest.setSpbillCreateIp("182.92.102.161");
        orderRequest.setTradeType("NATIVE");
        orderRequest.setTimeStart(new DateTime(order.getCreateTime()).toString("yyyyMMddHHmmss"));
        try {
            WxPayNativeOrderResult wxPayNativeOrderResult = wxPayDomain.getWxPayService().createOrder(orderRequest);
            this.orderDomainRepository.updateOrderStatusAndPayType(order,OrderStatus.COMMIT.getCode());
            return CallResult.success(wxPayNativeOrderResult.getCodeUrl());
        } catch (WxPayException e) {
            e.printStackTrace();
            return CallResult.fail(BusinessCodeEnum.PAY_ORDER_CREATE_FAIL.getCode(),"create order fail");
        }
    }


    public CallResult<Object> notifyOrder(String xmlData) {
        /**
         * 1. 解析微信参数
         * 2. 根据payorderid进行订单查询
         * 3. 如果订单存在 订单更改为已支付
         * 4. 处理了交易的流水信息 做了一个保存
         */
        WxPayDomain wxPayDomain = new WxPayDomain(this.orderDomainRepository.wxPayConfiguration);
        try {
            WxPayOrderNotifyResult notifyResult  = wxPayDomain.getWxPayService().parseOrderNotifyResult(xmlData);
            String returnCode = notifyResult.getReturnCode();
            if ("SUCCESS".equals(returnCode)){
                log.info(JSON.toJSONString(notifyResult));
                String orderId = notifyResult.getOutTradeNo();
                // 微信方的交易订单号
                String transactionId = notifyResult.getTransactionId();
                Order order = this.orderDomainRepository.findOrderByPayOrderId(orderId);
                if (order == null){
                    return CallResult.fail(BusinessCodeEnum.ORDER_NOT_EXIST.getCode(),"order not exist");
                }
                if (order.getOrderStatus() == OrderStatus.PAYED.getCode() && order.getPayStatus()==PayStatus.PAYED.getCode()) {
                    // 代表订单已经处理过了 无需进行重复处理
                    return CallResult.success();
                }
                order.setOrderStatus(OrderStatus.PAYED.getCode());
                order.setPayStatus(PayStatus.PAYED.getCode());
                // 支付完成时间
//                String timeEnd = notifyResult.getTimeEnd();
                order.setPayTime(System.currentTimeMillis());
                this.orderDomainRepository.updateOrderStatusAndPayStatus(order);
                //添加支付信息
                OrderTrade orderTrade = this.orderDomainRepository.findOrderTrade(order.getOrderId());


                if (orderTrade != null){
                    // 以前添加过 更新
                    orderTrade.setPayInfo(JSON.toJSONString(notifyResult));
                    this.orderDomainRepository.updateOrderTrade(orderTrade);
                }else{
                    // 第一次添加流水
                    orderTrade = new OrderTrade();
                    orderTrade.setPayInfo(JSON.toJSONString(notifyResult));
                    orderTrade.setOrderId(order.getOrderId());
                    orderTrade.setUserId(order.getUserId());
                    orderTrade.setPayType(order.getPayType());
                    orderTrade.setTransactionId(transactionId);
                    this.orderDomainRepository.saveOrderTrade(orderTrade);
                }
                //添加课程
                this.orderDomainRepository.createUserCourseDomain(null).saveUserCourse(order);
                Long couponId = order.getCouponId();
                if (couponId > 0) {
                    UserCoupon userCoupon = this.orderDomainRepository.createCouponDomain(null).findUserCouponByUserId(order.getUserId(),couponId);
                    if (userCoupon != null){
                        userCoupon.setStatus(1);
                        this.orderDomainRepository.createCouponDomain(null).updateUserCoupon(userCoupon);
                    }
                }
                return CallResult.success();
            }
            log.error("notifyOrder error: {}",notifyResult.getReturnMsg());
            return CallResult.fail();
        } catch (WxPayException e) {
            e.printStackTrace();
            return CallResult.fail(BusinessCodeEnum.PAY_ORDER_CREATE_FAIL.getCode(), "微信支付信息处理失败");
        }
    }

    public CallResult<Object> findOrder() {
        String orderId = this.orderParam.getOrderId();
        if (StringUtils.isEmpty(orderId)){
            return CallResult.fail(BusinessCodeEnum.ORDER_NOT_EXIST.getCode(),BusinessCodeEnum.ORDER_NOT_EXIST.getMsg());
        }
        Order order = this.orderDomainRepository.findOrderByOrderId(orderId);
        if (order == null){
            return CallResult.fail(BusinessCodeEnum.ORDER_NOT_EXIST.getCode(),BusinessCodeEnum.ORDER_NOT_EXIST.getMsg());
        }
        OrderViewModel orderViewModel = new OrderViewModel();
        orderViewModel.setOrderId(order.getOrderId());
        CourseViewModel courseViewModel = this.orderDomainRepository.createCourseDomain(null).findCourseViewModel(order.getCourseId());
        orderViewModel.setCourse(courseViewModel);
        orderViewModel.setOAmount(order.getOrderAmount());
        orderViewModel.setOrderStatus(order.getOrderStatus());
        orderViewModel.setPayStatus(order.getPayStatus());
        orderViewModel.setPayType(order.getPayType());
        orderViewModel.setCreateTime(new DateTime(order.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"));
        orderViewModel.setExpireTime(new DateTime(order.getCreateTime() + order.getExpireTime()*24*60*60*1000).toString("yyyy-MM-dd HH:mm:ss"));
        Long couponId = order.getCouponId();
        if (couponId <= 0){
            orderViewModel.setCouponAmount(new BigDecimal(0));
        }else{
            Coupon coupon = this.orderDomainRepository.createCouponDomain(null).findCouponById(couponId);
            BigDecimal price = coupon.getPrice();
            orderViewModel.setCouponAmount(price);
        }
        return CallResult.success(orderViewModel);
    }

    public CallResult<Object> orderList() {
        int page = this.orderParam.getPage();
        int pageSize = this.orderParam.getPageSize();
        Long userId = UserThreadLocal.get();
        Page<Order> orderPage = this.orderDomainRepository.orderList(userId, OrderStatus.CANCEL.getCode(),page,pageSize);
        List<OrderViewModel> orderViewModelList = new ArrayList<>();
        for (Order order : orderPage.getRecords()){
            OrderViewModel orderViewModel = new OrderViewModel();
            orderViewModel.setOrderId(order.getOrderId());
            CourseViewModel courseViewModel = this.orderDomainRepository.createCourseDomain(null).findCourseViewModel(order.getCourseId());
            orderViewModel.setCourse(courseViewModel);
            orderViewModel.setOAmount(order.getOrderAmount());
            orderViewModel.setOrderStatus(order.getOrderStatus());
            orderViewModel.setPayStatus(order.getPayStatus());
            orderViewModel.setPayType(order.getPayType());
            orderViewModel.setCreateTime(new DateTime(order.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"));
            orderViewModel.setExpireTime(new DateTime(order.getCreateTime() + order.getExpireTime()*24*60*60*1000).toString("yyyy-MM-dd HH:mm:ss"));
            Long couponId = order.getCouponId();
            if (couponId <= 0){
                orderViewModel.setCouponAmount(new BigDecimal(0));
            }else{
                Coupon coupon = this.orderDomainRepository.createCouponDomain(null).findCouponById(couponId);
                BigDecimal price = coupon.getPrice();
                orderViewModel.setCouponAmount(price);
            }
            orderViewModelList.add(orderViewModel);
        }
        ListPageModel listPageModel = new ListPageModel();
        int total = (int) orderPage.getTotal();
        listPageModel.setSize(total);
        listPageModel.setPageCount(orderPage.getPages());
        listPageModel.setPage(page);
        listPageModel.setList(orderViewModelList);
        return CallResult.success(listPageModel);
    }
}
