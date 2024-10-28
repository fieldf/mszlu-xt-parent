package com.mszlu.xt.admin.service;

import com.mszlu.xt.admin.params.NewsParam;
import com.mszlu.xt.common.model.CallResult;

public interface NewsService {
    CallResult findPage(NewsParam newsParam);
}
