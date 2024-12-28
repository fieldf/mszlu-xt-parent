package com.mszlu.xt.web.domain;

import com.mszlu.xt.pojo.Coupon;
import com.mszlu.xt.pojo.Course;
import com.mszlu.xt.pojo.UserCoupon;
import com.mszlu.xt.web.domain.repository.CouponDomainRepository;
import com.mszlu.xt.web.model.UserCouponModel;
import com.mszlu.xt.web.model.params.CouponParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CouponDomain {

    private CouponDomainRepository couponDomainRepository;
    private CouponParam couponParam;
    public CouponDomain(CouponDomainRepository couponDomainRepository, CouponParam couponParam) {
        this.couponDomainRepository = couponDomainRepository;
        this.couponParam =couponParam;
    }

    public List<UserCouponModel> findUserCoupon(Long userId,Long courseId) {
        List<UserCoupon> userCouponList = couponDomainRepository.findUserCouponByUserId(userId);
        List<UserCouponModel> userCouponModelList = new ArrayList<>();
        for (UserCoupon userCoupon : userCouponList) {
            Long startTime = userCoupon.getStartTime();
            Long expireTime = userCoupon.getExpireTime();
            long currentTimeMillis = System.currentTimeMillis();
            if (startTime != -1 && currentTimeMillis < startTime){
                continue;
            }
            if (expireTime != -1 && currentTimeMillis > expireTime){
                continue;
            }
            Long couponId = userCoupon.getCouponId();
            Coupon coupon = couponDomainRepository.findCouponById(couponId);
            Integer disStatus = coupon.getDisStatus();
            if (disStatus == 1){
                //需要满足满减条件
                Course course = this.couponDomainRepository.createCourseDomain(null).findCourseById(courseId);
                BigDecimal courseZhePrice = course.getCourseZhePrice();
                if (coupon.getMax().compareTo(courseZhePrice) > 0){
                    //最大满减 大于课程价格 代表不可使用
                    continue;
                }
            }
            UserCouponModel userCouponModel = new UserCouponModel();
            userCouponModel.setAmount(coupon.getPrice());
            userCouponModel.setCouponId(couponId);
            userCouponModel.setName(coupon.getName());
            userCouponModelList.add(userCouponModel);

        }
        return userCouponModelList;
    }

    public UserCoupon findUserCouponByUserId(Long userId, Long couponId) {
        return this.couponDomainRepository.findUserCouponByUserIdAndCouponId(userId,couponId);
    }

    public void updateCouponStatus(UserCoupon userCoupon) {
        this.couponDomainRepository.updateCouponStatus(userCoupon);
    }

    public Coupon findCouponById(Long couponId) {
        return couponDomainRepository.findCouponById(couponId);
    }

    public void updateUserCoupon(UserCoupon userCoupon) {
        this.couponDomainRepository.updateCouponStatus(userCoupon);
    }
}
