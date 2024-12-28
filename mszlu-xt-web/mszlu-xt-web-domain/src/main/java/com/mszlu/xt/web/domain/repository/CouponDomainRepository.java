package com.mszlu.xt.web.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mszlu.xt.pojo.Coupon;
import com.mszlu.xt.pojo.UserCoupon;
import com.mszlu.xt.web.dao.CouponMapper;
import com.mszlu.xt.web.dao.UserCouponMapper;
import com.mszlu.xt.web.domain.CouponDomain;
import com.mszlu.xt.web.domain.CourseDomain;
import com.mszlu.xt.web.model.params.CouponParam;
import com.mszlu.xt.web.model.params.CourseParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class CouponDomainRepository {

    @Resource
    private UserCouponMapper userCouponMapper;
    @Resource
    private CouponMapper couponMapper;
    @Autowired
    private CourseDomainRepository courseDomainRepository;

    public CouponDomain createDomain(CouponParam couponParam) {
        return new CouponDomain(this,couponParam);
    }

    public List<UserCoupon> findUserCouponByUserId(Long userId) {
        LambdaQueryWrapper<UserCoupon> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(UserCoupon::getUserId,userId);
        //0代表 未使用 应该做成枚举
        queryWrapper.eq(UserCoupon::getStatus,0);
        List<UserCoupon> userCouponList = userCouponMapper.selectList(queryWrapper);
        return userCouponList;
    }

    public Coupon findCouponById(Long couponId) {
        return couponMapper.selectById(couponId);
    }

    public CourseDomain createCourseDomain(CourseParam courseParam) {
        return courseDomainRepository.createDomain(courseParam);
    }

    public UserCoupon findUserCouponByUserIdAndCouponId(Long userId, Long couponId) {
        LambdaQueryWrapper<UserCoupon> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(UserCoupon::getUserId,userId);
        queryWrapper.eq(UserCoupon::getCouponId,couponId);
        //0代表 未使用 应该做成枚举
        queryWrapper.eq(UserCoupon::getStatus,0);
        queryWrapper.last("limit 1");
        UserCoupon userCoupon = userCouponMapper.selectOne(queryWrapper);
        return userCoupon;
    }

    public void updateCouponStatus(UserCoupon userCoupon) {
        LambdaUpdateWrapper<UserCoupon> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(UserCoupon::getId,userCoupon.getId());
        updateWrapper.set(UserCoupon::getStatus,userCoupon.getStatus());
        userCouponMapper.update(null, updateWrapper);
    }
}
