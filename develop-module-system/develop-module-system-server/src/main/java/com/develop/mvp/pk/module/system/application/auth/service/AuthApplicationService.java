package com.develop.mvp.pk.module.system.application.auth.service;

import cn.hutool.core.util.ObjectUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.util.monitor.TracerUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.common.util.servlet.ServletUtils;
import com.develop.mvp.pk.framework.common.util.validation.ValidationUtils;
import com.develop.mvp.pk.framework.datapermission.core.annotation.DataPermission;
import com.develop.mvp.pk.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.develop.mvp.pk.module.system.api.sms.SmsCodeApi;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserBindReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserRespDTO;
import com.develop.mvp.pk.module.system.application.auth.port.inbound.AuthUseCase;
import com.develop.mvp.pk.module.system.application.logger.service.LoggerApplicationService;
import com.develop.mvp.pk.module.system.application.member.service.MemberApplicationService;
import com.develop.mvp.pk.module.system.application.oauth2.port.inbound.OAuth2UseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.controller.admin.auth.vo.*;
import com.develop.mvp.pk.module.system.convert.auth.AuthConvert;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.enums.logger.LoginLogTypeEnum;
import com.develop.mvp.pk.module.system.enums.logger.LoginResultEnum;
import com.develop.mvp.pk.module.system.enums.oauth2.OAuth2ClientConstants;
import com.develop.mvp.pk.module.system.enums.sms.SmsSceneEnum;
import com.develop.mvp.pk.module.system.application.social.port.inbound.SocialUseCase;
import com.google.common.annotations.VisibleForTesting;
import jakarta.validation.Validator;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

/**
 * Auth Application Service 应用服务。
 */
@Slf4j
public class AuthApplicationService implements AuthUseCase {

	private final AdminUserUseCase userService;

	private final LoggerApplicationService loggerApplicationService;

	private final OAuth2UseCase oauth2TokenService;

	private final SocialUseCase socialUseCase;

	private final MemberApplicationService memberApplicationService;

	private final Validator validator;

	private final CaptchaService captchaService;

	private final SmsCodeApi smsCodeApi;

	@Setter
	private Boolean captchaEnable;

	/**
	 * 创建 AuthApplicationService 实例。
	 *
	 * @param userService              userService 参数
	 * @param loggerApplicationService loggerApplicationService 参数
	 * @param oauth2TokenService       oauth2TokenService 参数
	 * @param socialUseCase            socialUseCase 参数
	 * @param memberApplicationService memberApplicationService 参数
	 * @param validator                validator 参数
	 * @param captchaService           captchaService 参数
	 * @param smsCodeApi               smsCodeApi 参数
	 */
	public AuthApplicationService(AdminUserUseCase userService, LoggerApplicationService loggerApplicationService, OAuth2UseCase oauth2TokenService, SocialUseCase socialUseCase, MemberApplicationService memberApplicationService, Validator validator, CaptchaService captchaService, SmsCodeApi smsCodeApi) {

		this.userService = userService;
		this.loggerApplicationService = loggerApplicationService;
		this.oauth2TokenService = oauth2TokenService;
		this.socialUseCase = socialUseCase;
		this.memberApplicationService = memberApplicationService;
		this.validator = validator;
		this.captchaService = captchaService;
		this.smsCodeApi = smsCodeApi;
		this.captchaEnable = true;
	}

	/**
	 * 设置 set Captcha Enable 对应的数据。
	 *
	 * @param captchaEnable captchaEnable 参数
	 */
	public void setCaptchaEnable(Boolean captchaEnable) {

		this.captchaEnable = captchaEnable;
	}

