package com.develop.mvp.pk.module.member.infrastructure.user;

import com.develop.mvp.pk.module.member.domain.user.MemberUserFactory;
import com.develop.mvp.pk.module.member.domain.user.service.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MemberUserFactoryConfiguration {

    @Bean
    public MemberUserFactory memberUserFactory(PasswordEncoder passwordEncoder) {
        return new MemberUserFactory(passwordEncoder);
    }
}
