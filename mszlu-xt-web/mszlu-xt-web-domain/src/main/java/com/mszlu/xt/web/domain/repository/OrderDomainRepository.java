package com.mszlu.xt.web.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.common.wx.config.WxPayConfiguration;
import com.mszlu.xt.pojo.Order;
import com.mszlu.xt.pojo.OrderTrade;
import com.mszlu.xt.sso.model.params.InviteParam;
import com.mszlu.xt.web.dao.OrderMapper;
import com.mszlu.xt.web.dao.OrderTradeMapper;
import com.mszlu.xt.web.domain.*;
import com.mszlu.xt.web.domain.mq.MqService;
import com.mszlu.xt.web.model.params.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OrderDomainRepository {

    @Resource
    private OrderMapper orderMapper;
    @Autowired
    private CourseDomainRepository courseDomainRepository;
    @Autowired
    private CouponDomainRepository couponDomainRepository;
    @Autowired
    private SubjectDomainRepository subjectDomainRepository;

    @Autowired
    public MqService mqService;

    @Resource
    private OrderTradeMapper orderTradeMapper;

    public OrderDomain createDomain(OrderParam orderParam) {
        return new OrderDomain(this, orderParam);
    }

    public SubjectDomain createSubjectDomain(SubjectParam subjectParam) {
        return subjectDomainRepository.createDomain(subjectParam);
    }

    public CourseDomain createCourseDomain(CourseParam courseParam) {
        return courseDomainRepository.createDomain(courseParam);
    }

    public CouponDomain createCouponDomain(CouponParam couponParam) {
        return couponDomainRepository.createDomain(couponParam);
    }

    @Autowired
    private InviteDomainRepository inviteDomainRepository;

    public InviteDomain createInviteDomain(InviteParam inviteParam) {

        return inviteDomainRepository.createDomain(inviteParam);
    }

    public void saveOrder(Order order) {
        this.orderMapper.insert(order);
    }

    public Order findOrderByOrderId(String orderId) {
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Order::getOrderId, orderId);
        return this.orderMapper.selectOne(queryWrapper);
    }

    public Order findOrderByPayOrderId(String orderId) {
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Order::getPayOrderId, orderId);
        return this.orderMapper.selectOne(queryWrapper);
    }

    public boolean updateOrderStatus(Order order, int code) {
        LambdaUpdateWrapper<Order> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Order::getId, order.getId());
        updateWrapper.eq(Order::getOrderStatus, code);
        updateWrapper.set(Order::getOrderStatus, order.getOrderStatus());
        return this.orderMapper.update(null, updateWrapper) > 0;
    }

    @Autowired
    public WxPayConfiguration wxPayConfiguration;

    public void updatePayOrderId(Order order) {
        LambdaUpdateWrapper<Order> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Order::getId, order.getId());
        updateWrapper.set(Order::getPayOrderId, order.getPayOrderId());
        this.orderMapper.update(null, updateWrapper);
    }


    public boolean updateOrderStatusAndPayType(Order order, Integer updateOrderStatus) {
        LambdaUpdateWrapper<Order> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Order::getOrderId, order.getOrderId());
        //需要防止在修改的时候 被别的线程所修改
        updateWrapper.eq(Order::getOrderStatus, order.getOrderStatus());
        updateWrapper.set(Order::getOrderStatus, updateOrderStatus);
        updateWrapper.set(Order::getPayType, order.getPayType());
        int update = this.orderMapper.update(null, updateWrapper);
        return update > 0;
    }


    @Autowired
    private UserCourseDomainRepository userCourseDomainRepository;

    public void updateOrderTrade(OrderTrade orderTrade) {
        this.orderTradeMapper.updateById(orderTrade);
    }

    public void saveOrderTrade(OrderTrade orderTrade) {
        this.orderTradeMapper.insert(orderTrade);
    }

    public UserCourseDomain createUserCourseDomain(UserCourseParam userCourseParam) {
        return userCourseDomainRepository.createDomain(userCourseParam);
    }

    public void updateOrderStatusAndPayStatus(Order order) {
        LambdaUpdateWrapper<Order> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Order::getId, order.getId());
        updateWrapper.set(Order::getOrderStatus, order.getOrderStatus());
        updateWrapper.set(Order::getPayStatus, order.getPayStatus());
        updateWrapper.set(Order::getPayTime, order.getPayTime());
        this.orderMapper.update(null, updateWrapper);
    }

    public OrderTrade findOrderTrade(String orderId) {
        LambdaQueryWrapper<OrderTrade> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(OrderTrade::getOrderId, orderId);
        queryWrapper.last("limit 1");
        OrderTrade orderTrade = orderTradeMapper.selectOne(queryWrapper);
        return orderTrade;
    }

    public Page<Order> orderList(Long userId, int orderStatus, int currentPage, int pageSize) {
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Order::getUserId, userId);
        queryWrapper.ne(Order::getOrderStatus, orderStatus);
        Page<Order> page = new Page<>(currentPage, pageSize);
        return this.orderMapper.selectPage(page, queryWrapper);
    }
}
