package com.develop.mvp.pk.module.system.application.notify;

import cn.hutool.core.map.MapUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.test.core.ut.BaseMockitoUnitTest;
import com.develop.mvp.pk.module.system.application.notify.service.NotifyApplicationService;
import com.develop.mvp.pk.module.system.dal.dataobject.notify.NotifyTemplateDO;
import com.develop.mvp.pk.module.system.dal.mysql.notify.NotifyMessageMapper;
import com.develop.mvp.pk.module.system.dal.mysql.notify.NotifyTemplateMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.*;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.NOTIFY_SEND_TEMPLATE_PARAM_MISS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotifySendApplicationServiceTest extends BaseMockitoUnitTest {

    private NotifyApplicationService notifySendService;

    @Mock
    private NotifyMessageMapper notifyMessageMapper;
    @Mock
    private NotifyTemplateMapper notifyTemplateMapper;

    @BeforeEach
    void setUp() {
        notifySendService = spy(new NotifyApplicationService(notifyMessageMapper, notifyTemplateMapper));
    }

    @Test
    public void testSendSingleNotifyToAdmin() {
        Long userId = randomLongId();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        doReturn(template).when(notifySendService).getNotifyTemplateByCodeFromCache(eq(templateCode));
        String content = randomString();
        doReturn(content).when(notifySendService).formatNotifyTemplateContent(eq(template.getContent()), eq(templateParams));
        Long messageId = randomLongId();
        doReturn(messageId).when(notifySendService).createNotifyMessage(eq(userId), eq(UserTypeEnum.ADMIN.getValue()),
                eq(template), eq(content), eq(templateParams));

        Long resultMessageId = notifySendService.sendSingleNotifyToAdmin(userId, templateCode, templateParams);

        assertEquals(messageId, resultMessageId);
    }

    @Test
    public void testSendSingleNotifyToMember() {
        Long userId = randomLongId();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        doReturn(template).when(notifySendService).getNotifyTemplateByCodeFromCache(eq(templateCode));
        String content = randomString();
        doReturn(content).when(notifySendService).formatNotifyTemplateContent(eq(template.getContent()), eq(templateParams));
        Long messageId = randomLongId();
        doReturn(messageId).when(notifySendService).createNotifyMessage(eq(userId), eq(UserTypeEnum.MEMBER.getValue()),
                eq(template), eq(content), eq(templateParams));

        Long resultMessageId = notifySendService.sendSingleNotifyToMember(userId, templateCode, templateParams);

        assertEquals(messageId, resultMessageId);
    }

    @Test
    public void testSendSingleNotify_successWhenMailTemplateEnable() {
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        doReturn(template).when(notifySendService).getNotifyTemplateByCodeFromCache(eq(templateCode));
        String content = randomString();
        doReturn(content).when(notifySendService).formatNotifyTemplateContent(eq(template.getContent()), eq(templateParams));
        Long messageId = randomLongId();
        doReturn(messageId).when(notifySendService).createNotifyMessage(eq(userId), eq(userType),
                eq(template), eq(content), eq(templateParams));

        Long resultMessageId = notifySendService.sendSingleNotify(userId, userType, templateCode, templateParams);

        assertEquals(messageId, resultMessageId);
    }

    @Test
    public void testSendSingleMail_successWhenSmsTemplateDisable() {
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        doReturn(template).when(notifySendService).getNotifyTemplateByCodeFromCache(eq(templateCode));

        Long resultMessageId = notifySendService.sendSingleNotify(userId, userType, templateCode, templateParams);

        assertNull(resultMessageId);
        verify(notifySendService, never()).formatNotifyTemplateContent(anyString(), anyMap());
        verify(notifySendService, never()).createNotifyMessage(anyLong(), anyInt(), any(), anyString(), anyMap());
    }

    @Test
    public void testCheckMailTemplateValid_notExists() {
        String templateCode = randomString();
        doReturn(null).when(notifySendService).getNotifyTemplateByCodeFromCache(eq(templateCode));

        assertServiceException(() -> notifySendService.validateNotifyTemplate(templateCode),
                NOTICE_NOT_FOUND);
    }

    @Test
    public void testCheckTemplateParams_paramMiss() {
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class,
                o -> o.setParams(Lists.newArrayList("code")));
        Map<String, Object> templateParams = new HashMap<>();

        assertServiceException(() -> notifySendService.validateTemplateParams(template, templateParams),
                NOTIFY_SEND_TEMPLATE_PARAM_MISS, "code");
    }

    @Test
    public void testSendBatchNotify() {
        UnsupportedOperationException exception = Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> notifySendService.sendBatchNotify(null, null, null, null, null)
        );
        assertEquals("暂时不支持该操作，感兴趣可以实现该功能哟！", exception.getMessage());
    }
}
