package com.develop.mvp.pk.module.system.application.notify.service;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.application.notify.port.inbound.NotifyUseCase;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.message.NotifyMessageMyPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.template.NotifyTemplatePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.template.NotifyTemplateSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.notify.NotifyMessageDO;
import com.develop.mvp.pk.module.system.dal.dataobject.notify.NotifyTemplateDO;
import com.develop.mvp.pk.module.system.dal.mysql.notify.NotifyMessageMapper;
import com.develop.mvp.pk.module.system.dal.mysql.notify.NotifyTemplateMapper;
import com.develop.mvp.pk.module.system.dal.redis.RedisKeyConstants;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

/**
 * Notify Application Service 应用服务。
 */
@Slf4j
public class NotifyApplicationService implements NotifyUseCase {

    private static final Pattern PATTERN_PARAMS = Pattern.compile("\\{(.*?)}");

    private final NotifyMessageMapper notifyMessageMapper;
    private final NotifyTemplateMapper notifyTemplateMapper;

    /**
     * 创建 NotifyApplicationService 实例。
     *
     * @param notifyMessageMapper notifyMessageMapper 参数
     * @param notifyTemplateMapper notifyTemplateMapper 参数
     */
    public NotifyApplicationService(NotifyMessageMapper notifyMessageMapper,
                                     NotifyTemplateMapper notifyTemplateMapper) {
        this.notifyMessageMapper = notifyMessageMapper;
        this.notifyTemplateMapper = notifyTemplateMapper;
    }

    /**
     * 创建 create Notify Message 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param template template 参数
     * @param templateContent templateContent 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    public Long createNotifyMessage(Long userId, Integer userType,
                                    NotifyTemplateDO template, String templateContent, Map<String, Object> templateParams) {
        NotifyMessageDO message = new NotifyMessageDO().setUserId(userId).setUserType(userType)
                .setTemplateId(template.getId()).setTemplateCode(template.getCode())
                .setTemplateType(template.getType()).setTemplateNickname(template.getNickname())
                .setTemplateContent(templateContent).setTemplateParams(templateParams).setReadStatus(false);
        notifyMessageMapper.insert(message);
        return message.getId();
    }

    /**
     * 查询 get Notify Message Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    public PageResult<NotifyMessageDO> getNotifyMessagePage(NotifyMessagePageReqVO pageReqVO) {
        return notifyMessageMapper.selectPage(pageReqVO);
    }

    /**
     * 查询 get My My Notify Message Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    public PageResult<NotifyMessageDO> getMyMyNotifyMessagePage(NotifyMessageMyPageReqVO pageReqVO, Long userId, Integer userType) {
        return notifyMessageMapper.selectPage(pageReqVO, userId, userType);
    }

    /**
     * 查询 get Notify Message 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public NotifyMessageDO getNotifyMessage(Long id) {
        return notifyMessageMapper.selectById(id);
    }

    /**
     * 查询 get Unread Notify Message List 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param size size 参数
     * @return 处理结果
     */
    public List<NotifyMessageDO> getUnreadNotifyMessageList(Long userId, Integer userType, Integer size) {
        return notifyMessageMapper.selectUnreadListByUserIdAndUserType(userId, userType, size);
    }

    /**
     * 查询 get Unread Notify Message Count 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    public Long getUnreadNotifyMessageCount(Long userId, Integer userType) {
        return notifyMessageMapper.selectUnreadCountByUserIdAndUserType(userId, userType);
    }

    /**
     * 更新 update Notify Message Read 对应的数据。
     *
     * @param ids ids 参数
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    public int updateNotifyMessageRead(Collection<Long> ids, Long userId, Integer userType) {
        return notifyMessageMapper.updateListRead(ids, userId, userType);
    }

    /**
     * 更新 update All Notify Message Read 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    public int updateAllNotifyMessageRead(Long userId, Integer userType) {
        return notifyMessageMapper.updateListRead(userId, userType);
    }

    /**
     * 创建 create Notify Template 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    public Long createNotifyTemplate(NotifyTemplateSaveReqVO createReqVO) {
        validateNotifyTemplateCodeDuplicate(null, createReqVO.getCode());
        NotifyTemplateDO notifyTemplate = BeanUtils.toBean(createReqVO, NotifyTemplateDO.class);
        notifyTemplate.setParams(parseTemplateContentParams(notifyTemplate.getContent()));
        notifyTemplateMapper.insert(notifyTemplate);
        return notifyTemplate.getId();
    }

    /**
     * 更新 update Notify Template 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    @CacheEvict(cacheNames = RedisKeyConstants.NOTIFY_TEMPLATE, allEntries = true)
    public void updateNotifyTemplate(NotifyTemplateSaveReqVO updateReqVO) {
        validateNotifyTemplateExists(updateReqVO.getId());
        validateNotifyTemplateCodeDuplicate(updateReqVO.getId(), updateReqVO.getCode());
        NotifyTemplateDO updateObj = BeanUtils.toBean(updateReqVO, NotifyTemplateDO.class);
        updateObj.setParams(parseTemplateContentParams(updateObj.getContent()));
        notifyTemplateMapper.updateById(updateObj);
    }

    /**
     * 删除 delete Notify Template 对应的数据。
     *
     * @param id id 参数
     */
    @CacheEvict(cacheNames = RedisKeyConstants.NOTIFY_TEMPLATE, allEntries = true)
    public void deleteNotifyTemplate(Long id) {
        validateNotifyTemplateExists(id);
        notifyTemplateMapper.deleteById(id);
    }

