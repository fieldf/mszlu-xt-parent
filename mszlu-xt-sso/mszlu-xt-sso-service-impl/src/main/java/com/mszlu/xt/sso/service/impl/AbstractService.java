package com.mszlu.xt.sso.service.impl;

import com.mszlu.xt.common.service.ServiceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public abstract class AbstractService {
    @Autowired
    protected ServiceTemplate serviceTemplate;
}
