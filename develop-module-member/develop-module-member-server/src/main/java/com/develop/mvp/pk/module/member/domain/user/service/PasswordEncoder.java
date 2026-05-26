package com.develop.mvp.pk.module.member.domain.user.service;

import com.develop.mvp.pk.module.member.domain.user.valueobject.EncodedPassword;
import com.develop.mvp.pk.module.member.domain.user.valueobject.RawPassword;

// Skill: AggregateRoot_MemberUser_Skill — 领域服务接口 PasswordEncoder
public interface PasswordEncoder {
    EncodedPassword encode(RawPassword rawPassword);
    boolean matches(RawPassword rawPassword, EncodedPassword encodedPassword);
}
