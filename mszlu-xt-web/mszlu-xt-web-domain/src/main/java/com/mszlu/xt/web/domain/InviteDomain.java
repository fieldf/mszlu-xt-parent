package com.mszlu.xt.web.domain;

import com.mszlu.xt.pojo.Invite;

import com.mszlu.xt.sso.model.params.InviteParam;
import com.mszlu.xt.web.domain.repository.InviteDomainRepository;

public class InviteDomain {

    private InviteDomainRepository inviteDomainRepository;
    private InviteParam inviteParam;

    public InviteDomain(InviteDomainRepository inviteDomainRepository, InviteParam inviteParam) {
        this.inviteDomainRepository = inviteDomainRepository;
        this.inviteParam = inviteParam;
    }

    public void save(Invite invite) {
        inviteDomainRepository.save(invite);
    }
}
