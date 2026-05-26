package com.develop.mvp.pk.module.system.application.notify.port.inbound;

// DDD 角色：入站端口 — 定义 Notify 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.message.NotifyMessageMyPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.template.NotifyTemplatePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.template.NotifyTemplateSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.notify.NotifyMessageDO;
import com.develop.mvp.pk.module.system.dal.dataobject.notify.NotifyTemplateDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
/**
 * Notify 聚合的入站用例端口。
 */
public interface NotifyUseCase {

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
    Long createNotifyMessage(Long userId, Integer userType,
                             NotifyTemplateDO template, String templateContent, Map<String, Object> templateParams);

    /**
     * 查询 get Notify Message Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<NotifyMessageDO> getNotifyMessagePage(NotifyMessagePageReqVO pageReqVO);

    /**
     * 查询 get My My Notify Message Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    PageResult<NotifyMessageDO> getMyMyNotifyMessagePage(NotifyMessageMyPageReqVO pageReqVO,
                                                          Long userId, Integer userType);

    /**
     * 查询 get Notify Message 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    NotifyMessageDO getNotifyMessage(Long id);

    /**
     * 查询 get Unread Notify Message List 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param size size 参数
     * @return 处理结果
     */
    List<NotifyMessageDO> getUnreadNotifyMessageList(Long userId, Integer userType, Integer size);

    /**
     * 查询 get Unread Notify Message Count 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    Long getUnreadNotifyMessageCount(Long userId, Integer userType);

    /**
     * 更新 update Notify Message Read 对应的数据。
     *
     * @param ids ids 参数
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    int updateNotifyMessageRead(Collection<Long> ids, Long userId, Integer userType);

    /**
     * 更新 update All Notify Message Read 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    int updateAllNotifyMessageRead(Long userId, Integer userType);

    /**
     * 创建 create Notify Template 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createNotifyTemplate(NotifyTemplateSaveReqVO createReqVO);

    /**
     * 更新 update Notify Template 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateNotifyTemplate(NotifyTemplateSaveReqVO updateReqVO);

    /**
     * 删除 delete Notify Template 对应的数据。
     *
     * @param id id 参数
     */
    void deleteNotifyTemplate(Long id);

    /**
     * 删除 delete Notify Template List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteNotifyTemplateList(List<Long> ids);

    /**
     * 查询 get Notify Template 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    NotifyTemplateDO getNotifyTemplate(Long id);

    /**
     * 查询 get Notify Template By Code From Cache 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    NotifyTemplateDO getNotifyTemplateByCodeFromCache(String code);

    /**
     * 查询 get Notify Template Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<NotifyTemplateDO> getNotifyTemplatePage(NotifyTemplatePageReqVO pageReqVO);

    /**
     * 执行 format Notify Template Content 对应的业务操作。
     *
     * @param content content 参数
     * @param params params 参数
     * @return 处理结果
     */
    String formatNotifyTemplateContent(String content, Map<String, Object> params);

    /**
     * 发送 send Single Notify To Admin 对应的消息。
     *
     * @param userId userId 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    Long sendSingleNotifyToAdmin(Long userId, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送 send Single Notify To Member 对应的消息。
     *
     * @param userId userId 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    Long sendSingleNotifyToMember(Long userId, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送 send Single Notify 对应的消息。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    Long sendSingleNotify(Long userId, Integer userType, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送 send Batch Notify 对应的消息。
     *
     * @param mobiles mobiles 参数
     * @param userIds userIds 参数
     * @param userType userType 参数
     * @param templateCode templateCode 参数
     * @param templateParams templateParams 参数
     */
    void sendBatchNotify(List<String> mobiles, List<Long> userIds, Integer userType,
                         String templateCode, Map<String, Object> templateParams);
}
