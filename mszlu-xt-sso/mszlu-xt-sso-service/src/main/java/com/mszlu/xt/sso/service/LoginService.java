package com.mszlu.xt.sso.service;

import com.mszlu.xt.common.model.CallResult;

public interface LoginService {
    /**
     * 获取微信扫码的二维码链接
     * @return
     */
    CallResult getQRCodeUrl();
}
