package com.develop.mvp.pk.module.system.application.sms;

import cn.hutool.core.map.MapUtil;
import com.develop.mvp.pk.framework.common.core.KeyValue;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.test.core.ut.BaseMockitoUnitTest;
import com.develop.mvp.pk.module.system.application.member.service.MemberApplicationService;
import com.develop.mvp.pk.module.system.application.sms.service.SmsApplicationService;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsChannelDO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsTemplateDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsChannelMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsCodeMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsLogMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsTemplateMapper;
import com.develop.mvp.pk.module.system.domain.sms.repository.SmsChannelRepository;
import com.develop.mvp.pk.module.system.framework.sms.config.SmsCodeProperties;
import com.develop.mvp.pk.module.system.framework.sms.core.client.SmsClient;
import com.develop.mvp.pk.module.system.framework.sms.core.client.SmsClientFactory;
import com.develop.mvp.pk.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import com.develop.mvp.pk.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import com.develop.mvp.pk.module.system.mq.message.sms.SmsSendMessage;
import com.develop.mvp.pk.module.system.mq.producer.sms.SmsProducer;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.*;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SmsSendApplicationServiceTest extends BaseMockitoUnitTest {

    private SmsApplicationService smsSendService;

    @Mock
    private SmsChannelRepository smsChannelRepository;
    @Mock
    private SmsClientFactory smsClientFactory;
    @Mock
    private SmsChannelMapper smsChannelMapper;
    @Mock
    private SmsTemplateMapper smsTemplateMapper;
    @Mock
    private SmsLogMapper smsLogMapper;
    @Mock
    private SmsCodeMapper smsCodeMapper;
    @Mock
    private SmsCodeProperties smsCodeProperties;
    @Mock
    private AdminUserUseCase adminUserService;
    @Mock
    private MemberApplicationService memberApplicationService;
    @Mock
    private SmsProducer smsProducer;

    @BeforeEach
    public void setUp() {
        smsSendService = spy(new SmsApplicationService(smsChannelRepository,
                smsClientFactory, smsChannelMapper, smsTemplateMapper, smsLogMapper,
                smsCodeMapper, smsCodeProperties, adminUserService,
                memberApplicationService, smsProducer));
    }

    @Test
    public void testSendSingleSmsToAdmin() {
        Long userId = randomLongId();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setMobile("15601691300"));
        when(adminUserService.getUser(eq(userId))).thenReturn(user);
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        doReturn(template).when(smsSendService).getSmsTemplateByCodeFromCache(eq(templateCode));
        String content = randomString();
        doReturn(content).when(smsSendService).formatSmsTemplateContent(eq(template.getContent()), eq(templateParams));
        SmsChannelDO smsChannel = randomPojo(SmsChannelDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        doReturn(smsChannel).when(smsSendService).getSmsChannel(eq(template.getChannelId()));
        Long smsLogId = randomLongId();
        doReturn(smsLogId).when(smsSendService).createSmsLog(eq(user.getMobile()), eq(userId),
                eq(UserTypeEnum.ADMIN.getValue()), eq(Boolean.TRUE), eq(template), eq(content), eq(templateParams));

        Long resultSmsLogId = smsSendService.sendSingleSmsToAdmin(null, userId, templateCode, templateParams);

        assertEquals(smsLogId, resultSmsLogId);
        verify(smsProducer).sendSmsSendMessage(eq(smsLogId), eq(user.getMobile()),
                eq(template.getChannelId()), eq(template.getApiTemplateId()),
                eq(Lists.newArrayList(new KeyValue<>("code", "1234"), new KeyValue<>("op", "login"))));
    }

    @Test
    public void testSendSingleSmsToUser() {
        Long userId = randomLongId();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        String mobile = "15601691300";
        when(memberApplicationService.getMemberUserMobile(eq(userId))).thenReturn(mobile);
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        doReturn(template).when(smsSendService).getSmsTemplateByCodeFromCache(eq(templateCode));
        String content = randomString();
        doReturn(content).when(smsSendService).formatSmsTemplateContent(eq(template.getContent()), eq(templateParams));
        SmsChannelDO smsChannel = randomPojo(SmsChannelDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        doReturn(smsChannel).when(smsSendService).getSmsChannel(eq(template.getChannelId()));
        Long smsLogId = randomLongId();
        doReturn(smsLogId).when(smsSendService).createSmsLog(eq(mobile), eq(userId),
                eq(UserTypeEnum.MEMBER.getValue()), eq(Boolean.TRUE), eq(template), eq(content), eq(templateParams));

        Long resultSmsLogId = smsSendService.sendSingleSmsToMember(null, userId, templateCode, templateParams);

        assertEquals(smsLogId, resultSmsLogId);
        verify(smsProducer).sendSmsSendMessage(eq(smsLogId), eq(mobile),
                eq(template.getChannelId()), eq(template.getApiTemplateId()),
                eq(Lists.newArrayList(new KeyValue<>("code", "1234"), new KeyValue<>("op", "login"))));
    }

    @Test
    public void testSendSingleSms_successWhenSmsTemplateEnable() {
        String mobile = randomString();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        doReturn(template).when(smsSendService).getSmsTemplateByCodeFromCache(eq(templateCode));
        String content = randomString();
        doReturn(content).when(smsSendService).formatSmsTemplateContent(eq(template.getContent()), eq(templateParams));
        SmsChannelDO smsChannel = randomPojo(SmsChannelDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        doReturn(smsChannel).when(smsSendService).getSmsChannel(eq(template.getChannelId()));
        Long smsLogId = randomLongId();
        doReturn(smsLogId).when(smsSendService).createSmsLog(eq(mobile), eq(userId), eq(userType),
                eq(Boolean.TRUE), eq(template), eq(content), eq(templateParams));

        Long resultSmsLogId = smsSendService.sendSingleSms(mobile, userId, userType, templateCode, templateParams);

        assertEquals(smsLogId, resultSmsLogId);
        verify(smsProducer).sendSmsSendMessage(eq(smsLogId), eq(mobile),
                eq(template.getChannelId()), eq(template.getApiTemplateId()),
                eq(Lists.newArrayList(new KeyValue<>("code", "1234"), new KeyValue<>("op", "login"))));
    }

    @Test
    public void testSendSingleSms_successWhenSmsTemplateDisable() {
        String mobile = randomString();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        doReturn(template).when(smsSendService).getSmsTemplateByCodeFromCache(eq(templateCode));
        String content = randomString();
        doReturn(content).when(smsSendService).formatSmsTemplateContent(eq(template.getContent()), eq(templateParams));
        SmsChannelDO smsChannel = randomPojo(SmsChannelDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        doReturn(smsChannel).when(smsSendService).getSmsChannel(eq(template.getChannelId()));
        Long smsLogId = randomLongId();
        doReturn(smsLogId).when(smsSendService).createSmsLog(eq(mobile), eq(userId), eq(userType),
                eq(Boolean.FALSE), eq(template), eq(content), eq(templateParams));

        Long resultSmsLogId = smsSendService.sendSingleSms(mobile, userId, userType, templateCode, templateParams);

        assertEquals(smsLogId, resultSmsLogId);
        verify(smsProducer, times(0)).sendSmsSendMessage(anyLong(), anyString(), anyLong(), any(), anyList());
    }

    @Test
    public void testCheckSmsTemplateValid_notExists() {
        String templateCode = randomString();
        doReturn(null).when(smsSendService).getSmsTemplateByCodeFromCache(eq(templateCode));

        assertServiceException(() -> smsSendService.validateSmsTemplate(templateCode), SMS_SEND_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testBuildTemplateParams_paramMiss() {
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> o.setParams(Lists.newArrayList("code")));
        Map<String, Object> templateParams = new HashMap<>();

        assertServiceException(() -> smsSendService.buildTemplateParams(template, templateParams),
                SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS, "code");
    }

    @Test
    public void testCheckMobile_notExists() {
        assertServiceException(() -> smsSendService.validateMobile(null), SMS_SEND_MOBILE_NOT_EXISTS);
    }

    @Test
    public void testSendBatchNotify() {
        UnsupportedOperationException exception = Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> smsSendService.sendBatchSms(null, null, null, null, null)
        );
        assertEquals("暂时不支持该操作，感兴趣可以实现该功能哟！", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDoSendSms() throws Throwable {
        SmsSendMessage message = randomPojo(SmsSendMessage.class);
        SmsClient smsClient = mock(SmsClient.class);
        doReturn(smsClient).when(smsSendService).getSmsClient(eq(message.getChannelId()));
        SmsSendRespDTO sendResult = randomPojo(SmsSendRespDTO.class);
        when(smsClient.sendSms(eq(message.getLogId()), eq(message.getMobile()), eq(message.getApiTemplateId()),
                eq(message.getTemplateParams()))).thenReturn(sendResult);
        doNothing().when(smsSendService).updateSmsSendResult(anyLong(), anyBoolean(), any(), any(), any(), any());

        smsSendService.doSendSms(message);

        verify(smsSendService).updateSmsSendResult(eq(message.getLogId()),
                eq(sendResult.getSuccess()), eq(sendResult.getApiCode()),
                eq(sendResult.getApiMsg()), eq(sendResult.getApiRequestId()), eq(sendResult.getSerialNo()));
    }

    @Test
    public void testReceiveSmsStatus() throws Throwable {
        String channelCode = randomString();
        String text = randomString();
        SmsClient smsClient = mock(SmsClient.class);
        doReturn(smsClient).when(smsSendService).getSmsClient(eq(channelCode));
        List<SmsReceiveRespDTO> receiveResults = randomPojoList(SmsReceiveRespDTO.class);
        when(smsClient.parseSmsReceiveStatus(eq(text))).thenReturn(receiveResults);
        doNothing().when(smsSendService).updateSmsReceiveResult(any(), any(), anyBoolean(), any(), any(), any());

        smsSendService.receiveSmsStatus(channelCode, text);

        receiveResults.forEach(result -> verify(smsSendService).updateSmsReceiveResult(eq(result.getLogId()),
                eq(result.getSerialNo()), eq(result.getSuccess()), eq(result.getReceiveTime()),
                eq(result.getErrorCode()), eq(result.getErrorMsg())));
    }

}
