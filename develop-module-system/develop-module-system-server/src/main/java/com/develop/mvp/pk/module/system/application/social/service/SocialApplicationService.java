package com.develop.mvp.pk.module.system.application.social.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaSubscribeService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.ContactBean;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.OrderKeyBean;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.PayerBean;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.ShippingListBean;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.WxMaOrderShippingInfoNotifyConfirmRequest;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.WxMaOrderShippingInfoUploadRequest;
import cn.binarywang.wx.miniapp.bean.shop.response.WxMaOrderShippingInfoBaseResponse;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedisBetterConfigImpl;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReflectUtil;
import com.binarywang.spring.starter.wxjava.miniapp.properties.WxMaProperties;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.exception.ServiceException;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.cache.CacheUtils;
import com.develop.mvp.pk.framework.common.util.http.HttpUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserBindReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserRespDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxQrcodeReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaOrderNotifyConfirmReceiveReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaOrderUploadShippingInfoReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import com.develop.mvp.pk.module.system.application.social.port.inbound.SocialUseCase;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.client.SocialClientPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.client.SocialClientSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.user.SocialUserPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.social.SocialClientDO;
import com.develop.mvp.pk.module.system.dal.dataobject.social.SocialUserBindDO;
import com.develop.mvp.pk.module.system.dal.dataobject.social.SocialUserDO;
import com.develop.mvp.pk.module.system.dal.mysql.social.SocialClientMapper;
import com.develop.mvp.pk.module.system.dal.mysql.social.SocialUserBindMapper;
import com.develop.mvp.pk.module.system.dal.mysql.social.SocialUserMapper;
import com.develop.mvp.pk.module.system.dal.redis.RedisKeyConstants;
import com.develop.mvp.pk.module.system.enums.social.SocialTypeEnum;
import com.develop.mvp.pk.module.system.framework.justauth.core.AuthRequestFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.subscribemsg.TemplateInfo;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthAlipayRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;
import static com.develop.mvp.pk.framework.common.util.collection.MapUtils.findAndThen;
import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.UTC_MS_WITH_XXX_OFFSET_FORMATTER;
import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.toEpochSecond;
import static com.develop.mvp.pk.framework.common.util.json.JsonUtils.toJsonString;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;

/**
 * Social Application Service 应用服务。
 */
@Validated
@Slf4j
public class SocialApplicationService implements SocialUseCase {

    private static final long[] UPLOAD_SHIPPING_INFO_RETRY_BACKOFF_MILLIS = {1000, 2000, 4000};
    private static final int WX_ERR_CODE_PAY_ORDER_NOT_EXIST = 10060001;

    private final String envVersion;
    private final String miniprogramState;

    private final AuthRequestFactory authRequestFactory;

    private final WxMpService wxMpService;
    private final WxMpProperties wxMpProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final WxMaService wxMaService;
    private final WxMaProperties wxMaProperties;
    private final SocialClientMapper socialClientMapper;
    private final SocialUserBindMapper socialUserBindMapper;
    private final SocialUserMapper socialUserMapper;

    private final LoadingCache<String, WxMpService> wxMpServiceCache;
    private final LoadingCache<String, WxMaService> wxMaServiceCache;

