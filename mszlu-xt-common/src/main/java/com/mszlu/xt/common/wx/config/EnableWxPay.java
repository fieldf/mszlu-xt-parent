package com.mszlu.xt.common.wx.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(WxPayConfiguration.class)
public @interface EnableWxPay {
}
