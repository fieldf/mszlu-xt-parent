package com.mszlu.xt.sso.domain;

import com.mszlu.xt.common.constants.RedisKey;
import com.mszlu.xt.common.model.BusinessCodeEnum;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.utils.AESUtils;
import com.mszlu.xt.common.utils.JwtUtil;
import com.mszlu.xt.pojo.Invite;
import com.mszlu.xt.sso.dao.data.User;
import com.mszlu.xt.sso.dao.mongo.data.UserLog;
import com.mszlu.xt.sso.domain.repository.LoginDomainRepository;
import com.mszlu.xt.sso.domain.thread.InviteThread;
import com.mszlu.xt.sso.model.enums.InviteType;
import com.mszlu.xt.sso.model.enums.LoginType;
import com.mszlu.xt.sso.model.params.LoginParam;
import com.mszlu.xt.sso.model.params.UserParam;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 专门处理和登录相关的操作
 */
public class LoginDomain {
    private LoginDomainRepository loginDomainRepository;

    private LoginParam loginParam;

    public static String secretKey = "mszlu!@#$%xtsso";

    public LoginDomain(LoginDomainRepository loginDomainRepository, LoginParam loginParam) {
        this.loginDomainRepository = loginDomainRepository;
        this.loginParam = loginParam;
    }

    public CallResult<Object> buildQrConnectUrl() {
        String url = loginDomainRepository.buildQrConnectUrl();
        return CallResult.success(url);
    }

    public CallResult<Object> checkWxLoginCallBackBiz() {
        // 主要是检查state是否是合法的
        // csrf的检测
        String state = loginParam.getState();
        // todo 去redis检测 是否state为key的值存在 如果不存在，说明不合法
        boolean isVerify = loginDomainRepository.checkState(state);
        if (!isVerify) {
            return CallResult.fail(BusinessCodeEnum.CHECK_BIZ_NO_RESULT.getCode(), "参数不合法");
        }
        return CallResult.success();
    }

    private User user;
    private boolean isNewer;

    public CallResult<Object> wxLoginCallBack() {
        String code = loginParam.getCode();

        try {
            // 2. 下次进行登录的时候 如果refreshToken存在 可以直接获取accessToken，不需要用户重新授权
//            String refreshToken = loginDomainRepository.redisTemplate.opsForValue().get(RedisKey.REFRESH_TOKEN);
            String refreshToken = null;
            WxMpOAuth2AccessToken wxMpOAuth2AccessToken = null;
            if (refreshToken == null) {
                // 1. 通过code获取accessToken和refreshToken，
                wxMpOAuth2AccessToken = loginDomainRepository.wxMpService.oauth2getAccessToken(code);

                refreshToken = wxMpOAuth2AccessToken.getRefreshToken();
                String unionId = wxMpOAuth2AccessToken.getUnionId();
                // 需要保存refreshToken 保存在redis中 过期时间设置为28天
                loginDomainRepository.redisTemplate.opsForValue().set(RedisKey.REFRESH_TOKEN + unionId, refreshToken, 28, TimeUnit.DAYS);
            } else {
                wxMpOAuth2AccessToken = loginDomainRepository.wxMpService.oauth2refreshAccessToken(refreshToken);
            }

            // 3. 通过accessToken获取到微信的用户信息（openId和unionId）unionId在web端 公众号端 手机端唯一
            WxMpUser wxMpUser = loginDomainRepository.wxMpService.oauth2getUserInfo(wxMpOAuth2AccessToken, "zh_CN");
            String unionId = wxMpUser.getUnionId();

            // 4. 需要判断unionId在数据库中的user表中是否存在 存在就更新 最后登录时间 不存在就注册
            User user = loginDomainRepository.createUserDomain(new UserParam()).findUserByUnionId(unionId);
            boolean isNew = false;
            if (user == null) {
                // 注册
                user = new User();
                Long currentTime = System.currentTimeMillis();
                user.setNickname(wxMpUser.getNickname());
                user.setHeadImageUrl(wxMpUser.getHeadImgUrl());
                user.setSex(wxMpUser.getSex());
                user.setOpenid(wxMpUser.getOpenId());
                user.setLoginType(LoginType.WX.getCode());
                user.setCountry(wxMpUser.getCountry());
                user.setCity(wxMpUser.getCity());
                user.setProvince(wxMpUser.getProvince());
                user.setRegisterTime(currentTime);
                user.setLastLoginTime(currentTime);
                user.setUnionId(wxMpUser.getUnionId());
                user.setArea("");
                user.setMobile("");
                user.setGrade("");
                user.setName(wxMpUser.getNickname());
                user.setSchool("");
                loginDomainRepository.createUserDomain(new UserParam()).saveUser(user);
                isNew = true;

                fillInvite(user);
            }
            // 5. jwt技术 生成token 需要把token存储起来
            String token = JwtUtil.createJWT(7 * 24 * 60 * 60 * 1000, user.getId(), secretKey);
            loginDomainRepository.redisTemplate.opsForValue().set(RedisKey.TOKEN + token, String.valueOf(user.getId()), 7, TimeUnit.DAYS);

            // 6. 因为付费课程 所以账号只能在一端登录 如果用户在其他地方登录 需要将当前的登录用户踢下线
            String oldToken = loginDomainRepository.redisTemplate.opsForValue().get(RedisKey.LOGIN_USER_TOKEN + user.getId());
            if (oldToken != null) {
                // 当前用户之前在某一个设备登陆过
                // 在用户登录验证的时候需要先验证token是否合法，然后去redis查询是否存在token 不存在代表不合法
                loginDomainRepository.redisTemplate.delete(RedisKey.TOKEN + oldToken);
            }
            loginDomainRepository.redisTemplate.opsForValue().set(RedisKey.LOGIN_USER_TOKEN + user.getId(), token);

            // 7. 返回给前端token 存在cookie当中 下次请求的时候 从cookie中获取token
            HttpServletResponse response = loginParam.getResponse();
            Cookie cookie = new Cookie("t_token", token);
            cookie.setMaxAge(8*24*3600);
            cookie.setPath("/");
            response.addCookie(cookie);
            // 8. 比如给用户 加积分，成就系统，任务系统
            // 9. 需要记录日志，记录当前用户的登录行为 MQ+mongo进行日志记录

            // 10. 更新用户的最后登录时间
            if (!isNew) {
                user.setLastLoginTime(System.currentTimeMillis());
                this.loginDomainRepository.createUserDomain(new UserParam()).updateUser(user);
            }
            this.isNewer = isNew;
            this.user = user;
            return CallResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            return CallResult.fail(BusinessCodeEnum.LOGIN_WX_NOT_USER_INFO.getCode(), "授权问题,无法获取用户信息");
        }
    }

    /**
     * 邀请信息
     * @param user
     */
    private void fillInvite(User user) {
        HttpServletRequest request = this.loginParam.getRequest();
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

        this.loginDomainRepository.inviteThread.fillInvite(billTypeList, user);
    }

    public void wxLoginCallBackFinishUp(CallResult<Object> callResult) {
        // 记录日志
        UserLog userLog = new UserLog();
        userLog.setUserId(user.getId());
        userLog.setNewer(isNewer);
        userLog.setSex(user.getSex());
        userLog.setLastLoginTime(user.getLastLoginTime());
        userLog.setRegisterTime(user.getRegisterTime());
        // 同步操作 一旦代码出现异常 就影响了登录功能
        // rocketmq挂掉 这是一个风险 不是代码bug

        this.loginDomainRepository.recordLoginLog(userLog);
    }

}
