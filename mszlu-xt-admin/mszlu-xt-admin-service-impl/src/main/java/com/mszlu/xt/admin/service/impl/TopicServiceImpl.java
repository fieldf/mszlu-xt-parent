package com.mszlu.xt.admin.service.impl;

import com.mszlu.xt.admin.domain.TopicDomain;
import com.mszlu.xt.admin.domain.repository.TopicDomainRepository;
import com.mszlu.xt.admin.model.TopicModel;
import com.mszlu.xt.admin.params.TopicParam;
import com.mszlu.xt.admin.service.TopicService;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.service.AbstractTemplateAction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class TopicServiceImpl extends AbstractService implements TopicService {
    @Resource
    private TopicDomainRepository topicDomainRepository;

    @Override
    public CallResult findTopicList(TopicParam topicParam) {
        TopicDomain topicDomain = topicDomainRepository.createDomain(topicParam);
        return serviceTemplate.executeQuery(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return topicDomain.findTopicList();
            }
        });
    }

    @Override
    public TopicModel findTopicByTitle(String topicTitle) {
        TopicParam topicParam = new TopicParam();
        topicParam.setTopicTitle(topicTitle);
        TopicDomain topicDomain = this.topicDomainRepository.createDomain(topicParam);
        return topicDomain.findTopicByTitle();
    }

    @Override
    @Transactional
    public CallResult updateTopic(TopicParam topicParam) {
        TopicDomain topicDomain = topicDomainRepository.createDomain(topicParam);
        return serviceTemplate.execute(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return topicDomain.updateTopic();
            }
        });
    }

    @Override
    @Transactional
    public CallResult saveTopic(TopicParam topicParam) {
        TopicDomain topicDomain = topicDomainRepository.createDomain(topicParam);
        return serviceTemplate.execute(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return topicDomain.saveTopic();
            }
        });
    }
}
