package com.mszlu.xt.web.service;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.web.model.params.BillParam;

public interface BillService {
    CallResult gen(BillParam billParam);

    CallResult all(BillParam billParam);
}
