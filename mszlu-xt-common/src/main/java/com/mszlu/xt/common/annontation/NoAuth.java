package com.mszlu.xt.common.annontation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

//注解的意义，需要登录信息，但是如果未登录，无需拦截
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoAuth {
}
