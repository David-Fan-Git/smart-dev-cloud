package com.develop.mvp.pk.module.system.application.sms.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.core.KeyValue;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.datapermission.core.annotation.DataPermission;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import com.develop.mvp.pk.module.system.application.member.service.MemberApplicationService;
import com.develop.mvp.pk.module.system.application.sms.port.inbound.SmsUseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.channel.SmsChannelPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.channel.SmsChannelSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.log.SmsLogPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.template.SmsTemplatePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.template.SmsTemplateSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsChannelDO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsCodeDO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsLogDO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsTemplateDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsChannelMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsCodeMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsLogMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsTemplateMapper;
import com.develop.mvp.pk.module.system.dal.redis.RedisKeyConstants;
import com.develop.mvp.pk.module.system.domain.sms.SmsChannel;
import com.develop.mvp.pk.module.system.domain.sms.repository.SmsChannelRepository;
import com.develop.mvp.pk.module.system.enums.sms.SmsReceiveStatusEnum;
import com.develop.mvp.pk.module.system.enums.sms.SmsSceneEnum;
import com.develop.mvp.pk.module.system.enums.sms.SmsSendStatusEnum;
import com.develop.mvp.pk.module.system.framework.sms.config.SmsCodeProperties;
import com.develop.mvp.pk.module.system.framework.sms.core.client.SmsClient;
import com.develop.mvp.pk.module.system.framework.sms.core.client.SmsClientFactory;
import com.develop.mvp.pk.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import com.develop.mvp.pk.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import com.develop.mvp.pk.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import com.develop.mvp.pk.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import com.develop.mvp.pk.module.system.framework.sms.core.property.SmsChannelProperties;
import com.develop.mvp.pk.module.system.mq.message.sms.SmsSendMessage;
import com.develop.mvp.pk.module.system.mq.producer.sms.SmsProducer;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.hutool.core.util.RandomUtil.randomInt;
import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.util.date.DateUtils.isToday;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

/**
 * Sms Application Service 应用服务。
 */
@Slf4j
public class SmsApplicationService implements SmsUseCase {

    private static final Pattern PATTERN_PARAMS = Pattern.compile("\\{(.*?)}");

    private final SmsChannelRepository channelRepo;
    private final SmsClientFactory smsClientFactory;
    private final SmsChannelMapper smsChannelMapper;
    private final SmsTemplateMapper smsTemplateMapper;
    private final SmsLogMapper smsLogMapper;
    private final SmsCodeMapper smsCodeMapper;
    private final SmsCodeProperties smsCodeProperties;
    private final AdminUserUseCase adminUserService;
    private final MemberApplicationService memberApplicationService;
    private final SmsProducer smsProducer;

    /**
     * 创建 SmsApplicationService 实例。
     *
     * @param channelRepo channelRepo 参数
     * @param smsClientFactory smsClientFactory 参数
     * @param smsChannelMapper smsChannelMapper 参数
     * @param smsTemplateMapper smsTemplateMapper 参数
     * @param smsLogMapper smsLogMapper 参数
     * @param smsCodeMapper smsCodeMapper 参数
     * @param smsCodeProperties smsCodeProperties 参数
     * @param adminUserService adminUserService 参数
     * @param memberApplicationService memberApplicationService 参数
     * @param smsProducer smsProducer 参数
     */
    public SmsApplicationService(SmsChannelRepository channelRepo,
                                  SmsClientFactory smsClientFactory,
                                  SmsChannelMapper smsChannelMapper,
                                  SmsTemplateMapper smsTemplateMapper,
                                  SmsLogMapper smsLogMapper,
                                  SmsCodeMapper smsCodeMapper,
                                  SmsCodeProperties smsCodeProperties,
                                  AdminUserUseCase adminUserService,
                                  MemberApplicationService memberApplicationService,
                                  SmsProducer smsProducer) {
        this.channelRepo = channelRepo;
        this.smsClientFactory = smsClientFactory;
        this.smsChannelMapper = smsChannelMapper;
        this.smsTemplateMapper = smsTemplateMapper;
        this.smsLogMapper = smsLogMapper;
        this.smsCodeMapper = smsCodeMapper;
        this.smsCodeProperties = smsCodeProperties;
        this.adminUserService = adminUserService;
        this.memberApplicationService = memberApplicationService;
        this.smsProducer = smsProducer;
    }

