package com.mszlu.xt.web.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mszlu.xt.pojo.UserProblem;
import com.mszlu.xt.web.dao.UserProblemMapper;
import com.mszlu.xt.web.domain.UserProblemDomain;
import com.mszlu.xt.web.model.params.UserProblemParam;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserProblemDomainRepository {

    @Resource
    private UserProblemMapper userProblemMapper;

    public UserProblemDomain createDomain(UserProblemParam userProblemParam) {
        return new UserProblemDomain(this,userProblemParam);
    }

    public UserProblem getUserProblem(Long userId, Long topicId) {
        LambdaQueryWrapper<UserProblem> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(UserProblem::getTopicId,topicId);
        queryWrapper.eq(UserProblem::getUserId,userId);
        queryWrapper.last("limit 1");
        return userProblemMapper.selectOne(queryWrapper);
    }

    public void save(UserProblem userProblem) {
        this.userProblemMapper.insert(userProblem);
    }

    public void updateUserProblemErrorCount(Long userId, Long topicId, String answer) {
        UpdateWrapper<UserProblem> update = Wrappers.update();
        update.eq("user_id",userId);
        update.eq("topic_id",topicId);
//        update.set("error_count","error_count+1"); 错误写法
        update.setSql(true, "error_count=error_count+1");
        update.set("error_answer", answer);
        userProblemMapper.update(null, update);
    }
}
