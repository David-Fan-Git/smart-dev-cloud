package com.develop.mvp.pk.module.system.application.auth.port.inbound;

// DDD 角色：入站端口 — 定义 Auth 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.module.system.controller.admin.auth.vo.*;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
/**
 * Auth 聚合的入站用例端口。
 */
public interface AuthUseCase {

    /**
     * 处理 authenticate 对应的认证流程。
     *
     * @param username username 参数
     * @param password password 参数
     * @return 处理结果
     */
    AdminUserDO authenticate(String username, String password);

    /**
     * 处理 login 对应的认证流程。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    AuthLoginRespVO login(AuthLoginReqVO reqVO);

    /**
     * 发送 send Sms Code 对应的消息。
     *
     * @param reqVO reqVO 参数
     */
    void sendSmsCode(AuthSmsSendReqVO reqVO);

    /**
     * 执行 sms Login 对应的业务操作。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    AuthLoginRespVO smsLogin(AuthSmsLoginReqVO reqVO);

    /**
     * 执行 social Login 对应的业务操作。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    AuthLoginRespVO socialLogin(AuthSocialLoginReqVO reqVO);

    /**
     * 处理 refresh Token 对应的认证流程。
     *
     * @param refreshToken refreshToken 参数
     * @return 处理结果
     */
    AuthLoginRespVO refreshToken(String refreshToken);

    /**
     * 处理 logout 对应的认证流程。
     *
     * @param token token 参数
     * @param logType logType 参数
     */
    void logout(String token, Integer logType);

    /**
     * 处理 register 对应的认证流程。
     *
     * @param registerReqVO registerReqVO 参数
     * @return 处理结果
     */
    AuthLoginRespVO register(AuthRegisterReqVO registerReqVO);

    /**
     * 更新 reset Password 对应的数据。
     *
     * @param reqVO reqVO 参数
     */
    void resetPassword(AuthResetPasswordReqVO reqVO);
}
