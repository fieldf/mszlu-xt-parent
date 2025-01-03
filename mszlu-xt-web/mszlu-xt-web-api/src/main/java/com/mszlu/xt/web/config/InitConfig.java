package com.mszlu.xt.web.config;

import com.mszlu.xt.common.cache.EnableCache;
import com.mszlu.xt.common.service.EnableService;
import com.mszlu.xt.common.wx.config.EnableWxPay;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ComponentScan({"com.mszlu.xt.common.service", "com.mszlu.xt.common.cache"})
@EnableCache
@EnableService
@EnableWxPay
public class InitConfig {
}
