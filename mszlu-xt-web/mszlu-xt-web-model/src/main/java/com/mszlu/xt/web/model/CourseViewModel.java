package com.mszlu.xt.web.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseViewModel {
    private Long id;
    private String courseName;
    private String courseDesc;
    private BigDecimal coursePrice;
    private BigDecimal courseZhePrice;
    private Integer orderTime;
    private Integer studyCount;
    private List<Long> subjectIdList;
    private List<SubjectModel> subjectList;
    private SubjectModel subjectInfo;
    //0 未购买 1 已购买
    private Integer buy;
    private String expireTime;
    private String imageUrl;

    //用户名称
    private String userName;




}
