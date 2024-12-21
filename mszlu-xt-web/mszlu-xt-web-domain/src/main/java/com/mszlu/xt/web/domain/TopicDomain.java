package com.mszlu.xt.web.domain;

import com.alibaba.fastjson.JSON;
import com.mszlu.xt.common.enums.TopicType;
import com.mszlu.xt.common.login.UserThreadLocal;
import com.mszlu.xt.common.model.BusinessCodeEnum;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.model.topic.ContentAndImage;
import com.mszlu.xt.common.model.topic.FillBlankChoice;
import com.mszlu.xt.pojo.UserHistory;
import com.mszlu.xt.pojo.UserPractice;
import com.mszlu.xt.web.dao.data.TopicDTO;
import com.mszlu.xt.web.domain.repository.TopicDomainRepository;
import com.mszlu.xt.web.model.PracticeDetailModel;
import com.mszlu.xt.web.model.SubjectModel;
import com.mszlu.xt.web.model.TopicModelView;
import com.mszlu.xt.web.model.enums.HistoryStatus;
import com.mszlu.xt.web.model.params.TopicParam;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TopicDomain {
    private TopicDomainRepository topicDomainRepository;
    private TopicParam topicParam;
    public TopicDomain(TopicDomainRepository topicDomainRepository, TopicParam topicParam) {
        this.topicDomainRepository = topicDomainRepository;
        this.topicParam = topicParam;
    }

    public CallResult<Object> checkPracticeBiz() {
        /**
         * 1. 传过来的subjectId 对应课程到底此用户购买了没
         */
        Long userId = UserThreadLocal.get();
        Long subjectId = this.topicParam.getSubjectId();

        // 根据subjectId查询课程
        List<Long> courseIdList = this.topicDomainRepository.createCourseDomain(null).findCourseIdListBySubjectId(subjectId);
        if (CollectionUtils.isEmpty(courseIdList)) {
            return CallResult.fail(BusinessCodeEnum.COURSE_NO_BUY.getCode());
        }

        // 查询用户是否购买过课程
        Integer courseCount = this.topicDomainRepository.createUserCourseDomain(null).countUserCourseInCourseIdList(userId, courseIdList, System.currentTimeMillis());
        if (courseCount <= 0) {
            return CallResult.fail(BusinessCodeEnum.COURSE_NO_BUY.getCode(), "此课程还没购买或者已过期");
        }

        return CallResult.success();
    }

    public CallResult<Object> practice() {
        /**
         * 1. 判断 此用户 此学科 之前是否已经练习过 UserHistory有没有
         * 2. 如果没有 新的练习 开始一个新的练习
         * 3. 如果有 拿到之前的练习 返回对应的练习进度
         */
        Long userId = UserThreadLocal.get();
        Long subjectId = this.topicParam.getSubjectId();
        // 从课程立即学习进入 practiceId不穿 但是从我的学习模块进入 会传递当前的练习id(学习记录id)
        Long practiceId = this.topicParam.getPracticeId();
        UserHistory userHistory = null;
        if (practiceId == null) {
            userHistory = this.topicDomainRepository.createUserHistoryDomain(null).findUserHistory(userId, subjectId, HistoryStatus.NO_FINISH.getCode());
        } else {
            userHistory = this.topicDomainRepository.createUserHistoryDomain(null).findUserHistoryById(practiceId);
        }
        if (userHistory == null) {
            // 开始一个新的学习
            return startNewStudy(subjectId, userId);
        }
        // 已经有练习了 根据之前的练习进行显示
        if (userHistory.getHistoryStatus() == 3){
            return CallResult.fail(BusinessCodeEnum.PRACTICE_CANCEL.getCode(),BusinessCodeEnum.PRACTICE_CANCEL.getMsg());
        }
        //已有的练习题，开始练习
        Integer progress = userHistory.getProgress();
        Long topicId = this.topicDomainRepository.createUserPracticeDomain(null).findUserPractice(userId,userHistory.getId(), progress);
        if (topicId == null){
            return CallResult.fail();
        }
        System.out.println("已有练习题："+topicId);
        TopicDTO topic = this.topicDomainRepository.findTopicAnswer(topicId, userId, userHistory.getId());
        PracticeDetailModel practiceModel = new PracticeDetailModel();
        practiceModel.setProgress(progress);
        practiceModel.setTotal(userHistory.getTopicTotal());
        practiceModel.setTopic(getTopicModelView(topic));
        practiceModel.setPracticeId(userHistory.getId());
        practiceModel.setPracticeId(userHistory.getId());
        int answered = userHistory.getProgress();
        if (answered != userHistory.getTopicTotal()){
            answered = userHistory.getProgress()-1;
        }
        practiceModel.setAnswered(answered);
        int trueNum = this.topicDomainRepository.createUserPracticeDomain(null).countUserPracticeTrueNum(userId,userHistory.getId());
        int wrongNum = this.topicDomainRepository.createUserPracticeDomain(null).countUserPracticeWrongNum(userId,userHistory.getId());
        practiceModel.setTrueNum(trueNum);
        practiceModel.setWrongNum(wrongNum);
        practiceModel.setNoAnswer(0);
        SubjectModel subject = this.topicDomainRepository.createSubjectDomain(null).findSubject(userHistory.getSubjectId());
        practiceModel.setSubjectName(subject.getSubjectName()+" "+subject.getSubjectGrade()+" "+subject.getSubjectTerm());
        List<Integer> subjectUnitList1 = JSON.parseArray(userHistory.getSubjectUnits(), Integer.class);
        practiceModel.setSubjectUnitList(subjectUnitList1);
        practiceModel.setCreateTime(new DateTime(userHistory.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"));
        practiceModel.setFinishTime(userHistory.getFinishTime() == 0 ? "":new DateTime(userHistory.getFinishTime()).toString("yyyy-MM-dd HH:mm:ss"));
        practiceModel.setUseTime(userHistory.getFinishTime() == 0 ? "":useTime(userHistory.getFinishTime(),userHistory.getCreateTime()));
        List<Map<String,Object>> topicAnswerStatusList = this.topicDomainRepository.createUserPracticeDomain(null).findUserPracticeAll(userId,userHistory.getId());
        practiceModel.setTopicAnswerStatusList(topicAnswerStatusList);
        return CallResult.success(practiceModel);
    }

    private CallResult<Object> startNewStudy(Long subjectId, Long userId) {
        /**
         * 1. 根据对应的学科id和传递的学科对应的单元列表 进行习题的随机
         * 2. 随机50道题
         * 3. 生成 UserHistory 学习记录 进度 1
         * 4. 生成UserPractice此用户 学习记录id 题目id 完成状态 答案
         * 5. 返回生成题目的第一道题
         */
        // 随机50道题
        List<Integer> subjectUnitList = this.topicParam.getSubjectUnitList();
        if (subjectUnitList == null) {
            subjectUnitList = new ArrayList<>();
        }
        List<Long> topicIdList = this.topicDomainRepository.findTopicRandom(subjectId, subjectUnitList);
        if (topicIdList.size()<=0) {
            return CallResult.fail(BusinessCodeEnum.TOPIC_NO_PRACTICE.getCode(), "没有习题");
        }
        // 保留参数 理论应该不传 web端未实现
        String topicAreaPro = topicParam.getTopicAreaPro();
        UserHistory userHistory = new UserHistory();
        userHistory.setCreateTime(System.currentTimeMillis());
        userHistory.setHistoryStatus(HistoryStatus.NO_FINISH.getCode());
        userHistory.setSubjectId(subjectId);
        userHistory.setProgress(1);
        userHistory.setSubjectUnits(JSON.toJSONString(subjectUnitList));
        userHistory.setTopicTotal(topicIdList.size());
        if (StringUtils.isEmpty(topicAreaPro)){
            topicAreaPro = "全国";
        }
        userHistory.setTopicPro(topicAreaPro);
        userHistory.setUserId(userId);
        userHistory.setFinishTime(0L);
        userHistory.setErrorCount(0);
        this.topicDomainRepository.createUserHistoryDomain(null).saveUserHistory(userHistory);

        for (Long topicId : topicIdList) {
            // 生成 练习 用户回答状态详情
            UserPractice userPractice = new UserPractice();
            userPractice.setHistoryId(userHistory.getId());
            userPractice.setPStatus(0);
            userPractice.setTopicId(topicId);
            userPractice.setUserAnswer("");
            userPractice.setUserId(userId);
            this.topicDomainRepository.createUserPracticeDomain(null).saveUserPractice(userPractice);
        }

        TopicDTO topic = this.topicDomainRepository.findTopicAnswer(topicIdList.get(0), userId, userHistory.getId());

        PracticeDetailModel practiceModel = new PracticeDetailModel();
        practiceModel.setTotal(userHistory.getTopicTotal());
        practiceModel.setPracticeId(userHistory.getId());
        int answered = userHistory.getProgress();
        if (answered != userHistory.getTopicTotal()){
            answered = userHistory.getProgress()-1;
        }
        practiceModel.setAnswered(answered);
        practiceModel.setPracticeId(userHistory.getId());
        int trueNum = this.topicDomainRepository.createUserPracticeDomain(null).countUserPracticeTrueNum(userId, userHistory.getId());
        int wrongNum = this.topicDomainRepository.createUserPracticeDomain(null).countUserPracticeWrongNum(userId, userHistory.getId());
        practiceModel.setTrueNum(trueNum);
        practiceModel.setWrongNum(wrongNum);
        practiceModel.setNoAnswer(0);
        SubjectModel subject = this.topicDomainRepository.createSubjectDomain(null).findSubject(userHistory.getSubjectId());
        practiceModel.setSubjectName(subject.getSubjectName()+" "+subject.getSubjectGrade()+" "+subject.getSubjectTerm());
//        List<Integer> subjectUnitList1 = JSON.parseArray(userHistory.getSubjectUnits(), Integer.class);
        practiceModel.setSubjectUnitList(subjectUnitList);
        practiceModel.setCreateTime(new DateTime(userHistory.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"));
        practiceModel.setFinishTime(userHistory.getFinishTime() == 0 ? "":new DateTime(userHistory.getFinishTime()).toString("yyyy-MM-dd HH:mm:ss"));
        practiceModel.setUseTime(userHistory.getFinishTime() == 0 ? "":useTime(userHistory.getFinishTime(),userHistory.getCreateTime()));
        practiceModel.setTopic(getTopicModelView(topic));
        practiceModel.setProgress(1);
        List<Map<String,Object>> topicAnswerStatusList = this.topicDomainRepository.createUserPracticeDomain(null).findUserPracticeAll(userId,userHistory.getId());
        practiceModel.setTopicAnswerStatusList(topicAnswerStatusList);
        return CallResult.success(practiceModel);
    }

    private TopicModelView copyView(TopicDTO topic){
        TopicModelView topicModel = new TopicModelView();
        if (topic == null){
            return null;
        }
        BeanUtils.copyProperties(topic,topicModel);
        return topicModel;
    }

    private TopicModelView getTopicModelView(TopicDTO topic) {
        TopicModelView topicModel = copyView(topic);
        if (topic == null){
            return null;
        }
        String topicImg = topic.getTopicImg();
        if (!StringUtils.isEmpty(topicImg)){
            List<String> topicImgList = JSON.parseArray(topicImg, String.class);
            topicModel.setTopicImgList(topicImgList);
        }
        if (topic.getTopicType() == TopicType.FILL_BLANK.getCode()){
            List<FillBlankChoice> fillBlankChoiceList = JSON.parseArray(topic.getTopicChoice(), FillBlankChoice.class);
            topicModel.setFillBlankTopicChoice(fillBlankChoiceList.size());
            topicModel.setFillBlankAnswer(fillBlankChoiceList);
            topicModel.setAnswer(null);
            String userAnswer = topic.getUserAnswer();
            if (!StringUtils.isEmpty(userAnswer)){
                String[] split = userAnswer.split("\\$#\\$");
                String sss = "";
                for (String s : split) {
                    sss += s + "  ";
                }
                topicModel.setUserAnswer(sss);
            }
        }
        if (topic.getTopicType() == TopicType.RADIO.getCode()){
            List<Map<String, ContentAndImage>> list = JSON.parseObject(topic.getTopicChoice(), new com.alibaba.fastjson.TypeReference<List<Map<String,ContentAndImage>>>(){});
            topicModel.setRadioChoice(list);
        }
        if (topic.getTopicType() == TopicType.MUL_CHOICE.getCode()){
            List<Map<String,ContentAndImage>> list = JSON.parseObject(topic.getTopicChoice(), new com.alibaba.fastjson.TypeReference<List<Map<String,ContentAndImage>>>(){});
            topicModel.setMulChoice(list);
        }

        topicModel.setLastUpdateTime(new DateTime(topic.getLastUpdateTime()).toString("yyyy-MM-dd"));
        return topicModel;
    }

    private String useTime(Long finishTime, Long createTime) {

        long diff = finishTime - createTime;
        long dayTime = 24 * 60 * 60 * 1000;
        String useTime = "";
        long day = diff / dayTime;
        if (day > 0){
            useTime = day +"天";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(finishTime);
        int finishHour = calendar.get(Calendar.HOUR_OF_DAY);
        int finishMinute = calendar.get(Calendar.MINUTE);
        int finishSecond = calendar.get(Calendar.SECOND);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(createTime);
        int createHour = calendar1.get(Calendar.HOUR_OF_DAY);
        int createMinute = calendar1.get(Calendar.MINUTE);
        int createSecond = calendar1.get(Calendar.SECOND);
        int diffHour = finishHour - createHour;
        if (diffHour < 0){
            diffHour = -diffHour;
        }
        if (diffHour < 10){
            useTime += "0"+diffHour +":";
        }else {
            useTime += diffHour + ":";
        }
        int diffMinute = finishMinute - createMinute;
        if (diffMinute < 0){
            diffMinute = -diffMinute;
        }
        if (diffMinute < 10){
            useTime += "0"+diffMinute + ":";
        }else {
            useTime += diffMinute + ":";
        }
        int diffSecond = finishSecond - createSecond;
        if (diffSecond < 0){
            diffSecond = -diffSecond;
        }
        if (diffSecond < 10){
            useTime += "0"+ diffSecond;
        }else {
            useTime += String.valueOf(diffSecond);
        }
        return useTime;
    }
}
