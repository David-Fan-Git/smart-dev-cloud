package com.develop.mvp.pk.module.system.application.sms;

import cn.hutool.core.map.MapUtil;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import com.develop.mvp.pk.module.system.application.member.service.MemberApplicationService;
import com.develop.mvp.pk.module.system.application.sms.service.SmsApplicationService;
import com.develop.mvp.pk.module.system.application.user.service.AdminUserApplicationService;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsCodeDO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsChannelDO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsTemplateDO;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsChannelMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsCodeMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsLogMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsTemplateMapper;
import com.develop.mvp.pk.module.system.domain.sms.repository.SmsChannelRepository;
import com.develop.mvp.pk.module.system.enums.sms.SmsSceneEnum;
import com.develop.mvp.pk.module.system.framework.sms.config.SmsCodeProperties;
import com.develop.mvp.pk.module.system.framework.sms.core.client.SmsClientFactory;
import com.develop.mvp.pk.module.system.mq.producer.sms.SmsProducer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static com.develop.mvp.pk.framework.common.enums.CommonStatusEnum.ENABLE;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.randomPojo;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Import(SmsApplicationService.class)
public class SmsCodeApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private SmsApplicationService smsCodeService;

    @Resource
    private SmsCodeMapper smsCodeMapper;
    @Resource
    private SmsChannelMapper smsChannelMapper;
    @Resource
    private SmsTemplateMapper smsTemplateMapper;
    @Resource
    private SmsLogMapper smsLogMapper;

    @MockitoBean
    private SmsChannelRepository smsChannelRepository;
    @MockitoBean
    private SmsCodeProperties smsCodeProperties;
    @MockitoBean
    private SmsClientFactory smsClientFactory;
    @MockitoBean
    private AdminUserApplicationService adminUserService;
    @MockitoBean
    private MemberApplicationService memberApplicationService;
    @MockitoBean
    private SmsProducer smsProducer;

    @BeforeEach
    public void setUp() {
        when(smsCodeProperties.getExpireTimes()).thenReturn(Duration.ofMinutes(5));
        when(smsCodeProperties.getSendFrequency()).thenReturn(Duration.ofMinutes(1));
        when(smsCodeProperties.getSendMaximumQuantityPerDay()).thenReturn(10);
        when(smsCodeProperties.getBeginCode()).thenReturn(9999);
        when(smsCodeProperties.getEndCode()).thenReturn(9999);
    }

    @Test
    public void sendSmsCode_success() {
        // 准备参数
        SmsCodeSendReqDTO reqDTO = randomPojo(SmsCodeSendReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(SmsSceneEnum.MEMBER_LOGIN.getScene());
        });

        SmsChannelDO channel = randomPojo(SmsChannelDO.class, o -> o.setStatus(ENABLE.getStatus()));
        smsChannelMapper.insert(channel);
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setCode("user-sms-login");
            o.setStatus(ENABLE.getStatus());
            o.setContent("验证码为{code}");
            o.setParams(java.util.List.of("code"));
            o.setChannelId(channel.getId());
        });
        smsTemplateMapper.insert(template);

        // 调用
        smsCodeService.sendSmsCode(reqDTO);
        // 断言 code 验证码
        SmsCodeDO smsCodeDO = smsCodeMapper.selectOne(null);
        assertPojoEquals(reqDTO, smsCodeDO);
        assertEquals("9999", smsCodeDO.getCode());
        assertEquals(1, smsCodeDO.getTodayIndex());
        assertFalse(smsCodeDO.getUsed());
        assertEquals(1, smsLogMapper.selectCount());
    }

    @Test
    public void sendSmsCode_tooFast() {
        // mock 数据
        SmsCodeDO smsCodeDO = randomPojo(SmsCodeDO.class,
                o -> o.setMobile("15601691300").setTodayIndex(1));
        smsCodeMapper.insert(smsCodeDO);
        // 准备参数
        SmsCodeSendReqDTO reqDTO = randomPojo(SmsCodeSendReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(SmsSceneEnum.MEMBER_LOGIN.getScene());
        });

        // 调用，并断言异常
        assertServiceException(() -> smsCodeService.sendSmsCode(reqDTO),
                SMS_CODE_SEND_TOO_FAST);
    }

    @Test
    public void sendSmsCode_exceedDay() {
        // mock 数据
        SmsCodeDO smsCodeDO = randomPojo(SmsCodeDO.class,
                o -> o.setMobile("15601691300").setTodayIndex(10).setCreateTime(LocalDateTime.now()));
        smsCodeMapper.insert(smsCodeDO);
        // 准备参数
        SmsCodeSendReqDTO reqDTO = randomPojo(SmsCodeSendReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(SmsSceneEnum.MEMBER_LOGIN.getScene());
        });
        when(smsCodeProperties.getSendFrequency()).thenReturn(Duration.ofMillis(0));

        // 调用，并断言异常
        assertServiceException(() -> smsCodeService.sendSmsCode(reqDTO),
                SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY);
    }

    @Test
    public void testUseSmsCode_success() {
        // 准备参数
        SmsCodeUseReqDTO reqDTO = randomPojo(SmsCodeUseReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(randomEle(SmsSceneEnum.values()).getScene());
        });
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> {
            o.setMobile(reqDTO.getMobile()).setScene(reqDTO.getScene())
                    .setCode(reqDTO.getCode()).setUsed(false);
        }));

        // 调用
        smsCodeService.useSmsCode(reqDTO);
        // 断言
        SmsCodeDO smsCodeDO = smsCodeMapper.selectOne(null);
        assertTrue(smsCodeDO.getUsed());
        assertNotNull(smsCodeDO.getUsedTime());
        assertEquals(reqDTO.getUsedIp(), smsCodeDO.getUsedIp());
    }

    @Test
    public void validateSmsCode_success() {
        // 准备参数
        SmsCodeValidateReqDTO reqDTO = randomPojo(SmsCodeValidateReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(randomEle(SmsSceneEnum.values()).getScene());
        });
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> o.setMobile(reqDTO.getMobile())
                .setScene(reqDTO.getScene()).setCode(reqDTO.getCode()).setUsed(false)));

        // 调用
        smsCodeService.validateSmsCode(reqDTO);
    }

    @Test
    public void validateSmsCode_notFound() {
        // 准备参数
        SmsCodeValidateReqDTO reqDTO = randomPojo(SmsCodeValidateReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(randomEle(SmsSceneEnum.values()).getScene());
        });

        // 调用，并断言异常
        assertServiceException(() -> smsCodeService.validateSmsCode(reqDTO),
                SMS_CODE_NOT_FOUND);
    }

    @Test
    public void validateSmsCode_expired() {
        // 准备参数
        SmsCodeValidateReqDTO reqDTO = randomPojo(SmsCodeValidateReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(randomEle(SmsSceneEnum.values()).getScene());
        });
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> o.setMobile(reqDTO.getMobile())
                .setScene(reqDTO.getScene()).setCode(reqDTO.getCode()).setUsed(false)
                .setCreateTime(LocalDateTime.now().minusMinutes(6))));

        // 调用，并断言异常
        assertServiceException(() -> smsCodeService.validateSmsCode(reqDTO),
                SMS_CODE_EXPIRED);
    }

    @Test
    public void validateSmsCode_used() {
        // 准备参数
        SmsCodeValidateReqDTO reqDTO = randomPojo(SmsCodeValidateReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(randomEle(SmsSceneEnum.values()).getScene());
        });
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> o.setMobile(reqDTO.getMobile())
                .setScene(reqDTO.getScene()).setCode(reqDTO.getCode()).setUsed(true)
                .setCreateTime(LocalDateTime.now())));

        // 调用，并断言异常
        assertServiceException(() -> smsCodeService.validateSmsCode(reqDTO),
                SMS_CODE_USED);
    }

}
