package com.mszlu.xt.sso.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.mszlu.xt.common.constants.RedisKey;
import com.mszlu.xt.common.wx.config.WxOpenConfig;
import com.mszlu.xt.sso.dao.UserMapper;
import com.mszlu.xt.sso.dao.data.User;
import com.mszlu.xt.sso.domain.LoginDomain;
import com.mszlu.xt.sso.model.params.LoginParam;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class LoginDomainRepository {

    @Resource
    private UserMapper userMapper;

    @Autowired
    public WxMpService wxMpService;
    @Autowired
    private WxOpenConfig wxOpenConfig;

    @Autowired
    public StringRedisTemplate redisTemplate;

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

    public User findUserByUnionId(String unionId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // limit 1是对应的一个优化，查到数据就不再检索了
        queryWrapper.eq(User::getUnionId, unionId).last("limit 1");
        return userMapper.selectOne(queryWrapper);
    }

    public void saveUser(User user) {
        userMapper.insert(user);
    }
}