    /**
     * 创建 create Channel 对应的数据。
     *
     * @param code code 参数
     * @param signature signature 参数
     * @param status status 参数
     * @param apiKey apiKey 参数
     * @param apiSecret apiSecret 参数
     * @param callbackUrl callbackUrl 参数
     * @param remark remark 参数
     * @return 处理结果
     */
    @Transactional
    public Long createChannel(String code, String signature, Integer status, String apiKey, String apiSecret, String callbackUrl, String remark) {
        SmsChannelDO channel = new SmsChannelDO().setCode(code).setSignature(signature).setStatus(status)
                .setApiKey(apiKey).setApiSecret(apiSecret).setCallbackUrl(callbackUrl).setRemark(remark);
        smsChannelMapper.insert(channel);
        return channel.getId();
    }

    /**
     * 更新 update Channel 对应的数据。
     *
     * @param id id 参数
     * @param code code 参数
     * @param signature signature 参数
     * @param status status 参数
     * @param apiKey apiKey 参数
     * @param apiSecret apiSecret 参数
     * @param callbackUrl callbackUrl 参数
     * @param remark remark 参数
     */
    @Transactional
    public void updateChannel(Long id, String code, String signature, Integer status, String apiKey, String apiSecret, String callbackUrl, String remark) {
        channelRepo.save(SmsChannel.of(id, code, signature).status(status).apiKey(apiKey).apiSecret(apiSecret).callbackUrl(callbackUrl).remark(remark));
    }

    /**
     * 删除 delete Channel 对应的数据。
     *
     * @param id id 参数
     */
    @Transactional
    public void deleteChannel(Long id) {
        channelRepo.delete(id);
    }

    /**
     * 查询 get Channel 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public SmsChannel getChannel(Long id) {
        return channelRepo.findById(id);
    }

    /**
     * 查询 get Channel By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    public SmsChannel getChannelByCode(String code) {
        return channelRepo.findByCode(code);
    }

    /**
     * 查询 get Channel List 对应的数据。
     *
     * @return 处理结果
     */
    public List<SmsChannel> getChannelList() {
        return channelRepo.findAll();
    }

    /**
     * 查询 get Channel Page 对应的数据。
     *
     * @param signature signature 参数
     * @param status status 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    public PageResult<SmsChannel> getChannelPage(String signature, Integer status, Integer pageNo, Integer pageSize) {
        return channelRepo.findPage(signature, status, pageNo, pageSize);
    }

    /**
     * 创建 create Sms Channel 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    public Long createSmsChannel(SmsChannelSaveReqVO createReqVO) {
        SmsChannelDO channel = BeanUtils.toBean(createReqVO, SmsChannelDO.class);
        smsChannelMapper.insert(channel);
        return channel.getId();
    }

    /**
     * 更新 update Sms Channel 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    public void updateSmsChannel(SmsChannelSaveReqVO updateReqVO) {
        validateSmsChannelExists(updateReqVO.getId());
        SmsChannelDO updateObj = BeanUtils.toBean(updateReqVO, SmsChannelDO.class);
        smsChannelMapper.updateById(updateObj);
    }

    /**
     * 删除 delete Sms Channel 对应的数据。
     *
     * @param id id 参数
     */
    public void deleteSmsChannel(Long id) {
        validateSmsChannelExists(id);
        if (getSmsTemplateCountByChannelId(id) > 0) {
            throw exception(SMS_CHANNEL_HAS_CHILDREN);
        }
        smsChannelMapper.deleteById(id);
    }

    /**
     * 删除 delete Sms Channel List 对应的数据。
     *
     * @param ids ids 参数
     */
    public void deleteSmsChannelList(List<Long> ids) {
        ids.forEach(id -> {
            if (getSmsTemplateCountByChannelId(id) > 0) {
                throw exception(SMS_CHANNEL_HAS_CHILDREN);
            }
        });
        smsChannelMapper.deleteByIds(ids);
    }

    /**
     * 校验 validate Sms Channel Exists 对应的业务规则。
     *
     * @param id id 参数
     * @return 处理结果
     */
    private SmsChannelDO validateSmsChannelExists(Long id) {
        SmsChannelDO channel = smsChannelMapper.selectById(id);
        if (channel == null) {
            throw exception(SMS_CHANNEL_NOT_EXISTS);
        }
        return channel;
    }

