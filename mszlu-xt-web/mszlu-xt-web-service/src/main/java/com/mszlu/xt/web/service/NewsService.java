package com.mszlu.xt.web.service;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.web.model.params.NewsParam;

public interface NewsService {
    /**
     * 分页查询新闻列表
     * @param newsParam
     * @return
     */
    CallResult newsList(NewsParam newsParam);

    CallResult findNewsById(NewsParam newsParam);


    CallResult newsDetailList(NewsParam newsParam);
}
