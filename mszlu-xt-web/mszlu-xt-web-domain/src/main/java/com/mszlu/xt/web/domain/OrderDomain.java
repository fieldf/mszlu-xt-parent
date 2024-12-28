package com.mszlu.xt.web.domain;

import com.mszlu.xt.common.login.UserThreadLocal;
import com.mszlu.xt.common.model.BusinessCodeEnum;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.utils.CommonUtils;
import com.mszlu.xt.pojo.*;
import com.mszlu.xt.web.domain.repository.OrderDomainRepository;
import com.mszlu.xt.web.model.OrderDisplayModel;
import com.mszlu.xt.web.model.SubjectModel;
import com.mszlu.xt.web.model.enums.OrderStatus;
import com.mszlu.xt.web.model.enums.PayStatus;
import com.mszlu.xt.web.model.enums.PayType;
import com.mszlu.xt.web.model.params.OrderParam;

import java.math.BigDecimal;
import java.util.List;

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
        return CallResult.success(orderDisplayModel);
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
}
