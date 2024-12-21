package com.mszlu.xt.web.domain;

import com.mszlu.xt.pojo.UserHistory;
import com.mszlu.xt.web.domain.repository.UserHistoryDomainRepository;
import com.mszlu.xt.web.model.params.UserHistoryParam;

public class UserHistoryDomain {
    private UserHistoryDomainRepository userHistoryDomainRepository;
    private UserHistoryParam userHistoryParam;
    public UserHistoryDomain(UserHistoryDomainRepository userHistoryDomainRepository, UserHistoryParam userHistoryParam) {
        this.userHistoryDomainRepository = userHistoryDomainRepository;
        this.userHistoryParam = userHistoryParam;
    }

    public UserHistory findUserHistory(Long userId, Long subjectId, int historyStatus) {
        return userHistoryDomainRepository.findUserHistory(userId, subjectId, historyStatus);
    }

    public UserHistory findUserHistoryById(Long id) {
        return userHistoryDomainRepository.findUserHistoryById(id);
    }

    public void saveUserHistory(UserHistory userHistory) {
        userHistoryDomainRepository.save(userHistory);
    }
}
