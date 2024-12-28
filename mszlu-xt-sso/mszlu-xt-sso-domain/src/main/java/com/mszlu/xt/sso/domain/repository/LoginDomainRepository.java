package com.mszlu.xt.sso.domain.repository;

import com.mszlu.xt.common.constants.RedisKey;
import com.mszlu.xt.common.wx.config.WxOpenConfig;
import com.mszlu.xt.sso.dao.UserMapper;
import com.mszlu.xt.sso.dao.mongo.data.UserLog;
import com.mszlu.xt.sso.domain.LoginDomain;
import com.mszlu.xt.sso.domain.UserDomain;
import com.mszlu.xt.sso.domain.thread.InviteThread;
import com.mszlu.xt.sso.domain.thread.LogThread;
import com.mszlu.xt.sso.model.params.LoginParam;
import com.mszlu.xt.sso.model.params.UserParam;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LoginDomainRepository {

    @Resource
    private UserMapper userMapper;

    @Autowired
    public WxMpService wxMpService;
    @Autowired
    private WxOpenConfig wxOpenConfig;

    @Autowired
    public StringRedisTemplate redisTemplate;

    @Autowired
    private UserDomainRepository userDomainRepository;

    @Autowired
    public InviteThread inviteThread;


    public LoginDomain createDomain(LoginParam loginParam) {
        return new LoginDomain(this, loginParam);
    }

    public boolean checkState(String state) {
        Boolean isValid = redisTemplate.hasKey(RedisKey.WX_STATE_KEY + state);
        return isValid != null && isValid;
    }

    public String buildQrConnectUrl() {
        //        String csrfKey = wxOpenConfig.getCsrfKey();
//        String time = new DateTime().toString("yyyyMMddHHmmss");
//        csrfKey = csrfKey + "_" + time;
        String csrfKey = UUID.randomUUID().toString();
        // todo 把csrfkey放入redis中，并设置有效期
        redisTemplate.opsForValue().set(RedisKey.WX_STATE_KEY + csrfKey, "1", 60, TimeUnit.SECONDS);

        // 会用到第三方的工具
        return wxMpService.buildQrConnectUrl(wxOpenConfig.getRedirectUrl(), wxOpenConfig.getScope(), csrfKey);
    }

    public UserDomain createUserDomain(UserParam userParam) {
        return userDomainRepository.createDomain(userParam);
    }

    @Autowired
    private LogThread logThread;

    public void recordLoginLog(UserLog userLog) {
        // 为了避免出现问题 这个地方可以改为异步发送 不采用 异步 性能高 但是会丢数据
        // 不希望丢数据 同步的方式
        log.info("记录日志开始时间:{}", new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        logThread.send(userLog);

    }
}
