package com.develop.mvp.pk.module.system.framework.sms.core.client.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.http.HttpUtil;
import com.develop.mvp.pk.framework.common.core.KeyValue;
import com.develop.mvp.pk.framework.common.util.collection.MapUtils;
import com.develop.mvp.pk.framework.common.util.json.JsonUtils;
import com.develop.mvp.pk.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import com.develop.mvp.pk.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import com.develop.mvp.pk.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import com.develop.mvp.pk.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import com.develop.mvp.pk.module.system.framework.sms.core.property.SmsChannelProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于钉钉 WebHook 实现的调试的短信客户端实现类
 *
 * 考虑到省钱，我们使用钉钉 WebHook 模拟发送短信，方便调试。
 *
 * @author David
 */
public class DebugDingTalkSmsClient extends AbstractSmsClient {

    /**
     * 创建 DebugDingTalkSmsClient 实例。
     *
     * @param properties properties 参数
     */
    public DebugDingTalkSmsClient(SmsChannelProperties properties) {
        super(properties);
        Assert.notEmpty(properties.getApiKey(), "apiKey 不能为空");
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
    }

    /**
     * 发送 send Sms 对应的消息。
     *
     * @param sendLogId sendLogId 参数
     * @param mobile mobile 参数
     * @param apiTemplateId apiTemplateId 参数
     * @param templateParams templateParams 参数
     * @return 处理结果
     */
    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile,
                                  String apiTemplateId, List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 构建请求
        String url = buildUrl("robot/send");
        Map<String, Object> params = new HashMap<>();
        params.put("msgtype", "text");
        String content = String.format("【模拟短信】\n手机号：%s\n短信日志编号：%d\n模板参数：%s",
                mobile, sendLogId, MapUtils.convertMap(templateParams));
        params.put("text", MapUtil.builder().put("content", content).build());
        // 执行请求
        String responseText = HttpUtil.post(url, JsonUtils.toJsonString(params));
        // 解析结果
        Map<?, ?> responseObj = JsonUtils.parseObject(responseText, Map.class);
        String errorCode = MapUtil.getStr(responseObj, "errcode");
        return new SmsSendRespDTO().setSuccess(Objects.equals(errorCode, "0")).setSerialNo(StrUtil.uuid())
                .setApiCode(errorCode).setApiMsg(MapUtil.getStr(responseObj, "errorMsg"));
    }

    /**
     * 构建请求地址
     *
     * 参见 <a href="https://developers.dingtalk.com/document/app/custom-robot-access/title-nfv-794-g71">文档</a>
     *
     * @param path 请求路径
     * @return 请求地址
     */
    @SuppressWarnings("SameParameterValue")
    private String buildUrl(String path) {
        // 生成 timestamp
        long timestamp = System.currentTimeMillis();
        // 生成 sign
        String secret = properties.getApiSecret();
        String stringToSign = timestamp + "\n" + secret;
        byte[] signData = DigestUtil.hmac(HmacAlgorithm.HmacSHA256, StrUtil.bytes(secret)).digest(stringToSign);
        String sign = Base64.encode(signData);
        // 构建最终 URL
        return String.format("https://oapi.dingtalk.com/%s?access_token=%s&timestamp=%d&sign=%s",
                path, properties.getApiKey(), timestamp, sign);
    }

    /**
     * 执行 parse Sms Receive Status 对应的业务操作。
     *
     * @param text text 参数
     * @return 处理结果
     */
    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        throw new UnsupportedOperationException("模拟短信客户端，暂时无需解析回调");
    }

    /**
     * 查询 get Sms Template 对应的数据。
     *
     * @param apiTemplateId apiTemplateId 参数
     * @return 处理结果
     */
    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) {
        return new SmsTemplateRespDTO().setId(apiTemplateId).setContent("")
                .setAuditStatus(SmsTemplateAuditStatusEnum.SUCCESS.getStatus()).setAuditReason("");
    }

}