    /**
     * 删除 delete Notify Template List 对应的数据。
     *
     * @param ids ids 参数
     */
    @CacheEvict(cacheNames = RedisKeyConstants.NOTIFY_TEMPLATE, allEntries = true)
    public void deleteNotifyTemplateList(List<Long> ids) {
        notifyTemplateMapper.deleteByIds(ids);
    }

    /**
     * 查询 get Notify Template 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public NotifyTemplateDO getNotifyTemplate(Long id) {
        return notifyTemplateMapper.selectById(id);
    }

    /**
     * 查询 get Notify Template By Code From Cache 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    @Cacheable(cacheNames = RedisKeyConstants.NOTIFY_TEMPLATE, key = "#code", unless = "#result == null")
    public NotifyTemplateDO getNotifyTemplateByCodeFromCache(String code) {
        return notifyTemplateMapper.selectByCode(code);
    }

    /**
     * 查询 get Notify Template Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    public PageResult<NotifyTemplateDO> getNotifyTemplatePage(NotifyTemplatePageReqVO pageReqVO) {
        return notifyTemplateMapper.selectPage(pageReqVO);
    }

    /**
     * 执行 format Notify Template Content 对应的业务操作。
     *
     * @param content content 参数
     * @param params params 参数
     * @return 处理结果
     */
    public String formatNotifyTemplateContent(String content, Map<String, Object> params) {
        return StrUtil.format(content, params);
    }

    /**
     * 发送 send Single Notify To Admin 对应的消息。
     *
     * @param userId userId 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    public Long sendSingleNotifyToAdmin(Long userId, String templateCode, Map<String, Object> templateParams) {
        return sendSingleNotify(userId, UserTypeEnum.ADMIN.getValue(), templateCode, templateParams);
    }

    /**
     * 发送 send Single Notify To Member 对应的消息。
     *
     * @param userId userId 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    public Long sendSingleNotifyToMember(Long userId, String templateCode, Map<String, Object> templateParams) {
        return sendSingleNotify(userId, UserTypeEnum.MEMBER.getValue(), templateCode, templateParams);
    }

    /**
     * 发送 send Single Notify 对应的消息。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    public Long sendSingleNotify(Long userId, Integer userType, String templateCode, Map<String, Object> templateParams) {
        NotifyTemplateDO template = validateNotifyTemplate(templateCode);
        if (Objects.equals(template.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            log.info("[sendSingleNotify][模版({})已经关闭，无法给用户({}/{})发送]", templateCode, userId, userType);
            return null;
        }
        validateTemplateParams(template, templateParams);
        String content = formatNotifyTemplateContent(template.getContent(), templateParams);
        return createNotifyMessage(userId, userType, template, content, templateParams);
    }

    /**
     * 发送 send Batch Notify 对应的消息。
     *
     * @param mobiles mobiles 参数
     * @param userIds userIds 参数
     * @param userType userType 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     */
    public void sendBatchNotify(List<String> mobiles, List<Long> userIds, Integer userType,
                                String templateCode, Map<String, Object> templateParams) {
        throw new UnsupportedOperationException("暂时不支持该操作，感兴趣可以实现该功能哟！");
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
     * 校验 validate Notify Template Code Duplicate 对应的业务规则。
     *
     * @param id id 参数
     * @param code code 参数
     */
    @VisibleForTesting
    public void validateNotifyTemplateCodeDuplicate(Long id, String code) {
        NotifyTemplateDO template = notifyTemplateMapper.selectByCode(code);
        if (template == null) {
            return;
        }
        if (id == null) {
            throw exception(NOTIFY_TEMPLATE_CODE_DUPLICATE, code);
        }
        if (!template.getId().equals(id)) {
            throw exception(NOTIFY_TEMPLATE_CODE_DUPLICATE, code);
        }
    }

    /**
     * 校验 validate Notify Template 对应的业务规则。
     *
     * @param templateCode templateCode 参数
     * @return 处理结果
     */
    @VisibleForTesting
    public NotifyTemplateDO validateNotifyTemplate(String templateCode) {
        NotifyTemplateDO template = getNotifyTemplateByCodeFromCache(templateCode);
        if (template == null) {
            throw exception(NOTICE_NOT_FOUND);
        }
        return template;
    }

    /**
     * 校验 validate Template Params 对应的业务规则。
     *
     * @param template template 参数
     * @param templateParams templateParams 参数
     */
    @VisibleForTesting
    public void validateTemplateParams(NotifyTemplateDO template, Map<String, Object> templateParams) {
        template.getParams().forEach(key -> {
            Object value = templateParams.get(key);
            if (value == null) {
                throw exception(NOTIFY_SEND_TEMPLATE_PARAM_MISS, key);
            }
        });
    }

    /**
     * 校验 validate Notify Template Exists 对应的业务规则。
     *
     * @param id id 参数
     */
    private void validateNotifyTemplateExists(Long id) {
        if (notifyTemplateMapper.selectById(id) == null) {
            throw exception(NOTIFY_TEMPLATE_NOT_EXISTS);
        }
    }
}
