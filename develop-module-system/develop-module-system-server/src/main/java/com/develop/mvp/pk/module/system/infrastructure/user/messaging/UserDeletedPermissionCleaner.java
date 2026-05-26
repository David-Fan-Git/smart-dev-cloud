package com.develop.mvp.pk.module.system.infrastructure.user.messaging;

// Skill: AggregateRoot_User_Validation_Skill — 事件订阅者
// DDD 角色：基础设施层订阅者，监听 UserDeletedEvent 清理权限和岗位
// 对应原代码：AdminUserServiceImpl.deleteUser() L252-256
// 验收标准 AC11：删除用户时，UserDeletedEvent 被发布且被此订阅者消费

import com.develop.mvp.pk.module.system.dal.mysql.dept.UserPostMapper;
import com.develop.mvp.pk.module.system.domain.user.event.UserDeletedEvent;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * User Deleted Permission Cleaner 类。
 */
@Component
public class UserDeletedPermissionCleaner {

    private final PermissionUseCase permissionService;
    private final UserPostMapper userPostMapper;

    /**
     * 创建 UserDeletedPermissionCleaner 实例。
     *
     * @param permissionService permissionService 参数
     * @param userPostMapper userPostMapper 参数
     */
    public UserDeletedPermissionCleaner(PermissionUseCase permissionService,
                                         UserPostMapper userPostMapper) {
        this.permissionService = permissionService;
        this.userPostMapper = userPostMapper;
    }

    /**
     * 处理 on User Deleted 对应的业务逻辑。
     *
     * @param event event 参数
     */
    @EventListener
    public void onUserDeleted(UserDeletedEvent event) {
        permissionService.processUserDeleted(event.userId());
        userPostMapper.deleteByUserId(event.userId());
    }
}
