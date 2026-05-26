package com.develop.mvp.pk.module.system.application.sms.port.inbound;

// DDD 角色：入站端口 — 定义 Sms 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.core.KeyValue;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.channel.SmsChannelPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.channel.SmsChannelSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.log.SmsLogPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.template.SmsTemplatePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.template.SmsTemplateSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsChannelDO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsLogDO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsTemplateDO;
import com.develop.mvp.pk.module.system.domain.sms.SmsChannel;
import com.develop.mvp.pk.module.system.framework.sms.core.client.SmsClient;
import com.develop.mvp.pk.module.system.mq.message.sms.SmsSendMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
/**
 * Sms 聚合的入站用例端口。
 */
public interface SmsUseCase {

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
    Long createChannel(String code, String signature, Integer status,
                       String apiKey, String apiSecret, String callbackUrl, String remark);

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
    void updateChannel(Long id, String code, String signature, Integer status,
                       String apiKey, String apiSecret, String callbackUrl, String remark);

    /**
     * 删除 delete Channel 对应的数据。
     *
     * @param id id 参数
     */
    void deleteChannel(Long id);

    /**
     * 查询 get Channel 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    SmsChannel getChannel(Long id);

    /**
     * 查询 get Channel By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    SmsChannel getChannelByCode(String code);

    /**
     * 查询 get Channel List 对应的数据。
     *
     * @return 处理结果
     */
    List<SmsChannel> getChannelList();

    /**
     * 查询 get Channel Page 对应的数据。
     *
     * @param signature signature 参数
     * @param status status 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    PageResult<SmsChannel> getChannelPage(String signature, Integer status, Integer pageNo, Integer pageSize);

    /**
     * 创建 create Sms Channel 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createSmsChannel(SmsChannelSaveReqVO createReqVO);

    /**
     * 更新 update Sms Channel 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateSmsChannel(SmsChannelSaveReqVO updateReqVO);

    /**
     * 删除 delete Sms Channel 对应的数据。
     *
     * @param id id 参数
     */
    void deleteSmsChannel(Long id);

    /**
     * 删除 delete Sms Channel List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteSmsChannelList(List<Long> ids);

    /**
     * 查询 get Sms Channel 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    SmsChannelDO getSmsChannel(Long id);

    /**
     * 查询 get Sms Channel List 对应的数据。
     *
     * @return 处理结果
     */
    List<SmsChannelDO> getSmsChannelList();

    /**
     * 查询 get Sms Channel Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<SmsChannelDO> getSmsChannelPage(SmsChannelPageReqVO pageReqVO);

    /**
     * 查询 get Sms Client 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    SmsClient getSmsClient(Long id);

    /**
     * 查询 get Sms Client 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    SmsClient getSmsClient(String code);

    /**
     * 创建 create Sms Template 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createSmsTemplate(SmsTemplateSaveReqVO createReqVO);

    /**
     * 更新 update Sms Template 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateSmsTemplate(SmsTemplateSaveReqVO updateReqVO);

    /**
     * 删除 delete Sms Template 对应的数据。
     *
     * @param id id 参数
     */
    void deleteSmsTemplate(Long id);

    /**
     * 删除 delete Sms Template List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteSmsTemplateList(List<Long> ids);

    /**
     * 查询 get Sms Template 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    SmsTemplateDO getSmsTemplate(Long id);

    /**
     * 查询 get Sms Template By Code From Cache 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    SmsTemplateDO getSmsTemplateByCodeFromCache(String code);

    /**
     * 查询 get Sms Template Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<SmsTemplateDO> getSmsTemplatePage(SmsTemplatePageReqVO pageReqVO);

    /**
     * 查询 get Sms Template Count By Channel Id 对应的数据。
     *
     * @param channelId channelId 参数
     * @return 处理结果
     */
    Long getSmsTemplateCountByChannelId(Long channelId);

    /**
     * 执行 format Sms Template Content 对应的业务操作。
     *
     * @param content content 参数
     * @param params params 参数
     * @return 处理结果
     */
    String formatSmsTemplateContent(String content, Map<String, Object> params);

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
    Long createSmsLog(String mobile, Long userId, Integer userType, Boolean isSend,
                      SmsTemplateDO template, String templateContent, Map<String, Object> templateParams);

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
    void updateSmsSendResult(Long id, Boolean success,
                             String apiSendCode, String apiSendMsg,
                             String apiRequestId, String apiSerialNo);

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
    void updateSmsReceiveResult(Long id, String apiSerialNo, Boolean success, LocalDateTime receiveTime,
                                String apiReceiveCode, String apiReceiveMsg);

    /**
     * 查询 get Sms Log 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    SmsLogDO getSmsLog(Long id);

    /**
     * 查询 get Sms Log Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<SmsLogDO> getSmsLogPage(SmsLogPageReqVO pageReqVO);

    /**
     * 发送 send Single Sms To Admin 对应的消息。
     *
     * @param mobile mobile 参数
     * @param userId userId 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    Long sendSingleSmsToAdmin(String mobile, Long userId, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送 send Single Sms To Member 对应的消息。
     *
     * @param mobile mobile 参数
     * @param userId userId 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    Long sendSingleSmsToMember(String mobile, Long userId, String templateCode, Map<String, Object> templateParams);

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
    Long sendSingleSms(String mobile, Long userId, Integer userType,
                       String templateCode, Map<String, Object> templateParams);

    /**
     * 发送 send Batch Sms 对应的消息。
     *
     * @param mobiles mobiles 参数
     * @param userIds userIds 参数
     * @param userType userType 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     */
    void sendBatchSms(List<String> mobiles, List<Long> userIds, Integer userType,
                      String templateCode, Map<String, Object> templateParams);

    /**
     * 执行 do Send Sms 对应的业务操作。
     *
     * @param message message 参数
     */
    void doSendSms(SmsSendMessage message);

    /**
     * 执行 receive Sms Status 对应的业务操作。
     *
     * @param channelCode channelCode 参数
     * @param text text 参数
     */
    void receiveSmsStatus(String channelCode, String text) throws Throwable;

    /**
     * 发送 send Sms Code 对应的消息。
     *
     * @param reqDTO reqDTO 参数
     */
    void sendSmsCode(SmsCodeSendReqDTO reqDTO);

    /**
     * 执行 use Sms Code 对应的业务操作。
     *
     * @param reqDTO reqDTO 参数
     */
    void useSmsCode(SmsCodeUseReqDTO reqDTO);

    /**
     * 校验 validate Sms Code 对应的业务规则。
     *
     * @param reqDTO reqDTO 参数
     */
    void validateSmsCode(SmsCodeValidateReqDTO reqDTO);
}
