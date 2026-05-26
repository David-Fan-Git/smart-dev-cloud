package com.develop.mvp.pk.module.member.domain.user;

import com.develop.mvp.pk.module.member.domain.user.service.PasswordEncoder;
import com.develop.mvp.pk.module.member.domain.user.valueobject.*;

// Skill: AggregateRoot_MemberUser_Skill — 工厂 MemberUserFactory
public final class MemberUserFactory {

    private final PasswordEncoder passwordEncoder;

    public MemberUserFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public MemberUser create(Nickname nickname, Mobile mobile, RawPassword rawPassword) {
        EncodedPassword encoded = passwordEncoder.encode(rawPassword);
        return MemberUser.create(nickname, mobile, encoded);
    }

    public MemberUser createQuick(String mobile, String registerIp, Integer terminal) {
        // 自动生成密码和昵称
        String randomPwd = java.util.UUID.randomUUID().toString().replace("-", "");
        Nickname nickname = new Nickname("用户" + cn.hutool.core.util.RandomUtil.randomNumbers(6));
        Mobile m = new Mobile(mobile);
        RawPassword raw = new RawPassword(randomPwd);
        EncodedPassword encoded = passwordEncoder.encode(raw);
        return MemberUser.create(nickname, m, encoded)
                .recordRegisterInfo(registerIp, terminal);
    }
}