    /**
     * 查询 get Sms Channel 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public SmsChannelDO getSmsChannel(Long id) {
        return smsChannelMapper.selectById(id);
    }

    /**
     * 查询 get Sms Channel List 对应的数据。
     *
     * @return 处理结果
     */
    public List<SmsChannelDO> getSmsChannelList() {
        return smsChannelMapper.selectList();
    }

    /**
     * 查询 get Sms Channel Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    public PageResult<SmsChannelDO> getSmsChannelPage(SmsChannelPageReqVO pageReqVO) {
        return smsChannelMapper.selectPage(pageReqVO);
    }

    /**
     * 查询 get Sms Client 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public SmsClient getSmsClient(Long id) {
        SmsChannelDO channel = smsChannelMapper.selectById(id);
        SmsChannelProperties properties = BeanUtils.toBean(channel, SmsChannelProperties.class);
        return smsClientFactory.createOrUpdateSmsClient(properties);
    }

    /**
     * 查询 get Sms Client 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    public SmsClient getSmsClient(String code) {
        return smsClientFactory.getSmsClient(code);
    }

    /**
     * 创建 create Sms Template 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    public Long createSmsTemplate(SmsTemplateSaveReqVO createReqVO) {
        SmsChannelDO channelDO = validateSmsChannel(createReqVO.getChannelId());
        validateSmsTemplateCodeDuplicate(null, createReqVO.getCode());
        validateApiTemplate(createReqVO.getChannelId(), createReqVO.getApiTemplateId());
        SmsTemplateDO template = BeanUtils.toBean(createReqVO, SmsTemplateDO.class);
        template.setParams(parseTemplateContentParams(template.getContent()));
        template.setChannelCode(channelDO.getCode());
        smsTemplateMapper.insert(template);
        return template.getId();
    }

    /**
     * 更新 update Sms Template 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    @CacheEvict(cacheNames = RedisKeyConstants.SMS_TEMPLATE, allEntries = true)
    public void updateSmsTemplate(SmsTemplateSaveReqVO updateReqVO) {
        validateSmsTemplateExists(updateReqVO.getId());
        SmsChannelDO channelDO = validateSmsChannel(updateReqVO.getChannelId());
        validateSmsTemplateCodeDuplicate(updateReqVO.getId(), updateReqVO.getCode());
        validateApiTemplate(updateReqVO.getChannelId(), updateReqVO.getApiTemplateId());
        SmsTemplateDO updateObj = BeanUtils.toBean(updateReqVO, SmsTemplateDO.class);
        updateObj.setParams(parseTemplateContentParams(updateObj.getContent()));
        updateObj.setChannelCode(channelDO.getCode());
        smsTemplateMapper.updateById(updateObj);
    }

    /**
     * 删除 delete Sms Template 对应的数据。
     *
     * @param id id 参数
     */
    @CacheEvict(cacheNames = RedisKeyConstants.SMS_TEMPLATE, allEntries = true)
    public void deleteSmsTemplate(Long id) {
        validateSmsTemplateExists(id);
        smsTemplateMapper.deleteById(id);
    }

    /**
     * 删除 delete Sms Template List 对应的数据。
     *
     * @param ids ids 参数
     */
    @CacheEvict(cacheNames = RedisKeyConstants.SMS_TEMPLATE, allEntries = true)
    public void deleteSmsTemplateList(List<Long> ids) {
        smsTemplateMapper.deleteByIds(ids);
    }

    /**
     * 校验 validate Sms Template Exists 对应的业务规则。
     *
     * @param id id 参数
     */
    private void validateSmsTemplateExists(Long id) {
        if (smsTemplateMapper.selectById(id) == null) {
            throw exception(SMS_TEMPLATE_NOT_EXISTS);
        }
    }

    /**
     * 查询 get Sms Template 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public SmsTemplateDO getSmsTemplate(Long id) {
        return smsTemplateMapper.selectById(id);
    }

    /**
     * 查询 get Sms Template By Code From Cache 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    @Cacheable(cacheNames = RedisKeyConstants.SMS_TEMPLATE, key = "#code", unless = "#result == null")
    public SmsTemplateDO getSmsTemplateByCodeFromCache(String code) {
        return smsTemplateMapper.selectByCode(code);
    }

    /**
     * 查询 get Sms Template Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    public PageResult<SmsTemplateDO> getSmsTemplatePage(SmsTemplatePageReqVO pageReqVO) {
        return smsTemplateMapper.selectPage(pageReqVO);
    }

    /**
     * 查询 get Sms Template Count By Channel Id 对应的数据。
     *
     * @param channelId channelId 参数
     * @return 处理结果
     */
    public Long getSmsTemplateCountByChannelId(Long channelId) {
        return smsTemplateMapper.selectCountByChannelId(channelId);
    }

