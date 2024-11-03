package com.mszlu.xt.admin.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.admin.dao.SubjectMapper;
import com.mszlu.xt.admin.domain.SubjectDomain;
import com.mszlu.xt.admin.params.SubjectParam;
import com.mszlu.xt.pojo.Subject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SubjectDomainRepository {
    @Resource
    private SubjectMapper subjectMapper;

    public SubjectDomain createDomain(SubjectParam subjectParam) {
        return new SubjectDomain(this, subjectParam);
    }

    public Page<Subject> findSubjectListPage(int currentPage, int pageSize, String queryString) {

        Page<Subject> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Subject> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(queryString)) {
            queryWrapper.eq(Subject::getSubjectName, queryString);
        }
        return subjectMapper.selectPage(page, queryWrapper);
    }
}
