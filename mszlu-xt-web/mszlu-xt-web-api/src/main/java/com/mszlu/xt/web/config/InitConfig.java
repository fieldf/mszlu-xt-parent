package com.mszlu.xt.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"com.mszlu.xt.common.service", "com.mszlu.xt.common.cache"})
public class InitConfig {
}
