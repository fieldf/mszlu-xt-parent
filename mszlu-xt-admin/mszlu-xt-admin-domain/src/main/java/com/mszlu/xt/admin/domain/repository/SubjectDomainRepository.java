package com.mszlu.xt.admin.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.xt.admin.dao.SubjectMapper;
import com.mszlu.xt.admin.dao.SubjectUnitMapper;
import com.mszlu.xt.admin.domain.SubjectDomain;
import com.mszlu.xt.admin.params.SubjectParam;
import com.mszlu.xt.pojo.Subject;
import com.mszlu.xt.pojo.SubjectUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SubjectDomainRepository {
    @Resource
    private SubjectMapper subjectMapper;
    @Resource
    private SubjectUnitMapper subjectUnitMapper;

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

    public Subject findSubjectByCondition(String subjectName, String subjectGrade, String subjectTerm) {
        LambdaQueryWrapper<Subject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Subject::getSubjectName, subjectName);
        queryWrapper.eq(Subject::getSubjectGrade, subjectGrade);
        queryWrapper.eq(Subject::getSubjectTerm, subjectTerm);
        return subjectMapper.selectOne(queryWrapper);
    }

    public void save(Subject subject) {
        this.subjectMapper.insert(subject);
    }

    public void saveSubjectUnit(Long id, Integer subjectUnit) {
        SubjectUnit subjectUnit1 = new SubjectUnit();
        subjectUnit1.setSubjectId(id);
        subjectUnit1.setSubjectUnit(subjectUnit);
        subjectUnitMapper.insert(subjectUnit1);
    }
}
