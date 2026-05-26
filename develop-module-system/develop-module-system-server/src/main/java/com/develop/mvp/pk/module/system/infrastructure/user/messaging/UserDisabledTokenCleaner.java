package com.develop.mvp.pk.module.system.infrastructure.user.messaging;

// Skill: AggregateRoot_User_Validation_Skill — 事件订阅者
// DDD 角色：基础设施层订阅者，监听 UserDisabledEvent 清理 OAuth2 Token
// 对应原代码：AdminUserServiceImpl.updateUserStatus() L238-240
// 验收标准 AC10：禁用用户时，UserDisabledEvent 被发布且被此订阅者消费

import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.module.system.application.oauth2.port.inbound.OAuth2UseCase;
import com.develop.mvp.pk.module.system.domain.user.event.UserDisabledEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * User Disabled Token Cleaner 类。
 */
@Component
public class UserDisabledTokenCleaner {

    private final OAuth2UseCase oauth2TokenService;

    /**
     * 创建 UserDisabledTokenCleaner 实例。
     *
     * @param oauth2TokenService oauth2TokenService 参数
     */
    public UserDisabledTokenCleaner(OAuth2UseCase oauth2TokenService) {
        this.oauth2TokenService = oauth2TokenService;
    }

    /**
     * 处理 on User Disabled 对应的业务逻辑。
     *
     * @param event event 参数
     */
    @EventListener
    public void onUserDisabled(UserDisabledEvent event) {
        oauth2TokenService.removeAccessToken(event.userId(), UserTypeEnum.ADMIN.getValue());
    }
}
