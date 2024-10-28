package com.mszlu.xt.admin.qiniu;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class QiniuConfig {

    @Value("${qiniu.file.server.url}")
    private String fileServerUrl;
}