    /**
     * 校验 validate Sms Channel 对应的业务规则。
     *
     * @param channelId channelId 参数
     * @return 处理结果
     */
    @VisibleForTesting
    public SmsChannelDO validateSmsChannel(Long channelId) {
        SmsChannelDO channelDO = getSmsChannel(channelId);
        if (channelDO == null) {
            throw exception(SMS_CHANNEL_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(channelDO.getStatus())) {
            throw exception(SMS_CHANNEL_DISABLE);
        }
        return channelDO;
    }

    /**
     * 校验 validate Sms Template Code Duplicate 对应的业务规则。
     *
     * @param id id 参数
     * @param code code 参数
     */
    @VisibleForTesting
    public void validateSmsTemplateCodeDuplicate(Long id, String code) {
        SmsTemplateDO template = smsTemplateMapper.selectByCode(code);
        if (template == null) {
            return;
        }
        if (id == null) {
            throw exception(SMS_TEMPLATE_CODE_DUPLICATE, code);
        }
        if (!template.getId().equals(id)) {
            throw exception(SMS_TEMPLATE_CODE_DUPLICATE, code);
        }
    }

    /**
     * 校验 validate Api Template 对应的业务规则。
     *
     * @param channelId channelId 参数
     * @param apiTemplateId apiTemplateId 参数
     */
    @VisibleForTesting
    void validateApiTemplate(Long channelId, String apiTemplateId) {
        SmsClient smsClient = getSmsClient(channelId);
        Assert.notNull(smsClient, String.format("短信客户端(%d) 不存在", channelId));
        SmsTemplateRespDTO template;
        try {
            template = smsClient.getSmsTemplate(apiTemplateId);
        } catch (Throwable ex) {
            throw exception(SMS_TEMPLATE_API_ERROR, ExceptionUtil.getRootCauseMessage(ex));
        }
        if (template == null) {
            throw exception(SMS_TEMPLATE_API_NOT_FOUND);
        }
        if (Objects.equals(template.getAuditStatus(), SmsTemplateAuditStatusEnum.CHECKING.getStatus())) {
            throw exception(SMS_TEMPLATE_API_AUDIT_CHECKING);
        }
        if (Objects.equals(template.getAuditStatus(), SmsTemplateAuditStatusEnum.FAIL.getStatus())) {
            throw exception(SMS_TEMPLATE_API_AUDIT_FAIL, template.getAuditReason());
        }
        Assert.equals(template.getAuditStatus(), SmsTemplateAuditStatusEnum.SUCCESS.getStatus(),
                String.format("短信模板(%s) 审核状态(%d) 不正确", apiTemplateId, template.getAuditStatus()));
    }

    /**
     * 执行 format Sms Template Content 对应的业务操作。
     *
     * @param content content 参数
     * @param params params 参数
     * @return 处理结果
     */
    public String formatSmsTemplateContent(String content, Map<String, Object> params) {
        return StrUtil.format(content, params);
    }

    /**
     * 执行 parse Template Content Params 对应的业务操作。
     *
     * @param content content 参数
     * @return 处理结果
     */
    @VisibleForTesting
    public List<String> parseTemplateContentParams(String content) {
        return ReUtil.findAllGroup1(PATTERN_PARAMS, content);
    }

    /**
     * 创建 create Sms Log 对应的数据。
     *
     * @param mobile mobile 参数
     * @param userId userId 参数
     * @param userType userType 参数
     * @param isSend isSend 参数
     * @param template template 参数
     * @param templateContent templateContent 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    public Long createSmsLog(String mobile, Long userId, Integer userType, Boolean isSend,
                             SmsTemplateDO template, String templateContent, Map<String, Object> templateParams) {
        SmsLogDO.SmsLogDOBuilder logBuilder = SmsLogDO.builder();
        logBuilder.sendStatus(Objects.equals(isSend, true) ? SmsSendStatusEnum.INIT.getStatus()
                : SmsSendStatusEnum.IGNORE.getStatus());
        logBuilder.mobile(mobile).userId(userId).userType(userType);
        logBuilder.templateId(template.getId()).templateCode(template.getCode()).templateType(template.getType());
        logBuilder.templateContent(templateContent).templateParams(templateParams)
                .apiTemplateId(template.getApiTemplateId());
        logBuilder.channelId(template.getChannelId()).channelCode(template.getChannelCode());
        logBuilder.receiveStatus(SmsReceiveStatusEnum.INIT.getStatus());
        SmsLogDO logDO = logBuilder.build();
        smsLogMapper.insert(logDO);
        return logDO.getId();
    }

    /**
     * 更新 update Sms Send Result 对应的数据。
     *
     * @param id id 参数
     * @param success success 参数
     * @param apiSendCode apiSendCode 参数
     * @param apiSendMsg apiSendMsg 参数
     * @param apiRequestId apiRequestId 参数
     * @param apiSerialNo apiSerialNo 参数
     */
    public void updateSmsSendResult(Long id, Boolean success,
                                    String apiSendCode, String apiSendMsg,
                                    String apiRequestId, String apiSerialNo) {
        SmsSendStatusEnum sendStatus = success ? SmsSendStatusEnum.SUCCESS : SmsSendStatusEnum.FAILURE;
        smsLogMapper.updateById(SmsLogDO.builder().id(id)
                .sendStatus(sendStatus.getStatus()).sendTime(LocalDateTime.now())
                .apiSendCode(apiSendCode).apiSendMsg(apiSendMsg)
                .apiRequestId(apiRequestId).apiSerialNo(apiSerialNo).build());
    }

    /**
     * 更新 update Sms Receive Result 对应的数据。
     *
     * @param id id 参数
     * @param apiSerialNo apiSerialNo 参数
     * @param success success 参数
     * @param receiveTime receiveTime 参数
     * @param apiReceiveCode apiReceiveCode 参数
     * @param apiReceiveMsg apiReceiveMsg 参数
     */
    public void updateSmsReceiveResult(Long id, String apiSerialNo, Boolean success, LocalDateTime receiveTime,
                                       String apiReceiveCode, String apiReceiveMsg) {
        SmsReceiveStatusEnum receiveStatus = Objects.equals(success, true) ?
                SmsReceiveStatusEnum.SUCCESS : SmsReceiveStatusEnum.FAILURE;
        if (id == null || id == 0) {
            SmsLogDO log = smsLogMapper.selectByApiSerialNo(apiSerialNo);
            if (log == null) {
                return;
            }
            id = log.getId();
        }
        smsLogMapper.updateById(SmsLogDO.builder().id(id).receiveStatus(receiveStatus.getStatus())
                .receiveTime(receiveTime).apiReceiveCode(apiReceiveCode).apiReceiveMsg(apiReceiveMsg).build());
    }

    /**
     * 查询 get Sms Log 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public SmsLogDO getSmsLog(Long id) {
        return smsLogMapper.selectById(id);
    }

    /**
     * 查询 get Sms Log Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    public PageResult<SmsLogDO> getSmsLogPage(SmsLogPageReqVO pageReqVO) {
        return smsLogMapper.selectPage(pageReqVO);
    }

    /**
     * 发送 send Single Sms To Admin 对应的消息。
     *
     * @param mobile mobile 参数
     * @param userId userId 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    @DataPermission(enable = false)
    public Long sendSingleSmsToAdmin(String mobile, Long userId, String templateCode, Map<String, Object> templateParams) {
        if (StrUtil.isEmpty(mobile)) {
            AdminUserDO user = adminUserService.getUser(userId);
            if (user != null) {
                mobile = user.getMobile();
            }
        }
        return sendSingleSms(mobile, userId, UserTypeEnum.ADMIN.getValue(), templateCode, templateParams);
    }

    /**
     * 发送 send Single Sms To Member 对应的消息。
     *
     * @param mobile mobile 参数
     * @param userId userId 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    public Long sendSingleSmsToMember(String mobile, Long userId, String templateCode, Map<String, Object> templateParams) {
        if (StrUtil.isEmpty(mobile)) {
            mobile = memberApplicationService.getMemberUserMobile(userId);
        }
        return sendSingleSms(mobile, userId, UserTypeEnum.MEMBER.getValue(), templateCode, templateParams);
    }

    /**
     * 发送 send Single Sms 对应的消息。
     *
     * @param mobile mobile 参数
     * @param userId userId 参数
     * @param userType userType 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    public Long sendSingleSms(String mobile, Long userId, Integer userType,
                              String templateCode, Map<String, Object> templateParams) {
        SmsTemplateDO template = validateSmsTemplate(templateCode);
        SmsChannelDO smsChannel = validateSmsSendChannel(template.getChannelId());
        mobile = validateMobile(mobile);
        List<KeyValue<String, Object>> newTemplateParams = buildTemplateParams(template, templateParams);
        Boolean isSend = CommonStatusEnum.ENABLE.getStatus().equals(template.getStatus())
                && CommonStatusEnum.ENABLE.getStatus().equals(smsChannel.getStatus());
        String content = formatSmsTemplateContent(template.getContent(), templateParams);
        Long sendLogId = createSmsLog(mobile, userId, userType, isSend, template, content, templateParams);
        if (isSend) {
            smsProducer.sendSmsSendMessage(sendLogId, mobile, template.getChannelId(),
                    template.getApiTemplateId(), newTemplateParams);
        }
        return sendLogId;
    }

    /**
     * 校验 validate Sms Send Channel 对应的业务规则。
     *
     * @param channelId channelId 参数
     * @return 处理结果
     */
    @VisibleForTesting
    SmsChannelDO validateSmsSendChannel(Long channelId) {
        SmsChannelDO channelDO = getSmsChannel(channelId);
        if (channelDO == null) {
            throw exception(SMS_CHANNEL_NOT_EXISTS);
        }
        return channelDO;
    }

    /**
     * 校验 validate Sms Template 对应的业务规则。
     *
     * @param templateCode templateCode 参数
     * @return 处理结果
     */
    @VisibleForTesting
    public SmsTemplateDO validateSmsTemplate(String templateCode) {
        SmsTemplateDO template = getSmsTemplateByCodeFromCache(templateCode);
        if (template == null) {
            throw exception(SMS_SEND_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    /**
     * 构建 build Template Params 对应的数据对象。
     *
     * @param template template 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    @VisibleForTesting
    public List<KeyValue<String, Object>> buildTemplateParams(SmsTemplateDO template, Map<String, Object> templateParams) {
        return template.getParams().stream().map(key -> {
            Object value = templateParams.get(key);
            if (value == null) {
                throw exception(SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS, key);
            }
            return new KeyValue<>(key, value);
        }).collect(Collectors.toList());
    }

    /**
     * 校验 validate Mobile 对应的业务规则。
     *
     * @param mobile mobile 参数
     * @return 处理结果
     */
    @VisibleForTesting
    public String validateMobile(String mobile) {
        if (StrUtil.isEmpty(mobile)) {
            throw exception(SMS_SEND_MOBILE_NOT_EXISTS);
        }
        return mobile;
    }

    /**
     * 发送 send Batch Sms 对应的消息。
     *
     * @param mobiles mobiles 参数
     * @param userIds userIds 参数
     * @param userType userType 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     */
    public void sendBatchSms(List<String> mobiles, List<Long> userIds, Integer userType,
                             String templateCode, Map<String, Object> templateParams) {
        throw new UnsupportedOperationException("暂时不支持该操作，感兴趣可以实现该功能哟！");
    }

    /**
     * 执行 do Send Sms 对应的业务操作。
     *
     * @param message message 参数
     */
    public void doSendSms(SmsSendMessage message) {
        SmsClient smsClient = getSmsClient(message.getChannelId());
        Assert.notNull(smsClient, "短信客户端({}) 不存在", message.getChannelId());
        try {
            SmsSendRespDTO sendResponse = smsClient.sendSms(message.getLogId(), message.getMobile(),
                    message.getApiTemplateId(), message.getTemplateParams());
            updateSmsSendResult(message.getLogId(), sendResponse.getSuccess(),
                    sendResponse.getApiCode(), sendResponse.getApiMsg(),
                    sendResponse.getApiRequestId(), sendResponse.getSerialNo());
        } catch (Throwable ex) {
            log.error("[doSendSms][发送短信异常，日志编号({})]", message.getLogId(), ex);
            updateSmsSendResult(message.getLogId(), false,
                    "EXCEPTION", ExceptionUtil.getRootCauseMessage(ex), null, null);
        }
    }

    /**
     * 执行 receive Sms Status 对应的业务操作。
     *
     * @param channelCode channelCode 参数
     * @param text text 参数
     */
    public void receiveSmsStatus(String channelCode, String text) throws Throwable {
        SmsClient smsClient = getSmsClient(channelCode);
        Assert.notNull(smsClient, "短信客户端({}) 不存在", channelCode);
        List<SmsReceiveRespDTO> receiveResults = smsClient.parseSmsReceiveStatus(text);
        if (CollUtil.isEmpty(receiveResults)) {
            return;
        }
        receiveResults.forEach(result -> updateSmsReceiveResult(result.getLogId(), result.getSerialNo(),
                result.getSuccess(), result.getReceiveTime(), result.getErrorCode(), result.getErrorMsg()));
    }

    /**
     * 发送 send Sms Code 对应的消息。
     *
     * @param reqDTO reqDTO 参数
     */
    public void sendSmsCode(SmsCodeSendReqDTO reqDTO) {
        SmsSceneEnum sceneEnum = SmsSceneEnum.getCodeByScene(reqDTO.getScene());
        Assert.notNull(sceneEnum, "验证码场景({}) 查找不到配置", reqDTO.getScene());
        String code = createSmsCode(reqDTO.getMobile(), reqDTO.getScene(), reqDTO.getCreateIp());
        sendSingleSms(reqDTO.getMobile(), null, null, sceneEnum.getTemplateCode(), MapUtil.of("code", code));
    }

    /**
     * 创建 create Sms Code 对应的数据。
     *
     * @param mobile mobile 参数
     * @param scene scene 参数
     * @param ip ip 参数
     * @return 处理结果
     */
    private String createSmsCode(String mobile, Integer scene, String ip) {
        SmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, null, null);
        if (lastSmsCode != null) {
            if (LocalDateTimeUtil.between(lastSmsCode.getCreateTime(), LocalDateTime.now()).toMillis()
                    < smsCodeProperties.getSendFrequency().toMillis()) {
                throw exception(SMS_CODE_SEND_TOO_FAST);
            }
            if (isToday(lastSmsCode.getCreateTime())
                    && lastSmsCode.getTodayIndex() >= smsCodeProperties.getSendMaximumQuantityPerDay()) {
                throw exception(SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY);
            }
        }
        String code = String.format("%0" + smsCodeProperties.getEndCode().toString().length() + "d",
                randomInt(smsCodeProperties.getBeginCode(), smsCodeProperties.getEndCode() + 1));
        SmsCodeDO newSmsCode = SmsCodeDO.builder().mobile(mobile).code(code).scene(scene)
                .todayIndex(lastSmsCode != null && isToday(lastSmsCode.getCreateTime()) ? lastSmsCode.getTodayIndex() + 1 : 1)
                .createIp(ip).used(false).build();
        smsCodeMapper.insert(newSmsCode);
        return code;
    }

    /**
     * 执行 use Sms Code 对应的业务操作。
     *
     * @param reqDTO reqDTO 参数
     */
    public void useSmsCode(SmsCodeUseReqDTO reqDTO) {
        SmsCodeDO lastSmsCode = validateSmsCode0(reqDTO.getMobile(), reqDTO.getCode(), reqDTO.getScene());
        smsCodeMapper.updateById(SmsCodeDO.builder().id(lastSmsCode.getId())
                .used(true).usedTime(LocalDateTime.now()).usedIp(reqDTO.getUsedIp()).build());
    }

    /**
     * 校验 validate Sms Code 对应的业务规则。
     *
     * @param reqDTO reqDTO 参数
     */
    public void validateSmsCode(SmsCodeValidateReqDTO reqDTO) {
        validateSmsCode0(reqDTO.getMobile(), reqDTO.getCode(), reqDTO.getScene());
    }

    /**
     * 校验 validate Sms Code0 对应的业务规则。
     *
     * @param mobile mobile 参数
     * @param code code 参数
     * @param scene scene 参数
     * @return 处理结果
     */
    private SmsCodeDO validateSmsCode0(String mobile, String code, Integer scene) {
        SmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, code, scene);
        if (lastSmsCode == null) {
            throw exception(SMS_CODE_NOT_FOUND);
        }
        if (LocalDateTimeUtil.between(lastSmsCode.getCreateTime(), LocalDateTime.now()).toMillis()
                >= smsCodeProperties.getExpireTimes().toMillis()) {
            throw exception(SMS_CODE_EXPIRED);
        }
        if (Boolean.TRUE.equals(lastSmsCode.getUsed())) {
            throw exception(SMS_CODE_USED);
        }
        return lastSmsCode;
    }

}