	/**
	 * 处理 authenticate 对应的认证流程。
	 *
	 * @param username username 参数
	 * @param password password 参数
	 * @return 处理结果
	 */
	public AdminUserDO authenticate(String username, String password) {

		final LoginLogTypeEnum logTypeEnum = LoginLogTypeEnum.LOGIN_USERNAME;
		AdminUserDO user = userService.getUserByUsername(username);
		if (user == null) {
			createLoginLog(null, username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
			throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
		}
		if (!userService.isPasswordMatch(password, user.getPassword())) {
			createLoginLog(user.getId(), username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
			throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
		}
		if (CommonStatusEnum.isDisable(user.getStatus())) {
			createLoginLog(user.getId(), username, logTypeEnum, LoginResultEnum.USER_DISABLED);
			throw exception(AUTH_LOGIN_USER_DISABLED);
		}
		return user;
	}

	/**
	 * 处理 login 对应的认证流程。
	 *
	 * @param reqVO reqVO 参数
	 * @return 处理结果
	 */
	@DataPermission(enable = false)
	public AuthLoginRespVO login(AuthLoginReqVO reqVO) {

		validateCaptcha(reqVO);
		AdminUserDO user = authenticate(reqVO.getUsername(), reqVO.getPassword());
		if (reqVO.getSocialType() != null) {
			socialUseCase.bindSocialUser(new SocialUserBindReqDTO(user.getId(), getUserType().getValue(), reqVO.getSocialType(), reqVO.getSocialCode(), reqVO.getSocialState()));
		}
		return createTokenAfterLoginSuccess(user.getId(), reqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME);
	}

	/**
	 * 发送 send Sms Code 对应的消息。
	 *
	 * @param reqVO reqVO 参数
	 */
	public void sendSmsCode(AuthSmsSendReqVO reqVO) {

		if (Objects.equals(SmsSceneEnum.ADMIN_MEMBER_RESET_PASSWORD.getScene(), reqVO.getScene())) {
			ResponseModel response = doValidateCaptcha(reqVO);
			if (!response.isSuccess()) {
				throw exception(AUTH_REGISTER_CAPTCHA_CODE_ERROR, response.getRepMsg());
			}
		}
		if (userService.getUserByMobile(reqVO.getMobile()) == null) {
			throw exception(AUTH_MOBILE_NOT_EXISTS);
		}
		smsCodeApi.sendSmsCode(AuthConvert.INSTANCE.convert(reqVO).setCreateIp(getClientIP()));
	}

	/**
	 * 执行 sms Login 对应的业务操作。
	 *
	 * @param reqVO reqVO 参数
	 * @return 处理结果
	 */
	public AuthLoginRespVO smsLogin(AuthSmsLoginReqVO reqVO) {

		smsCodeApi.useSmsCode(AuthConvert.INSTANCE.convert(reqVO, SmsSceneEnum.ADMIN_MEMBER_LOGIN.getScene(), getClientIP())).checkError();
		AdminUserDO user = userService.getUserByMobile(reqVO.getMobile());
		if (user == null) {
			throw exception(USER_NOT_EXISTS);
		}
		return createTokenAfterLoginSuccess(user.getId(), reqVO.getMobile(), LoginLogTypeEnum.LOGIN_MOBILE);
	}

	/**
	 * 执行 social Login 对应的业务操作。
	 *
	 * @param reqVO reqVO 参数
	 * @return 处理结果
	 */
	public AuthLoginRespVO socialLogin(AuthSocialLoginReqVO reqVO) {

		SocialUserRespDTO socialUser = socialUseCase.getSocialUserByCode(UserTypeEnum.ADMIN.getValue(), reqVO.getType(), reqVO.getCode(), reqVO.getState());
		if (socialUser == null || socialUser.getUserId() == null) {
			throw exception(AUTH_THIRD_LOGIN_NOT_BIND);
		}
		AdminUserDO user = userService.getUser(socialUser.getUserId());
		if (user == null) {
			throw exception(USER_NOT_EXISTS);
		}
		return createTokenAfterLoginSuccess(user.getId(), user.getUsername(), LoginLogTypeEnum.LOGIN_SOCIAL);
	}

	/**
	 * 校验 validate Captcha 对应的业务规则。
	 *
	 * @param reqVO reqVO 参数
	 */
	@VisibleForTesting
	void validateCaptcha(AuthLoginReqVO reqVO) {

		ResponseModel response = doValidateCaptcha(reqVO);
		if (!response.isSuccess()) {
			createLoginLog(null, reqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME, LoginResultEnum.CAPTCHA_CODE_ERROR);
			throw exception(AUTH_LOGIN_CAPTCHA_CODE_ERROR, response.getRepMsg());
		}
	}

	/**
	 * 执行 do Validate Captcha 对应的业务操作。
	 *
	 * @param reqVO reqVO 参数
	 * @return 处理结果
	 */
	private ResponseModel doValidateCaptcha(CaptchaVerificationReqVO reqVO) {

		if (!captchaEnable) {
			return ResponseModel.success();
		}
		ValidationUtils.validate(validator, reqVO, CaptchaVerificationReqVO.CodeEnableGroup.class);
		CaptchaVO captchaVO = new CaptchaVO();
		captchaVO.setCaptchaVerification(reqVO.getCaptchaVerification());
		return captchaService.verification(captchaVO);
	}

	/**
	 * 执行 refresh Token 对应的业务操作。
	 *
	 * @param refreshToken refreshToken 参数
	 * @return 处理结果
	 */
	public AuthLoginRespVO refreshToken(String refreshToken) {

		OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, OAuth2ClientConstants.CLIENT_ID_DEFAULT);
		return BeanUtils.toBean(accessTokenDO, AuthLoginRespVO.class);
	}

	/**
	 * 处理 logout 对应的认证流程。
	 *
	 * @param token   token 参数
	 * @param logType logType 参数
	 */
	public void logout(String token, Integer logType) {

		OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.removeAccessToken(token);
		if (accessTokenDO == null) {
			return;
		}
		createLogoutLog(accessTokenDO.getUserId(), accessTokenDO.getUserType(), logType);
	}

	/**
	 * 处理 register 对应的认证流程。
	 *
	 * @param registerReqVO registerReqVO 参数
	 * @return 处理结果
	 */
	public AuthLoginRespVO register(AuthRegisterReqVO registerReqVO) {

		validateCaptcha(registerReqVO);
		Long userId = userService.registerUser(registerReqVO);
		return createTokenAfterLoginSuccess(userId, registerReqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME);
	}

	/**
	 * 校验 validate Captcha 对应的业务规则。
	 *
	 * @param reqVO reqVO 参数
	 */
	@VisibleForTesting
	void validateCaptcha(AuthRegisterReqVO reqVO) {

		ResponseModel response = doValidateCaptcha(reqVO);
		if (!response.isSuccess()) {
			throw exception(AUTH_REGISTER_CAPTCHA_CODE_ERROR, response.getRepMsg());
		}
	}

	/**
	 * 更新 reset Password 对应的数据。
	 *
	 * @param reqVO reqVO 参数
	 */
	@Transactional(rollbackFor = Exception.class)
	public void resetPassword(AuthResetPasswordReqVO reqVO) {

		AdminUserDO userByMobile = userService.getUserByMobile(reqVO.getMobile());
		if (userByMobile == null) {
			throw exception(USER_MOBILE_NOT_EXISTS);
		}
		smsCodeApi.useSmsCode(new SmsCodeUseReqDTO().setCode(reqVO.getCode()).setMobile(reqVO.getMobile()).setScene(SmsSceneEnum.ADMIN_MEMBER_RESET_PASSWORD.getScene()).setUsedIp(getClientIP())).checkError();
		userService.updateUserPassword(userByMobile.getId(), reqVO.getPassword());
	}

	/**
	 * 创建 create Token After Login Success 对应的数据。
	 *
	 * @param userId   userId 参数
	 * @param username username 参数
	 * @param logType  logType 参数
	 * @return 处理结果
	 */
	private AuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String username, LoginLogTypeEnum logType) {

		createLoginLog(userId, username, logType, LoginResultEnum.SUCCESS);
		OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(userId, getUserType().getValue(), OAuth2ClientConstants.CLIENT_ID_DEFAULT, null);
		return BeanUtils.toBean(accessTokenDO, AuthLoginRespVO.class);
	}

