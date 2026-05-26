package com.develop.mvp.pk.module.member.infrastructure.user;

import com.develop.mvp.pk.module.member.domain.user.service.PasswordEncoder;
import com.develop.mvp.pk.module.member.domain.user.valueobject.EncodedPassword;
import com.develop.mvp.pk.module.member.domain.user.valueobject.RawPassword;
import org.springframework.stereotype.Component;

// Skill: AggregateRoot_MemberUser_Skill — 基础设施层密码加密实现
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {

    private final org.springframework.security.crypto.password.PasswordEncoder delegate;

    public BCryptPasswordEncoderAdapter() {
        this.delegate = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Override
    public EncodedPassword encode(RawPassword rawPassword) {
        return new EncodedPassword(delegate.encode(rawPassword.value()));
    }

    @Override
    public boolean matches(RawPassword rawPassword, EncodedPassword encodedPassword) {
        return delegate.matches(rawPassword.value(), encodedPassword.value());
    }
}
