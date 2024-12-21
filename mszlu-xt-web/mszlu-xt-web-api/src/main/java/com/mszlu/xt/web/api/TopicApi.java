package com.mszlu.xt.web.api;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.web.model.params.TopicParam;
import com.mszlu.xt.web.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("topic")
public class TopicApi {

    @Autowired
    private TopicService topicService;



    @RequestMapping(value = "practice",method = RequestMethod.POST)
    public CallResult practice(@RequestBody TopicParam topicParam){
        return topicService.practice(topicParam);
    }
}
