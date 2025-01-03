package com.mszlu.xt.web.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.pojo.News;
import com.mszlu.xt.web.dao.NewsMapper;
import com.mszlu.xt.web.domain.NewsDomain;
import com.mszlu.xt.web.domain.qiniu.QiniuConfig;
import com.mszlu.xt.web.model.enums.Status;
import com.mszlu.xt.web.model.params.NewsParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsDomainRepository {
    @Autowired
    public QiniuConfig qiniuConfig;
    @Autowired
    private NewsMapper newsMapper;

    public NewsDomain createDomain(NewsParam newsParam) {
        return new NewsDomain(this, newsParam);
    }

    public Page<News> findNewsListByPage(int currentPage, int pageSize, Integer tab) {
        Page<News> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<News> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(News::getTab, tab);
        queryWrapper.eq(News::getStatus, Status.NORMAL.getCode());
        queryWrapper.select(News::getId, News::getTitle, News::getImageUrl);
        return newsMapper.selectPage(page, queryWrapper);
    }

    public News findNewsById(Long id) {
        return newsMapper.selectById(id);
    }
}
