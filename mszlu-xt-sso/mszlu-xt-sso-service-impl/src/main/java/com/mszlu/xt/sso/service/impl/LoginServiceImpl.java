package com.mszlu.xt.sso.service.impl;

import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.wx.config.WxOpenConfig;
import com.mszlu.xt.sso.service.LoginService;
import me.chanjar.weixin.mp.api.WxMpService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private WxOpenConfig wxOpenConfig;

    @Override
    public CallResult getQRCodeUrl() {
        String csrfKey = wxOpenConfig.getCsrfKey();
        String time = new DateTime().toString("yyyyMMddHHmmss");
        csrfKey = csrfKey + "_" +time;
        // 会用到第三方的工具
        String url = wxMpService.buildQrConnectUrl(wxOpenConfig.getRedirectUrl(), wxOpenConfig.getScope(), csrfKey);

        return CallResult.success(url);
    }
}
