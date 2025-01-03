package com.mszlu.xt.web.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mszlu.xt.pojo.UserPractice;
import com.mszlu.xt.web.dao.UserPracticeMapper;
import com.mszlu.xt.web.domain.UserPracticeDomain;
import com.mszlu.xt.web.model.params.UserPracticeParam;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class UserPracticeDomainRepository {

    @Resource
    private UserPracticeMapper userPracticeMapper;

    public UserPracticeDomain createDomain(UserPracticeParam userPracticeParam){
        return new UserPracticeDomain(this,userPracticeParam);
    }

    public void save(UserPractice userPractice) {
        userPracticeMapper.insert(userPractice);
    }

    public int countUserPracticeNumByStatus(Long userId,Long userHistoryId, int pStatus) {
        LambdaQueryWrapper<UserPractice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPractice::getHistoryId,userHistoryId);
        queryWrapper.eq(UserPractice::getUserId,userId);
        //2 正确答案
        queryWrapper.eq(UserPractice::getPStatus,pStatus);
        return userPracticeMapper.selectCount(queryWrapper);
    }

    public List<Map<String, Object>> findUserPracticeAnswerMap(Long userId, Long userHistoryId) {
        QueryWrapper<UserPractice> queryWrapper = Wrappers.query();
        queryWrapper.eq("history_id",userHistoryId);
        queryWrapper.eq("user_id",userId);
        queryWrapper.select("topic_id as topicId","p_status as pStatus","user_answer as userAnswer");
        return userPracticeMapper.selectMaps(queryWrapper);
    }

    public Long findUserPracticeTopic(Long userId, Long userHistoryId, Integer progress) {
        int pre = progress - 1;
        LambdaQueryWrapper<UserPractice> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(UserPractice::getUserId,userId);
        queryWrapper.eq(UserPractice::getHistoryId,userHistoryId);
        queryWrapper.select(UserPractice::getTopicId);
        queryWrapper.last("limit "+pre+",1");
        UserPractice userPractice = userPracticeMapper.selectOne(queryWrapper);
        return userPractice.getTopicId();
    }

    public UserPractice findUserPracticeByTopicId(Long userId, Long topicId, Long userHistoryId) {
        LambdaQueryWrapper<UserPractice> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(UserPractice::getUserId,userId);
        queryWrapper.eq(UserPractice::getHistoryId,userHistoryId);
        queryWrapper.eq(UserPractice::getTopicId,topicId);
        return userPracticeMapper.selectOne(queryWrapper);
    }

    public void updateUserPractice(Long userHistoryId, Long topicId, Long userId, String answer, int pStatus) {
        LambdaUpdateWrapper<UserPractice> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper
                .eq(UserPractice::getUserId,userId)
                .eq(UserPractice::getHistoryId,userHistoryId)
                .eq(UserPractice::getTopicId,topicId);
        updateWrapper.set(UserPractice::getUserAnswer,answer);
        // 1 错误 2 正确
        updateWrapper.set(UserPractice::getPStatus,pStatus);
        userPracticeMapper.update(null, updateWrapper);
    }
}
