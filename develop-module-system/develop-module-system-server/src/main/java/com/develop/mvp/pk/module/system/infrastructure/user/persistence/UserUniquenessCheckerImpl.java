package com.develop.mvp.pk.module.system.infrastructure.user.persistence;

// Skill: AggregateRoot_User_Validation_Skill — 领域服务实现
// DDD 角色：基础设施层实现 UserUniquenessChecker，委托 UserRepository
// 验收标准 AC13：唯一性校验通过此接口完成

import com.develop.mvp.pk.module.system.domain.user.repository.UserRepository;
import com.develop.mvp.pk.module.system.domain.user.service.UserUniquenessChecker;
import com.develop.mvp.pk.module.system.domain.user.valueobject.*;
import org.springframework.stereotype.Component;

/**
 * User Uniqueness Checker Impl 类。
 */
@Component
public class UserUniquenessCheckerImpl implements UserUniquenessChecker {

    private final UserRepository userRepository;

    /**
     * 创建 UserUniquenessCheckerImpl 实例。
     *
     * @param userRepository userRepository 参数
     */
    public UserUniquenessCheckerImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 判断 is Username Unique 对应的条件是否成立。
     *
     * @param username username 参数
     * @param excludeUserId excludeUserId 参数
     * @return 处理结果
     */
    @Override
    public boolean isUsernameUnique(Username username, UserId excludeUserId) {
        return userRepository.findByUsername(username)
                .map(u -> excludeUserId != null && u.id().equals(excludeUserId))
                .orElse(true);
    }

    /**
     * 判断 is Email Unique 对应的条件是否成立。
     *
     * @param email email 参数
     * @param excludeUserId excludeUserId 参数
     * @return 处理结果
     */
    @Override
    public boolean isEmailUnique(Email email, UserId excludeUserId) {
        if (!email.isPresent()) return true;
        return userRepository.findByEmail(email)
                .map(u -> excludeUserId != null && u.id().equals(excludeUserId))
                .orElse(true);
    }

    /**
     * 判断 is Mobile Unique 对应的条件是否成立。
     *
     * @param mobile mobile 参数
     * @param excludeUserId excludeUserId 参数
     * @return 处理结果
     */
    @Override
    public boolean isMobileUnique(Mobile mobile, UserId excludeUserId) {
        if (!mobile.isPresent()) return true;
        return userRepository.findByMobile(mobile)
                .map(u -> excludeUserId != null && u.id().equals(excludeUserId))
                .orElse(true);
    }
}