    /**
     * 创建 SocialApplicationService 实例。
     *
     * @param authRequestFactory authRequestFactory 参数
     * @param wxMpService wxMpService 参数
     * @param wxMpProperties wxMpProperties 参数
     * @param stringRedisTemplate stringRedisTemplate 参数
     * @param wxMaService wxMaService 参数
     * @param wxMaProperties wxMaProperties 参数
     * @param socialClientMapper socialClientMapper 参数
     * @param socialUserBindMapper socialUserBindMapper 参数
     * @param socialUserMapper socialUserMapper 参数
     * @param envVersion envVersion 参数
     * @param miniprogramState miniprogramState 参数
     */
    public SocialApplicationService(
            @Autowired(required = false) AuthRequestFactory authRequestFactory,
            WxMpService wxMpService,
            WxMpProperties wxMpProperties,
            StringRedisTemplate stringRedisTemplate,
            WxMaService wxMaService,
            WxMaProperties wxMaProperties,
            SocialClientMapper socialClientMapper,
            SocialUserBindMapper socialUserBindMapper,
            SocialUserMapper socialUserMapper,
            @Value("${develop.wxa-code.env-version:release}") String envVersion,
            @Value("${develop.wxa-subscribe-message.miniprogram-state:formal}") String miniprogramState) {
        this.authRequestFactory = authRequestFactory;
        this.wxMpService = wxMpService;
        this.wxMpProperties = wxMpProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.wxMaService = wxMaService;
        this.wxMaProperties = wxMaProperties;
        this.socialClientMapper = socialClientMapper;
        this.socialUserBindMapper = socialUserBindMapper;
        this.socialUserMapper = socialUserMapper;
        this.envVersion = envVersion;
        this.miniprogramState = miniprogramState;
        this.wxMpServiceCache = CacheUtils.buildAsyncReloadingCache(
                Duration.ofSeconds(10L), new CacheLoader<>() {
                    /**
                     * 加载微信公众号服务实例。
                     *
                     * @param key 缓存键
                     * @return 微信公众号服务实例
                     */
                    @Override
                    public WxMpService load(String key) {
                        String[] keys = key.split(":");
                        return buildWxMpService(keys[0], keys[1]);
                    }
                });
        this.wxMaServiceCache = CacheUtils.buildAsyncReloadingCache(
                Duration.ofSeconds(10L), new CacheLoader<>() {
                    /**
                     * 加载微信小程序服务实例。
                     *
                     * @param key 缓存键
                     * @return 微信小程序服务实例
                     */
                    @Override
                    public WxMaService load(String key) {
                        String[] keys = key.split(":");
                        return buildWxMaService(keys[0], keys[1]);
                    }
                });
    }

    /**
     * 查询 get Authorize Url 对应的数据。
     *
     * @param socialType socialType 参数
     * @param userType userType 参数
     * @param redirectUri redirectUri 参数
     * @return 处理结果
     */
    public String getAuthorizeUrl(Integer socialType, Integer userType, String redirectUri) {
        AuthRequest authRequest = buildAuthRequest(socialType, userType);
        String authorizeUri = authRequest.authorize(AuthStateUtils.createState());
        return HttpUtils.replaceUrlQuery(authorizeUri, "redirect_uri", redirectUri);
    }

    /**
     * 查询 get Auth User 对应的数据。
     *
     * @param socialType socialType 参数
     * @param userType userType 参数
     * @param code code 参数
     * @param state state 参数
     * @return 处理结果
     */
    public AuthUser getAuthUser(Integer socialType, Integer userType, String code, String state) {
        AuthRequest authRequest = buildAuthRequest(socialType, userType);
        AuthCallback authCallback = AuthCallback.builder().code(code).auth_code(code).state(state).build();
        AuthResponse<?> authResponse = authRequest.login(authCallback);
        log.info("[getAuthUser][请求社交平台 type({}) request({}) response({})]", socialType,
                toJsonString(authCallback), toJsonString(authResponse));
        if (!authResponse.ok()) {
            throw exception(SOCIAL_USER_AUTH_FAILURE, authResponse.getMsg());
        }
        return (AuthUser) authResponse.getData();
    }

