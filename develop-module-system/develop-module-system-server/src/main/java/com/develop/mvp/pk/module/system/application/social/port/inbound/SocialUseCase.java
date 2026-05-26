package com.develop.mvp.pk.module.system.application.social.port.inbound;

// DDD 角色：入站端口 — 定义 Social 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserBindReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserRespDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxQrcodeReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaOrderNotifyConfirmReceiveReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaOrderUploadShippingInfoReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.client.SocialClientPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.client.SocialClientSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.user.SocialUserPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.social.SocialClientDO;
import com.develop.mvp.pk.module.system.dal.dataobject.social.SocialUserDO;
import jakarta.validation.Valid;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.subscribemsg.TemplateInfo;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;

import java.util.List;
/**
 * Social 聚合的入站用例端口。
 */
public interface SocialUseCase {

    /**
     * 查询 get Authorize Url 对应的数据。
     *
     * @param socialType socialType 参数
     * @param userType userType 参数
     * @param redirectUri redirectUri 参数
     * @return 处理结果
     */
    String getAuthorizeUrl(Integer socialType, Integer userType, String redirectUri);

    /**
     * 查询 get Auth User 对应的数据。
     *
     * @param socialType socialType 参数
     * @param userType userType 参数
     * @param code code 参数
     * @param state state 参数
     * @return 处理结果
     */
    AuthUser getAuthUser(Integer socialType, Integer userType, String code, String state);

    /**
     * 执行 build Auth Request 对应的业务操作。
     *
     * @param socialType socialType 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    AuthRequest buildAuthRequest(Integer socialType, Integer userType);

    /**
     * 创建 create Wx Mp Jsapi Signature 对应的数据。
     *
     * @param userType userType 参数
     * @param url url 参数
     * @return 处理结果
     */
    WxJsapiSignature createWxMpJsapiSignature(Integer userType, String url);

    /**
     * 查询 get Wx Ma Phone Number Info 对应的数据。
     *
     * @param userType userType 参数
     * @param phoneCode phoneCode 参数
     * @return 处理结果
     */
    WxMaPhoneNumberInfo getWxMaPhoneNumberInfo(Integer userType, String phoneCode);

    /**
     * 查询 get Wxa Qrcode 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    byte[] getWxaQrcode(SocialWxQrcodeReqDTO reqVO);

    /**
     * 查询 get Subscribe Template List 对应的数据。
     *
     * @param userType userType 参数
     * @return 处理结果
     */
    List<TemplateInfo> getSubscribeTemplateList(Integer userType);

    /**
     * 发送 send Subscribe Message 对应的消息。
     *
     * @param reqDTO reqDTO 参数
     * @param templateId templateId 参数
     * @param openId openId 参数
     */
    void sendSubscribeMessage(SocialWxaSubscribeMessageSendReqDTO reqDTO, String templateId, String openId);

    /**
     * 执行 upload Wxa Order Shipping Info 对应的业务操作。
     *
     * @param userType userType 参数
     * @param reqDTO reqDTO 参数
     */
    void uploadWxaOrderShippingInfo(Integer userType, SocialWxaOrderUploadShippingInfoReqDTO reqDTO);

    /**
     * 执行 notify Wxa Order Confirm Receive 对应的业务操作。
     *
     * @param userType userType 参数
     * @param reqDTO reqDTO 参数
     */
    void notifyWxaOrderConfirmReceive(Integer userType, SocialWxaOrderNotifyConfirmReceiveReqDTO reqDTO);

    /**
     * 创建 create Social Client 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createSocialClient(@Valid SocialClientSaveReqVO createReqVO);

    /**
     * 更新 update Social Client 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateSocialClient(@Valid SocialClientSaveReqVO updateReqVO);

    /**
     * 删除 delete Social Client 对应的数据。
     *
     * @param id id 参数
     */
    void deleteSocialClient(Long id);

    /**
     * 删除 delete Social Client List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteSocialClientList(List<Long> ids);

    /**
     * 查询 get Social Client 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    SocialClientDO getSocialClient(Long id);

    /**
     * 查询 get Social Client Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<SocialClientDO> getSocialClientPage(SocialClientPageReqVO pageReqVO);

    /**
     * 查询 get Social User List 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    List<SocialUserDO> getSocialUserList(Long userId, Integer userType);

    /**
     * 执行 bind Social User 对应的业务操作。
     *
     * @param reqDTO reqDTO 参数
     * @return 处理结果
     */
    String bindSocialUser(@Valid SocialUserBindReqDTO reqDTO);

    /**
     * 执行 unbind Social User 对应的业务操作。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param socialType socialType 参数
     * @param openid openid 参数
     */
    void unbindSocialUser(Long userId, Integer userType, Integer socialType, String openid);

    /**
     * 查询 get Social User By User Id 对应的数据。
     *
     * @param userType userType 参数
     * @param userId userId 参数
     * @param socialType socialType 参数
     * @return 处理结果
     */
    SocialUserRespDTO getSocialUserByUserId(Integer userType, Long userId, Integer socialType);

    /**
     * 查询 get Social User By Code 对应的数据。
     *
     * @param userType userType 参数
     * @param socialType socialType 参数
     * @param code code 参数
     * @param state state 参数
     * @return 处理结果
     */
    SocialUserRespDTO getSocialUserByCode(Integer userType, Integer socialType, String code, String state);

    /**
     * 处理 auth Social User 对应的认证流程。
     *
     * @param socialType socialType 参数
     * @param userType userType 参数
     * @param code code 参数
     * @param state state 参数
     * @return 处理结果
     */
    SocialUserDO authSocialUser(Integer socialType, Integer userType, String code, String state);

    /**
     * 查询 get Social User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    SocialUserDO getSocialUser(Long id);

    /**
     * 查询 get Social User Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<SocialUserDO> getSocialUserPage(SocialUserPageReqVO pageReqVO);
}
