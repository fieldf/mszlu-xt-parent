package com.mszlu.xt.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 默认扫包是当前包以及子包
@SpringBootApplication
public class SSOApp {
    public static void main(String[] args) {
        SpringApplication.run(SSOApp.class,args);
    }
}
