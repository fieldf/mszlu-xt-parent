package com.mszlu.xt.admin.service.impl;

import com.mszlu.xt.admin.domain.NewsDomain;
import com.mszlu.xt.admin.domain.repository.NewsDomainRepository;
import com.mszlu.xt.admin.params.NewsParam;
import com.mszlu.xt.admin.service.NewsService;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.service.AbstractTemplateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NewsServiceImpl extends AbstractService implements NewsService {
    @Autowired
    private NewsDomainRepository newsDomainRepository;

    @Override
    public CallResult findPage(NewsParam newsParam) {

        NewsDomain newsDomain = newsDomainRepository.createDomain(newsParam);
        return this.serviceTemplate.executeQuery(new AbstractTemplateAction<Object>() {

            @Override
            public CallResult<Object> doAction() {
                return newsDomain.findNewsPage();
            }
        });
    }

    @Override
    @Transactional
    public CallResult save(NewsParam newsParam) {
        NewsDomain newsDomain = newsDomainRepository.createDomain(newsParam);
        return this.serviceTemplate.executeQuery(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                newsDomain.save();
                return null;
            }
        });
    }

    @Override
    public CallResult findNewsById(NewsParam newsParam) {

        NewsDomain newsDomain = newsDomainRepository.createDomain(newsParam);
        return this.serviceTemplate.executeQuery(new AbstractTemplateAction<Object>() {

            @Override
            public CallResult<Object> doAction() {
                return newsDomain.findNewsById();
            }
        });
    }

    @Override
    @Transactional
    public CallResult update(NewsParam newsParam) {

        NewsDomain newsDomain = newsDomainRepository.createDomain(newsParam);
        return this.serviceTemplate.executeQuery(new AbstractTemplateAction<Object>() {
            @Override
            public CallResult<Object> doAction() {
                return newsDomain.update();
            }
        });
    }

    @Override
    public CallResult upload(MultipartFile file) {
        // 图片往七牛云上传
        // 云存储 图片 用户访问的时候需要消耗带宽，带宽如果都被占用完了，应用就无法访问了
        // 一张图片2m，10个人同时访问就是20m，服务器带宽10M作用
        // 网络卡了，电脑需要用网络的应用 卡了，如果图片的访问将服务器的带宽资源占用完，代表应用不能访问了

        NewsDomain newsDomain = newsDomainRepository.createDomain(new NewsParam());
        return newsDomain.upload(file);
    }
}
