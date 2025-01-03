package com.mszlu.xt.sso.domain.thread;

import com.mszlu.xt.sso.dao.mongo.data.UserLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LogThread {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Async("taskExecutor")
    public void send(UserLog userLog) {
//        try {
//            TimeUnit.SECONDS.sleep(5);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        try {
            rocketMQTemplate.convertAndSend("xt_log_sso_login",userLog);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("record-log:{}", userLog);
        }

        log.info("记录日志完成时间:{}", new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
    }
}