    /**
     * 构建 build Auth Request 对应的数据对象。
     *
     * @param socialType socialType 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    @VisibleForTesting
    public AuthRequest buildAuthRequest(Integer socialType, Integer userType) {
        AuthRequest request = authRequestFactory.get(SocialTypeEnum.valueOfType(socialType).getSource());
        Assert.notNull(request, String.format("社交平台(%d) 不存在", socialType));
        SocialClientDO client = socialClientMapper.selectBySocialTypeAndUserType(socialType, userType);
        if (client != null && Objects.equals(client.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            AuthConfig authConfig = (AuthConfig) ReflectUtil.getFieldValue(request, "config");
            AuthConfig newAuthConfig = ReflectUtil.newInstance(authConfig.getClass());
            BeanUtil.copyProperties(authConfig, newAuthConfig);
            newAuthConfig.setClientId(client.getClientId());
            newAuthConfig.setClientSecret(client.getClientSecret());
            if (client.getAgentId() != null) {
                newAuthConfig.setAgentId(client.getAgentId());
            }
            if (SocialTypeEnum.ALIPAY_MINI_PROGRAM.getType().equals(socialType)) {
                return new AuthAlipayRequest(newAuthConfig, client.getPublicKey());
            }
            ReflectUtil.setFieldValue(request, "config", newAuthConfig);
        }
        return request;
    }

    /**
     * 创建 create Wx Mp Jsapi Signature 对应的数据。
     *
     * @param userType userType 参数
     * @param url url 参数
     * @return 处理结果
     */
    @SneakyThrows
    public WxJsapiSignature createWxMpJsapiSignature(Integer userType, String url) {
        return getWxMpService(userType).createJsapiSignature(url);
    }

