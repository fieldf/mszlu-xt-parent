package com.mszlu.xt.sso.domain;


import com.mszlu.xt.sso.dao.data.User;
import com.mszlu.xt.sso.domain.repository.UserDomainRepository;
import com.mszlu.xt.sso.model.params.UserParam;

/**
 * 用户领域 权责明确 用户的事情交给用户的领域去做
 */
public class UserDomain {
    private UserDomainRepository userDomainRepository;
    private UserParam userParam;
    public UserDomain(UserDomainRepository userDomainRepository, UserParam userParam) {
        this.userDomainRepository = userDomainRepository;
        this.userParam = userParam;
    }

    public void updateUser(User user) {
        userDomainRepository.updateUser(user);
    }

    public void saveUser(User user) {
        userDomainRepository.saveUser(user);
    }

    public User findUserByUnionId(String unionId) {
        return userDomainRepository.findUserByUnionId(unionId);
    }
}
