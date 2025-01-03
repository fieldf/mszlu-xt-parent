package com.mszlu.xt.pojo;

import lombok.Data;

@Data
public class UserHistory {
    private Long id;
    private Long userId;
    private Integer topicTotal;
    private Integer progress;
    private Long createTime;
    // [1,2,3,4]
    private String subjectUnits;
    private String topicPro;
    private Long subjectId;
    //1 未完成 2 已完成 3 已取消
    private Integer historyStatus;
    private Long finishTime;
    private Integer errorCount;
}