    /**
     * 查询 get Wx Mp Service 对应的数据。
     *
     * @param userType userType 参数
     * @return 处理结果
     */
    @VisibleForTesting
    WxMpService getWxMpService(Integer userType) {
        SocialClientDO client = socialClientMapper.selectBySocialTypeAndUserType(
                SocialTypeEnum.WECHAT_MP.getType(), userType);
        if (client != null && Objects.equals(client.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            return wxMpServiceCache.getUnchecked(client.getClientId() + ":" + client.getClientSecret());
        }
        return wxMpService;
    }

    /**
     * 构建 build Wx Mp Service 对应的数据对象。
     *
     * @param clientId clientId 参数
     * @param clientSecret clientSecret 参数
     * @return 处理结果
     */
    public WxMpService buildWxMpService(String clientId, String clientSecret) {
        WxMpRedisConfigImpl configStorage = new WxMpRedisConfigImpl(
                new RedisTemplateWxRedisOps(stringRedisTemplate), wxMpProperties.getConfigStorage().getKeyPrefix());
        configStorage.setAppId(clientId);
        configStorage.setSecret(clientSecret);
        WxMpService service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(configStorage);
        return service;
    }

    /**
     * 查询 get Wx Ma Phone Number Info 对应的数据。
     *
     * @param userType userType 参数
     * @param phoneCode phoneCode 参数
     * @return 处理结果
     */
    public WxMaPhoneNumberInfo getWxMaPhoneNumberInfo(Integer userType, String phoneCode) {
        try {
            return getWxMaService(userType).getUserService().getPhoneNumber(phoneCode);
        } catch (WxErrorException e) {
            log.error("[getPhoneNumber][userType({}) phoneCode({}) 获得手机号失败]", userType, phoneCode, e);
            throw exception(SOCIAL_CLIENT_WEIXIN_MINI_APP_PHONE_CODE_ERROR);
        }
    }

    /**
     * 查询 get Wxa Qrcode 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    public byte[] getWxaQrcode(SocialWxQrcodeReqDTO reqVO) {
        try {
            return getWxMaService(UserTypeEnum.MEMBER.getValue()).getQrcodeService().createWxaCodeUnlimitBytes(
                    ObjUtil.defaultIfEmpty(reqVO.getScene(), SocialWxQrcodeReqDTO.SCENE),
                    reqVO.getPath(), ObjUtil.defaultIfNull(reqVO.getCheckPath(), SocialWxQrcodeReqDTO.CHECK_PATH),
                    envVersion, ObjUtil.defaultIfNull(reqVO.getWidth(), SocialWxQrcodeReqDTO.WIDTH),
                    ObjUtil.defaultIfNull(reqVO.getAutoColor(), SocialWxQrcodeReqDTO.AUTO_COLOR), null,
                    ObjUtil.defaultIfNull(reqVO.getHyaline(), SocialWxQrcodeReqDTO.HYALINE));
        } catch (WxErrorException e) {
            log.error("[getWxQrcode][reqVO({}) 获得小程序码失败]", reqVO, e);
            throw exception(SOCIAL_CLIENT_WEIXIN_MINI_APP_QRCODE_ERROR);
        }
    }

    /**
     * 查询 get Subscribe Template List 对应的数据。
     *
     * @param userType userType 参数
     * @return 处理结果
     */
    @Cacheable(cacheNames = RedisKeyConstants.WXA_SUBSCRIBE_TEMPLATE, key = "#userType", unless = "#result == null")
    public List<TemplateInfo> getSubscribeTemplateList(Integer userType) {
        try {
            WxMaSubscribeService subscribeService = getWxMaService(userType).getSubscribeService();
            return subscribeService.getTemplateList();
        } catch (WxErrorException e) {
            log.error("[getSubscribeTemplate][userType({}) 获得小程序订阅消息模版]", userType, e);
            throw exception(SOCIAL_CLIENT_WEIXIN_MINI_APP_SUBSCRIBE_TEMPLATE_ERROR);
        }
    }

    /**
     * 发送 send Subscribe Message 对应的消息。
     *
     * @param reqDTO reqDTO 参数
     * @param templateId templateId 参数
     * @param openId openId 参数
     */
    public void sendSubscribeMessage(SocialWxaSubscribeMessageSendReqDTO reqDTO, String templateId, String openId) {
        try {
            getWxMaService(reqDTO.getUserType()).getSubscribeService()
                    .sendSubscribeMsg(buildMessageSendReqDTO(reqDTO, templateId, openId));
        } catch (WxErrorException e) {
            log.error("[sendSubscribeMessage][reqVO({}) templateId({}) openId({}) 发送小程序订阅消息失败]", reqDTO, templateId, openId, e);
            throw exception(SOCIAL_CLIENT_WEIXIN_MINI_APP_SUBSCRIBE_MESSAGE_ERROR);
        }
    }

    /**
     * 构建 build Message Send Req DTO 对应的数据对象。
     *
     * @param reqDTO reqDTO 参数
     * @param templateId templateId 参数
     * @param openId openId 参数
     * @return 处理结果
     */
    private WxMaSubscribeMessage buildMessageSendReqDTO(SocialWxaSubscribeMessageSendReqDTO reqDTO,
                                                        String templateId, String openId) {
        WxMaSubscribeMessage subscribeMessage = new WxMaSubscribeMessage().setLang(WxMaConstants.MiniProgramLang.ZH_CN)
                .setMiniprogramState(miniprogramState).setTemplateId(templateId).setToUser(openId).setPage(reqDTO.getPage());
        Map<String, String> messages = reqDTO.getMessages();
        if (CollUtil.isNotEmpty(messages)) {
            reqDTO.getMessages().keySet().forEach(key -> findAndThen(messages, key, value ->
                    subscribeMessage.addData(new WxMaSubscribeMessage.MsgData(key, value))));
        }
        return subscribeMessage;
    }

    /**
     * 执行 upload Wxa Order Shipping Info 对应的业务操作。
     *
     * @param userType userType 参数
     * @param reqDTO reqDTO 参数
     */
    public void uploadWxaOrderShippingInfo(Integer userType, SocialWxaOrderUploadShippingInfoReqDTO reqDTO) {
        WxMaService service = getWxMaService(userType);
        List<ShippingListBean> shippingList = Objects.equals(reqDTO.getLogisticsType(), SocialWxaOrderUploadShippingInfoReqDTO.LOGISTICS_TYPE_EXPRESS)
                ? singletonList(ShippingListBean.builder().trackingNo(reqDTO.getLogisticsNo()).expressCompany(reqDTO.getExpressCompany())
                .itemDesc(reqDTO.getItemDesc()).contact(ContactBean.builder()
                        .receiverContact(DesensitizedUtil.mobilePhone(reqDTO.getReceiverContact())).build()).build())
                : singletonList(ShippingListBean.builder().itemDesc(reqDTO.getItemDesc()).build());
        WxMaOrderShippingInfoUploadRequest request = WxMaOrderShippingInfoUploadRequest.builder()
                .orderKey(OrderKeyBean.builder().orderNumberType(2).transactionId(reqDTO.getTransactionId()).build())
                .logisticsType(reqDTO.getLogisticsType()).deliveryMode(1).shippingList(shippingList)
                .payer(PayerBean.builder().openid(reqDTO.getOpenid()).build())
                .uploadTime(ZonedDateTime.now().format(UTC_MS_WITH_XXX_OFFSET_FORMATTER)).build();
        int maxAttempts = UPLOAD_SHIPPING_INFO_RETRY_BACKOFF_MILLIS.length + 1;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                WxMaOrderShippingInfoBaseResponse response = service.getWxMaOrderShippingService().upload(request);
                log.info("[uploadWxaOrderShippingInfo][上传微信小程序发货信息成功：request({}) response({})]", request, response);
                return;
            } catch (WxErrorException ex) {
                if (ex.getError().getErrorCode() == WX_ERR_CODE_PAY_ORDER_NOT_EXIST && attempt < maxAttempts) {
                    ThreadUtil.sleep(UPLOAD_SHIPPING_INFO_RETRY_BACKOFF_MILLIS[attempt - 1]);
                    continue;
                }
                log.error("[uploadWxaOrderShippingInfo][上传微信小程序发货信息失败：request({})]", request, ex);
                throw exception(SOCIAL_CLIENT_WEIXIN_MINI_APP_ORDER_UPLOAD_SHIPPING_INFO_ERROR, ex.getError().getErrorMsg());
            }
        }
    }

    /**
     * 执行 notify Wxa Order Confirm Receive 对应的业务操作。
     *
     * @param userType userType 参数
     * @param reqDTO reqDTO 参数
     */
    public void notifyWxaOrderConfirmReceive(Integer userType, SocialWxaOrderNotifyConfirmReceiveReqDTO reqDTO) {
        WxMaOrderShippingInfoNotifyConfirmRequest request = WxMaOrderShippingInfoNotifyConfirmRequest.builder()
                .transactionId(reqDTO.getTransactionId()).receivedTime(toEpochSecond(reqDTO.getReceivedTime())).build();
        try {
            WxMaOrderShippingInfoBaseResponse response = getWxMaService(userType).getWxMaOrderShippingService().notifyConfirmReceive(request);
            if (response.getErrCode() != 0) {
                log.error("[notifyWxaOrderConfirmReceive][确认收货提醒到微信小程序失败：request({}) response({})]", request, response);
                throw exception(SOCIAL_CLIENT_WEIXIN_MINI_APP_ORDER_NOTIFY_CONFIRM_RECEIVE_ERROR, response.getErrMsg());
            }
            log.info("[notifyWxaOrderConfirmReceive][确认收货提醒到微信小程序成功：request({}) response({})]", request, response);
        } catch (WxErrorException ex) {
            log.error("[notifyWxaOrderConfirmReceive][确认收货提醒到微信小程序失败：request({})]", request, ex);
            throw exception(SOCIAL_CLIENT_WEIXIN_MINI_APP_ORDER_NOTIFY_CONFIRM_RECEIVE_ERROR, ex.getError().getErrorMsg());
        }
    }

    /**
     * 查询 get Wx Ma Service 对应的数据。
     *
     * @param userType userType 参数
     * @return 处理结果
     */
    @VisibleForTesting
    WxMaService getWxMaService(Integer userType) {
        SocialClientDO client = socialClientMapper.selectBySocialTypeAndUserType(
                SocialTypeEnum.WECHAT_MINI_PROGRAM.getType(), userType);
        if (client != null && Objects.equals(client.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            return wxMaServiceCache.getUnchecked(client.getClientId() + ":" + client.getClientSecret());
        }
        return wxMaService;
    }

    /**
     * 构建 build Wx Ma Service 对应的数据对象。
     *
     * @param clientId clientId 参数
     * @param clientSecret clientSecret 参数
     * @return 处理结果
     */
    private WxMaService buildWxMaService(String clientId, String clientSecret) {
        WxMaRedisBetterConfigImpl configStorage = new WxMaRedisBetterConfigImpl(
                new RedisTemplateWxRedisOps(stringRedisTemplate), wxMaProperties.getConfigStorage().getKeyPrefix());
        configStorage.setAppid(clientId);
        configStorage.setSecret(clientSecret);
        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(configStorage);
        return service;
    }

    /**
     * 创建 create Social Client 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    public Long createSocialClient(@Valid SocialClientSaveReqVO createReqVO) {
        validateSocialClientUnique(null, createReqVO.getUserType(), createReqVO.getSocialType());
        SocialClientDO client = BeanUtils.toBean(createReqVO, SocialClientDO.class);
        socialClientMapper.insert(client);
        return client.getId();
    }

    /**
     * 更新 update Social Client 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    public void updateSocialClient(@Valid SocialClientSaveReqVO updateReqVO) {
        validateSocialClientExists(updateReqVO.getId());
        validateSocialClientUnique(updateReqVO.getId(), updateReqVO.getUserType(), updateReqVO.getSocialType());
        socialClientMapper.updateById(BeanUtils.toBean(updateReqVO, SocialClientDO.class));
    }

    /**
     * 删除 delete Social Client 对应的数据。
     *
     * @param id id 参数
     */
    public void deleteSocialClient(Long id) {
        validateSocialClientExists(id);
        socialClientMapper.deleteById(id);
    }

    /**
     * 删除 delete Social Client List 对应的数据。
     *
     * @param ids ids 参数
     */
    public void deleteSocialClientList(List<Long> ids) {
        socialClientMapper.deleteByIds(ids);
    }

    /**
     * 校验 validate Social Client Exists 对应的业务规则。
     *
     * @param id id 参数
     */
    private void validateSocialClientExists(Long id) {
        if (socialClientMapper.selectById(id) == null) {
            throw exception(SOCIAL_CLIENT_NOT_EXISTS);
        }
    }

    /**
     * 校验 validate Social Client Unique 对应的业务规则。
     *
     * @param id id 参数
     * @param userType userType 参数
     * @param socialType socialType 参数
     */
    private void validateSocialClientUnique(Long id, Integer userType, Integer socialType) {
        SocialClientDO client = socialClientMapper.selectBySocialTypeAndUserType(socialType, userType);
        if (client == null) {
            return;
        }
        if (id == null || ObjUtil.notEqual(id, client.getId())) {
            throw exception(SOCIAL_CLIENT_UNIQUE);
        }
    }

    /**
     * 查询 get Social Client 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public SocialClientDO getSocialClient(Long id) {
        return socialClientMapper.selectById(id);
    }

    /**
     * 查询 get Social Client Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    public PageResult<SocialClientDO> getSocialClientPage(SocialClientPageReqVO pageReqVO) {
        return socialClientMapper.selectPage(pageReqVO);
    }

    /**
     * 查询 get Social User List 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    public List<SocialUserDO> getSocialUserList(Long userId, Integer userType) {
        List<SocialUserBindDO> socialUserBinds = socialUserBindMapper.selectListByUserIdAndUserType(userId, userType);
        if (CollUtil.isEmpty(socialUserBinds)) {
            return Collections.emptyList();
        }
        return socialUserMapper.selectByIds(convertSet(socialUserBinds, SocialUserBindDO::getSocialUserId));
    }

    /**
     * 执行 bind Social User 对应的业务操作。
     *
     * @param reqDTO reqDTO 参数
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public String bindSocialUser(@Valid SocialUserBindReqDTO reqDTO) {
        SocialUserDO socialUser = authSocialUser(reqDTO.getSocialType(), reqDTO.getUserType(), reqDTO.getCode(), reqDTO.getState());
        Assert.notNull(socialUser, "社交用户不能为空");
        socialUserBindMapper.deleteByUserTypeAndSocialUserId(reqDTO.getUserType(), socialUser.getId());
        socialUserBindMapper.deleteByUserTypeAndUserIdAndSocialType(reqDTO.getUserType(), reqDTO.getUserId(), socialUser.getType());
        SocialUserBindDO socialUserBind = SocialUserBindDO.builder()
                .userId(reqDTO.getUserId()).userType(reqDTO.getUserType())
                .socialUserId(socialUser.getId()).socialType(socialUser.getType()).build();
        socialUserBindMapper.insert(socialUserBind);
        return socialUser.getOpenid();
    }

    /**
     * 执行 unbind Social User 对应的业务操作。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param socialType socialType 参数
     * @param openid openid 参数
     */
    public void unbindSocialUser(Long userId, Integer userType, Integer socialType, String openid) {
        SocialUserDO socialUser = socialUserMapper.selectByTypeAndOpenid(socialType, openid);
        if (socialUser == null) {
            throw exception(SOCIAL_USER_NOT_FOUND);
        }
        socialUserBindMapper.deleteByUserTypeAndUserIdAndSocialType(userType, userId, socialUser.getType());
    }

    /**
     * 查询 get Social User By User Id 对应的数据。
     *
     * @param userType userType 参数
     * @param userId userId 参数
     * @param socialType socialType 参数
     * @return 处理结果
     */
    public SocialUserRespDTO getSocialUserByUserId(Integer userType, Long userId, Integer socialType) {
        SocialUserBindDO socialUserBind = socialUserBindMapper.selectByUserIdAndUserTypeAndSocialType(userId, userType, socialType);
        if (socialUserBind == null) {
            return null;
        }
        SocialUserDO socialUser = socialUserMapper.selectById(socialUserBind.getSocialUserId());
        Assert.notNull(socialUser, "社交用户不能为空");
        return new SocialUserRespDTO(socialUser.getOpenid(), socialUser.getNickname(), socialUser.getAvatar(), socialUserBind.getUserId());
    }

    /**
     * 查询 get Social User By Code 对应的数据。
     *
     * @param userType userType 参数
     * @param socialType socialType 参数
     * @param code code 参数
     * @param state state 参数
     * @return 处理结果
     */
    public SocialUserRespDTO getSocialUserByCode(Integer userType, Integer socialType, String code, String state) {
        SocialUserDO socialUser = authSocialUser(socialType, userType, code, state);
        Assert.notNull(socialUser, "社交用户不能为空");
        SocialUserBindDO socialUserBind = socialUserBindMapper.selectByUserTypeAndSocialUserId(userType, socialUser.getId());
        return new SocialUserRespDTO(socialUser.getOpenid(), socialUser.getNickname(), socialUser.getAvatar(),
                socialUserBind != null ? socialUserBind.getUserId() : null);
    }

    /**
     * 处理 auth Social User 对应的认证流程。
     *
     * @param socialType socialType 参数
     * @param userType userType 参数
     * @param code code 参数
     * @param state state 参数
     * @return 处理结果
     */
    @NotNull
    public SocialUserDO authSocialUser(Integer socialType, Integer userType, String code, String state) {
        SocialUserDO socialUser = socialUserMapper.selectByTypeAndCodeAnState(socialType, code, state);
        if (socialUser != null) {
            return socialUser;
        }
        AuthUser authUser = getAuthUser(socialType, userType, code, state);
        Assert.notNull(authUser, "三方用户不能为空");
        socialUser = socialUserMapper.selectByTypeAndOpenid(socialType, authUser.getUuid());
        if (socialUser == null) {
            socialUser = new SocialUserDO();
        }
        socialUser.setType(socialType).setCode(code).setState(state)
                .setOpenid(authUser.getUuid()).setToken(authUser.getToken().getAccessToken()).setRawTokenInfo(toJsonString(authUser.getToken()))
                .setNickname(authUser.getNickname()).setAvatar(authUser.getAvatar()).setRawUserInfo(toJsonString(authUser.getRawUserInfo()));
        if (socialUser.getId() == null) {
            socialUserMapper.insert(socialUser);
        } else {
            socialUser.clean();
            socialUserMapper.updateById(socialUser);
        }
        return socialUser;
    }

    /**
     * 查询 get Social User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public SocialUserDO getSocialUser(Long id) {
        return socialUserMapper.selectById(id);
    }

    /**
     * 查询 get Social User Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    public PageResult<SocialUserDO> getSocialUserPage(SocialUserPageReqVO pageReqVO) {
        return socialUserMapper.selectPage(pageReqVO);
    }
}
