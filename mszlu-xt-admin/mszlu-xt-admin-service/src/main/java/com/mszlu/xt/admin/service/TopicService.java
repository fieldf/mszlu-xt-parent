package com.mszlu.xt.admin.service;

import com.mszlu.xt.admin.params.TopicParam;
import com.mszlu.xt.common.model.CallResult;

public interface TopicService {
    CallResult findTopicList(TopicParam topicParam);
}
