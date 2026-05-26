package com.develop.mvp.pk.module.system.application.user.service;

import com.develop.mvp.pk.module.system.application.dept.service.DeptApplicationService;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.UserPostDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.dal.mysql.dept.UserPostMapper;
import com.develop.mvp.pk.module.system.dal.mysql.user.AdminUserMapper;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEventPublisher;
import com.develop.mvp.pk.module.system.domain.user.event.UserCreatedEvent;
import com.develop.mvp.pk.module.system.domain.user.service.PasswordEncoder;
import com.develop.mvp.pk.module.system.infrastructure.user.persistence.UserRepositoryImpl;
import com.develop.mvp.pk.module.system.infrastructure.user.persistence.UserUniquenessCheckerImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import({UserApplicationService.class, UserRepositoryImpl.class, UserUniquenessCheckerImpl.class})
class UserApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private UserApplicationService userApplicationService;
    @Resource
    private AdminUserMapper userMapper;
    @Resource
    private UserPostMapper userPostMapper;

    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private DomainEventPublisher eventPublisher;
    @MockitoBean
    private DeptApplicationService deptService;

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void createUserUsesTenantContextAndReturnsGeneratedId() {
        TenantContextHolder.setTenantId(10L);
        when(passwordEncoder.encode(any())).thenReturn(com.develop.mvp.pk.module.system.domain.user.valueobject.EncodedPassword.of("encoded-password"));

        Long userId = userApplicationService.createUser(
                null, "tenantAdmin", "raw-password", null,
                20L, "admin@example.com", "15601691300", "David",
                "avatar.png", 1, "remark", Set.of(30L, 40L));

        assertNotNull(userId);
        AdminUserDO user = userMapper.selectById(userId);
        assertNotNull(user);
        assertEquals("tenantAdmin", user.getUsername());
        assertEquals("encoded-password", user.getPassword());
        assertEquals(10L, user.getTenantId());
        assertEquals(20L, user.getDeptId());
        assertEquals("admin@example.com", user.getEmail());
        assertEquals("15601691300", user.getMobile());
        assertEquals("David", user.getNickname());
        assertEquals("avatar.png", user.getAvatar());
        assertEquals(1, user.getSex());
        assertEquals("remark", user.getRemark());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), user.getStatus());
        assertEquals(Set.of(30L, 40L), user.getPostIds());
        assertEquals(Set.of(30L, 40L), com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet(
                userPostMapper.selectListByUserId(userId), UserPostDO::getPostId));
        verify(deptService).validateDeptList(Set.of(20L));
        verify(deptService).validatePostList(Set.of(30L, 40L));
        verify(eventPublisher).publish(isA(UserCreatedEvent.class));
    }
}
