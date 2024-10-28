package com.mszlu.xt.admin.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.admin.domain.repository.NewsDomainRepository;
import com.mszlu.xt.admin.model.NewsModel;
import com.mszlu.xt.admin.params.NewsParam;
import com.mszlu.xt.common.model.CallResult;
import com.mszlu.xt.common.model.ListPageModel;
import com.mszlu.xt.pojo.News;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class NewsDomain {

    private NewsDomainRepository newsDomainRepository;
    private NewsParam newsParam;

    public NewsDomain(NewsDomainRepository newsDomainRepository, NewsParam newsParam) {
        this.newsDomainRepository = newsDomainRepository;
        this.newsParam = newsParam;
    }

    public CallResult<Object> findNewsPage() {
        int currentPage = this.newsParam.getCurrentPage();
        int pageSize = this.newsParam.getPageSize();
        String queryString = this.newsParam.getQueryString();
        Page<News> newsPage = newsDomainRepository.findNewsPageByCondition(currentPage, pageSize, queryString);

        List<News> records = newsPage.getRecords();
        List<NewsModel> newsModels = copyList(records);

        ListPageModel<NewsModel> listPageModel = new ListPageModel<>();
        listPageModel.setList(newsModels);
        listPageModel.setSize(newsPage.getTotal());

        return CallResult.success(listPageModel);
    }

    public NewsModel copy(News news) {
        if (news == null){
            return null;
        }
        NewsModel newsModel = new NewsModel();
        //属性copy
        BeanUtils.copyProperties(news,newsModel);
        if (news.getCreateTime() != null) {
            newsModel.setCreateTime(new DateTime(news.getCreateTime()).toString("yyyy年MM月dd日 HH:mm:ss"));
        }
        if (news.getImageUrl() != null) {
            if (!news.getImageUrl().startsWith("http")) {
                newsModel.setImageUrl(newsDomainRepository.qiniuConfig.getFileServerUrl() + news.getImageUrl());
            }
        }
        return newsModel;
    }

    public List<NewsModel> copyList(List<News> newsList){
        List<NewsModel> newsModelList = new ArrayList<>();
        for (News news : newsList){
            newsModelList.add(copy(news));
        }
        return newsModelList;
    }
}