	/**
	 * 创建 create Login Log 对应的数据。
	 *
	 * @param userId      userId 参数
	 * @param username    username 参数
	 * @param logTypeEnum logTypeEnum 参数
	 * @param loginResult loginResult 参数
	 */
	private void createLoginLog(Long userId, String username, LoginLogTypeEnum logTypeEnum, LoginResultEnum loginResult) {

		LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
		reqDTO.setLogType(logTypeEnum.getType());
		reqDTO.setTraceId(TracerUtils.getTraceId());
		reqDTO.setUserId(userId);
		reqDTO.setUserType(getUserType().getValue());
		reqDTO.setUsername(username);
		reqDTO.setUserAgent(ServletUtils.getUserAgent());
		reqDTO.setUserIp(ServletUtils.getClientIP());
		reqDTO.setResult(loginResult.getResult());
		loggerApplicationService.createLoginLog(reqDTO);
		if (userId != null && Objects.equals(LoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
			userService.updateUserLogin(userId, ServletUtils.getClientIP());
		}
	}

	/**
	 * 创建 create Logout Log 对应的数据。
	 *
	 * @param userId   userId 参数
	 * @param userType userType 参数
	 * @param logType  logType 参数
	 */
	private void createLogoutLog(Long userId, Integer userType, Integer logType) {

		LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
		reqDTO.setLogType(logType);
		reqDTO.setTraceId(TracerUtils.getTraceId());
		reqDTO.setUserId(userId);
		reqDTO.setUserType(userType);
		if (ObjectUtil.equal(getUserType().getValue(), userType)) {
			reqDTO.setUsername(getUsername(userId));
		} else {
			reqDTO.setUsername(memberApplicationService.getMemberUserMobile(userId));
		}
		reqDTO.setUserAgent(ServletUtils.getUserAgent());
		reqDTO.setUserIp(ServletUtils.getClientIP());
		reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
		loggerApplicationService.createLoginLog(reqDTO);
	}

	/**
	 * 查询 get Username 对应的数据。
	 *
	 * @param userId userId 参数
	 * @return 处理结果
	 */
	private String getUsername(Long userId) {

		if (userId == null) {
			return null;
		}
		AdminUserDO user = userService.getUser(userId);
		return user != null ? user.getUsername() : null;
	}

	/**
	 * 查询 get User Type 对应的数据。
	 *
	 * @return 处理结果
	 */
	private UserTypeEnum getUserType() {

		return UserTypeEnum.ADMIN;
	}

}
