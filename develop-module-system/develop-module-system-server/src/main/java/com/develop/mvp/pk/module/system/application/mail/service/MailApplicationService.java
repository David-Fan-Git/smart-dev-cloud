package com.develop.mvp.pk.module.system.application.mail.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.application.mail.port.inbound.MailUseCase;
import com.develop.mvp.pk.module.system.application.member.service.MemberApplicationService;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.account.MailAccountSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.template.MailTemplateSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.mail.MailAccountDO;
import com.develop.mvp.pk.module.system.dal.dataobject.mail.MailLogDO;
import com.develop.mvp.pk.module.system.dal.dataobject.mail.MailTemplateDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.dal.mysql.mail.MailAccountMapper;
import com.develop.mvp.pk.module.system.dal.mysql.mail.MailLogMapper;
import com.develop.mvp.pk.module.system.dal.mysql.mail.MailTemplateMapper;
import com.develop.mvp.pk.module.system.dal.redis.RedisKeyConstants;
import com.develop.mvp.pk.module.system.domain.mail.MailAccount;
import com.develop.mvp.pk.module.system.domain.mail.MailTemplate;
import com.develop.mvp.pk.module.system.domain.mail.repository.MailAccountRepository;
import com.develop.mvp.pk.module.system.domain.mail.repository.MailTemplateRepository;
import com.develop.mvp.pk.module.system.enums.mail.MailSendStatusEnum;
import com.develop.mvp.pk.module.system.mq.message.mail.MailSendMessage;
import com.develop.mvp.pk.module.system.mq.producer.mail.MailProducer;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import org.dromara.hutool.extra.mail.MailUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.hutool.core.exceptions.ExceptionUtil.getRootCauseMessage;
import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

/**
 * Mail Application Service 应用服务。
 */
public class MailApplicationService implements MailUseCase {

	private static final Pattern PATTERN_PARAMS = Pattern.compile("\\{(.*?)}");

	private MailAccountRepository accountRepo;

	private MailTemplateRepository templateRepo;

	private final MailAccountMapper mailAccountMapper;

	private final MailTemplateMapper mailTemplateMapper;

	private final MailLogMapper mailLogMapper;

	private final AdminUserUseCase adminUserService;

	private final MemberApplicationService memberApplicationService;

	private final MailProducer mailProducer;

	/**
	 * 创建 MailApplicationService 实例。
	 *
	 * @param mailAccountMapper        mailAccountMapper 参数
	 * @param mailTemplateMapper       mailTemplateMapper 参数
	 * @param mailLogMapper            mailLogMapper 参数
	 * @param adminUserService         adminUserService 参数
	 * @param memberApplicationService memberApplicationService 参数
	 * @param mailProducer             mailProducer 参数
	 */
	public MailApplicationService(MailAccountMapper mailAccountMapper, MailTemplateMapper mailTemplateMapper, MailLogMapper mailLogMapper, AdminUserUseCase adminUserService, MemberApplicationService memberApplicationService, MailProducer mailProducer) {

		this.mailAccountMapper = mailAccountMapper;
		this.mailTemplateMapper = mailTemplateMapper;
		this.mailLogMapper = mailLogMapper;
		this.adminUserService = adminUserService;
		this.memberApplicationService = memberApplicationService;
		this.mailProducer = mailProducer;
	}

	/**
	 * 设置 set Account Repo 对应的数据。
	 *
	 * @param accountRepo accountRepo 参数
	 */
	public void setAccountRepo(MailAccountRepository accountRepo) {

		this.accountRepo = accountRepo;
	}

	/**
	 * 设置 set Template Repo 对应的数据。
	 *
	 * @param templateRepo templateRepo 参数
	 */
	public void setTemplateRepo(MailTemplateRepository templateRepo) {

		this.templateRepo = templateRepo;
	}

	/**
	 * 创建 create Mail Account 对应的数据。
	 *
	 * @param createReqVO createReqVO 参数
	 * @return 处理结果
	 */
	public Long createMailAccount(MailAccountSaveReqVO createReqVO) {

		MailAccountDO account = BeanUtils.toBean(createReqVO, MailAccountDO.class);
		mailAccountMapper.insert(account);
		return account.getId();
	}

	/**
	 * 更新 update Mail Account 对应的数据。
	 *
	 * @param updateReqVO updateReqVO 参数
	 */
	@CacheEvict(value = RedisKeyConstants.MAIL_ACCOUNT, key = "#updateReqVO.id")
	public void updateMailAccount(MailAccountSaveReqVO updateReqVO) {

		validateMailAccountExists(updateReqVO.getId());
		MailAccountDO updateObj = BeanUtils.toBean(updateReqVO, MailAccountDO.class);
		mailAccountMapper.updateById(updateObj);
	}

	/**
	 * 删除 delete Mail Account 对应的数据。
	 *
	 * @param id id 参数
	 */
	@CacheEvict(value = RedisKeyConstants.MAIL_ACCOUNT, key = "#id")
	public void deleteMailAccount(Long id) {

		validateMailAccountExists(id);
		if (getMailTemplateCountByAccountId(id) > 0) {
			throw exception(MAIL_ACCOUNT_RELATE_TEMPLATE_EXISTS);
		}
		mailAccountMapper.deleteById(id);
	}

	/**
	 * 删除 delete Mail Account List 对应的数据。
	 *
	 * @param ids ids 参数
	 */
	@CacheEvict(value = RedisKeyConstants.MAIL_ACCOUNT, allEntries = true)
	public void deleteMailAccountList(List<Long> ids) {

		for (Long id : ids) {
			if (getMailTemplateCountByAccountId(id) > 0) {
				throw exception(MAIL_ACCOUNT_RELATE_TEMPLATE_EXISTS);
			}
		}
		mailAccountMapper.deleteByIds(ids);
	}

	/**
	 * 校验 validate Mail Account Exists 对应的业务规则。
	 *
	 * @param id id 参数
	 */
	private void validateMailAccountExists(Long id) {

		if (mailAccountMapper.selectById(id) == null) {
			throw exception(MAIL_ACCOUNT_NOT_EXISTS);
		}
	}

	/**
	 * 查询 get Mail Account 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public MailAccountDO getMailAccount(Long id) {

		return mailAccountMapper.selectById(id);
	}

	/**
	 * 查询 get Mail Account From Cache 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	@Cacheable(value = RedisKeyConstants.MAIL_ACCOUNT, key = "#id", unless = "#result == null")
	public MailAccountDO getMailAccountFromCache(Long id) {

		return getMailAccount(id);
	}

	/**
	 * 查询 get Mail Account Page 对应的数据。
	 *
	 * @param pageReqVO pageReqVO 参数
	 * @return 处理结果
	 */
	public PageResult<MailAccountDO> getMailAccountPage(MailAccountPageReqVO pageReqVO) {

		return mailAccountMapper.selectPage(pageReqVO);
	}

	/**
	 * 查询 get Mail Account List 对应的数据。
	 *
	 * @return 处理结果
	 */
	public List<MailAccountDO> getMailAccountList() {

		return mailAccountMapper.selectList();
	}

	/**
	 * 创建 create Mail Template 对应的数据。
	 *
	 * @param createReqVO createReqVO 参数
	 * @return 处理结果
	 */
	public Long createMailTemplate(MailTemplateSaveReqVO createReqVO) {

		validateCodeUnique(null, createReqVO.getCode());
		MailTemplateDO template = BeanUtils.toBean(createReqVO, MailTemplateDO.class).setParams(parseTemplateTitleAndContentParams(createReqVO.getTitle(), createReqVO.getContent()));
		mailTemplateMapper.insert(template);
		return template.getId();
	}

	/**
	 * 更新 update Mail Template 对应的数据。
	 *
	 * @param updateReqVO updateReqVO 参数
	 */
	@CacheEvict(cacheNames = RedisKeyConstants.MAIL_TEMPLATE, allEntries = true)
	public void updateMailTemplate(MailTemplateSaveReqVO updateReqVO) {

		validateMailTemplateExists(updateReqVO.getId());
		validateCodeUnique(updateReqVO.getId(), updateReqVO.getCode());
		MailTemplateDO updateObj = BeanUtils.toBean(updateReqVO, MailTemplateDO.class).setParams(parseTemplateTitleAndContentParams(updateReqVO.getTitle(), updateReqVO.getContent()));
		mailTemplateMapper.updateById(updateObj);
	}

	/**
	 * 校验 validate Code Unique 对应的业务规则。
	 *
	 * @param id   id 参数
	 * @param code code 参数
	 */
	@VisibleForTesting
	void validateCodeUnique(Long id, String code) {

		MailTemplateDO template = mailTemplateMapper.selectByCode(code);
		if (template == null) {
			return;
		}
		if (id == null || ObjUtil.notEqual(id, template.getId())) {
			throw exception(MAIL_TEMPLATE_CODE_EXISTS);
		}
	}

	/**
	 * 删除 delete Mail Template 对应的数据。
	 *
	 * @param id id 参数
	 */
	@CacheEvict(cacheNames = RedisKeyConstants.MAIL_TEMPLATE, allEntries = true)
	public void deleteMailTemplate(Long id) {

		validateMailTemplateExists(id);
		mailTemplateMapper.deleteById(id);
	}

	/**
	 * 删除 delete Mail Template List 对应的数据。
	 *
	 * @param ids ids 参数
	 */
	@CacheEvict(cacheNames = RedisKeyConstants.MAIL_TEMPLATE, allEntries = true)
	public void deleteMailTemplateList(List<Long> ids) {

		mailTemplateMapper.deleteByIds(ids);
	}

	/**
	 * 校验 validate Mail Template Exists 对应的业务规则。
	 *
	 * @param id id 参数
	 */
	private void validateMailTemplateExists(Long id) {

		if (mailTemplateMapper.selectById(id) == null) {
			throw exception(MAIL_TEMPLATE_NOT_EXISTS);
		}
	}

	/**
	 * 查询 get Mail Template 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public MailTemplateDO getMailTemplate(Long id) {

		return mailTemplateMapper.selectById(id);
	}

	/**
	 * 查询 get Mail Template By Code From Cache 对应的数据。
	 *
	 * @param code code 参数
	 * @return 处理结果
	 */
	@Cacheable(value = RedisKeyConstants.MAIL_TEMPLATE, key = "#code", unless = "#result == null")
	public MailTemplateDO getMailTemplateByCodeFromCache(String code) {

		return mailTemplateMapper.selectByCode(code);
	}

	/**
	 * 查询 get Mail Template Page 对应的数据。
	 *
	 * @param pageReqVO pageReqVO 参数
	 * @return 处理结果
	 */
	public PageResult<MailTemplateDO> getMailTemplatePage(MailTemplatePageReqVO pageReqVO) {

		return mailTemplateMapper.selectPage(pageReqVO);
	}

	/**
	 * 查询 get Mail Template List 对应的数据。
	 *
	 * @return 处理结果
	 */
	public List<MailTemplateDO> getMailTemplateList() {

		return mailTemplateMapper.selectList();
	}

	/**
	 * 执行 format Mail Template Content 对应的业务操作。
	 *
	 * @param content content 参数
	 * @param params  params 参数
	 * @return 处理结果
	 */
	public String formatMailTemplateContent(String content, Map<String, Object> params) {

		String formattedContent = StrUtil.format(content, params);
		formattedContent = unescapeHtml(formattedContent);
		formattedContent = formatHtmlCodeBlocks(formattedContent);
		return replaceOuterPreWithDiv(formattedContent);
	}

	/**
	 * 执行 replace Outer Pre With Div 对应的业务操作。
	 *
	 * @param content content 参数
	 * @return 处理结果
	 */
	private String replaceOuterPreWithDiv(String content) {

		if (StrUtil.isEmpty(content)) {
			return content;
		}
		Matcher matcher = Pattern.compile("(?s)<pre[^>]*>(.*?)</pre>").matcher(content);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			matcher.appendReplacement(sb, "<div>" + matcher.group(1) + "</div>");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 执行 unescape Html 对应的业务操作。
	 *
	 * @param input input 参数
	 * @return 处理结果
	 */
	private String unescapeHtml(String input) {

		if (StrUtil.isEmpty(input)) {
			return input;
		}
		return input.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'").replace("&nbsp;", " ");
	}

	/**
	 * 执行 format Html Code Blocks 对应的业务操作。
	 *
	 * @param content content 参数
	 * @return 处理结果
	 */
	private String formatHtmlCodeBlocks(String content) {

		Matcher matcher = Pattern.compile("<pre\\s*.*?><code\\s*.*?>(.*?)</code></pre>", Pattern.DOTALL).matcher(content);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			String codeBlock = matcher.group(1);
			String replacement = "<pre style=\"background-color: #f5f5f5; padding: 10px; border-radius: 5px; overflow-x: auto;\"><code>" + codeBlock + "</code></pre>";
			matcher.appendReplacement(sb, replacement);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 查询 get Mail Template Count By Account Id 对应的数据。
	 *
	 * @param accountId accountId 参数
	 * @return 处理结果
	 */
	public long getMailTemplateCountByAccountId(Long accountId) {

		return mailTemplateMapper.selectCountByAccountId(accountId);
	}

	/**
	 * 执行 parse Template Title And Content Params 对应的业务操作。
	 *
	 * @param title   title 参数
	 * @param content content 参数
	 * @return 处理结果
	 */
	@VisibleForTesting
	public List<String> parseTemplateTitleAndContentParams(String title, String content) {

		List<String> titleParams = ReUtil.findAllGroup1(PATTERN_PARAMS, title);
		List<String> contentParams = ReUtil.findAllGroup1(PATTERN_PARAMS, content);
		List<String> allParams = new ArrayList<>(titleParams);
		for (String param : contentParams) {
			if (!allParams.contains(param)) {
				allParams.add(param);
			}
		}
		return allParams;
	}

	/**
	 * 执行 parse Template Content Params 对应的业务操作。
	 *
	 * @param content content 参数
	 * @return 处理结果
	 */
	List<String> parseTemplateContentParams(String content) {

		return ReUtil.findAllGroup1(PATTERN_PARAMS, content);
	}

	/**
	 * 查询 get Mail Log Page 对应的数据。
	 *
	 * @param pageVO pageVO 参数
	 * @return 处理结果
	 */
	public PageResult<MailLogDO> getMailLogPage(MailLogPageReqVO pageVO) {

		return mailLogMapper.selectPage(pageVO);
	}

	/**
	 * 查询 get Mail Log 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public MailLogDO getMailLog(Long id) {

		return mailLogMapper.selectById(id);
	}

	/**
	 * 创建 create Mail Log 对应的数据。
	 *
	 * @param userId          userId 参数
	 * @param userType        userType 参数
	 * @param toMails         toMails 参数
	 * @param ccMails         ccMails 参数
	 * @param bccMails        bccMails 参数
	 * @param account         account 参数
	 * @param template        template 参数
	 * @param templateContent templateContent 参数
	 * @param templateParams  templateParams 参数
	 * @param isSend          isSend 参数
	 * @return 处理结果
	 */
	public Long createMailLog(Long userId, Integer userType, Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails, MailAccountDO account, MailTemplateDO template, String templateContent, Map<String, Object> templateParams, Boolean isSend) {

		MailLogDO logDO = MailLogDO.builder().sendStatus(Objects.equals(isSend, true) ? MailSendStatusEnum.INIT.getStatus() : MailSendStatusEnum.IGNORE.getStatus()).userId(userId).userType(userType).toMails(ListUtil.toList(toMails)).ccMails(ListUtil.toList(ccMails)).bccMails(ListUtil.toList(bccMails)).accountId(account.getId()).fromMail(account.getMail()).templateId(template.getId()).templateCode(template.getCode()).templateNickname(template.getNickname()).templateTitle(template.getTitle()).templateContent(templateContent).templateParams(templateParams).build();
		mailLogMapper.insert(logDO);
		return logDO.getId();
	}

	/**
	 * 更新 update Mail Send Result 对应的数据。
	 *
	 * @param logId     logId 参数
	 * @param messageId messageId 参数
	 * @param exception exception 参数
	 */
	public void updateMailSendResult(Long logId, String messageId, Exception exception) {

		if (exception == null) {
			mailLogMapper.updateById(new MailLogDO().setId(logId).setSendTime(LocalDateTime.now()).setSendStatus(MailSendStatusEnum.SUCCESS.getStatus()).setSendMessageId(messageId));
			return;
		}
		mailLogMapper.updateById(new MailLogDO().setId(logId).setSendTime(LocalDateTime.now()).setSendStatus(MailSendStatusEnum.FAILURE.getStatus()).setSendException(getRootCauseMessage(exception)));
	}

	/**
	 * 发送 send Single Mail To Admin 对应的消息。
	 *
	 * @param userId         userId 参数
	 * @param toMails        toMails 参数
	 * @param ccMails        ccMails 参数
	 * @param bccMails       bccMails 参数
	 * @param templateCode   templateCode 参数
	 * @param templateParams templateParams 参数
	 * @param attachments    attachments 参数
	 * @return 处理结果
	 */
	public Long sendSingleMailToAdmin(Long userId, Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails, String templateCode, Map<String, Object> templateParams, File... attachments) {

		return sendSingleMail(toMails, ccMails, bccMails, userId, UserTypeEnum.ADMIN.getValue(), templateCode, templateParams, attachments);
	}

	/**
	 * 发送 send Single Mail To Member 对应的消息。
	 *
	 * @param userId         userId 参数
	 * @param toMails        toMails 参数
	 * @param ccMails        ccMails 参数
	 * @param bccMails       bccMails 参数
	 * @param templateCode   templateCode 参数
	 * @param templateParams templateParams 参数
	 * @param attachments    attachments 参数
	 * @return 处理结果
	 */
	public Long sendSingleMailToMember(Long userId, Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails, String templateCode, Map<String, Object> templateParams, File... attachments) {

		return sendSingleMail(toMails, ccMails, bccMails, userId, UserTypeEnum.MEMBER.getValue(), templateCode, templateParams, attachments);
	}

	/**
	 * 发送 send Single Mail 对应的消息。
	 *
	 * @param toMails        toMails 参数
	 * @param ccMails        ccMails 参数
	 * @param bccMails       bccMails 参数
	 * @param userId         userId 参数
	 * @param userType       userType 参数
	 * @param templateCode   templateCode 参数
	 * @param templateParams templateParams 参数
	 * @param attachments    attachments 参数
	 * @return 处理结果
	 */
	public Long sendSingleMail(Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails, Long userId, Integer userType, String templateCode, Map<String, Object> templateParams, File... attachments) {

		MailTemplateDO template = validateMailTemplate(templateCode);
		MailAccountDO account = validateMailAccount(template.getAccountId());
		validateTemplateParams(template, templateParams);
		String userMail = getUserMail(userId, userType);
		Collection<String> toMailSet = new LinkedHashSet<>();
		Collection<String> ccMailSet = new LinkedHashSet<>();
		Collection<String> bccMailSet = new LinkedHashSet<>();
		if (Validator.isEmail(userMail)) {
			toMailSet.add(userMail);
		}
		if (CollUtil.isNotEmpty(toMails)) {
			toMails.stream().filter(Validator::isEmail).forEach(toMailSet::add);
		}
		if (CollUtil.isNotEmpty(ccMails)) {
			ccMails.stream().filter(Validator::isEmail).forEach(ccMailSet::add);
		}
		if (CollUtil.isNotEmpty(bccMails)) {
			bccMails.stream().filter(Validator::isEmail).forEach(bccMailSet::add);
		}
		if (CollUtil.isEmpty(toMailSet)) {
			throw exception(MAIL_SEND_MAIL_NOT_EXISTS);
		}
		Boolean isSend = CommonStatusEnum.ENABLE.getStatus().equals(template.getStatus());
		String title = formatMailTemplateContent(template.getTitle(), templateParams);
		String content = formatMailTemplateContent(template.getContent(), templateParams);
		Long sendLogId = createMailLog(userId, userType, toMailSet, ccMailSet, bccMailSet, account, template, content, templateParams, isSend);
		if (isSend) {
			mailProducer.sendMailSendMessage(sendLogId, toMailSet, ccMailSet, bccMailSet, account.getId(), template.getNickname(), title, content, attachments);
		}
		return sendLogId;
	}

	/**
	 * 查询 get User Mail 对应的数据。
	 *
	 * @param userId   userId 参数
	 * @param userType userType 参数
	 * @return 处理结果
	 */
	private String getUserMail(Long userId, Integer userType) {

		if (userId == null || userType == null) {
			return null;
		}
		if (UserTypeEnum.ADMIN.getValue().equals(userType)) {
			AdminUserDO user = adminUserService.getUser(userId);
			return user != null ? user.getEmail() : null;
		}
		if (UserTypeEnum.MEMBER.getValue().equals(userType)) {
			return memberApplicationService.getMemberUserEmail(userId);
		}
		return null;
	}

	/**
	 * 执行 do Send Mail 对应的业务操作。
	 *
	 * @param message message 参数
	 */
	public void doSendMail(MailSendMessage message) {

		MailAccountDO account = validateMailAccount(message.getAccountId());
		org.dromara.hutool.extra.mail.MailAccount mailAccount = buildMailAccount(account, message.getNickname());
		try {
			String messageId = MailUtil.send(mailAccount, message.getToMails(), message.getCcMails(), message.getBccMails(), message.getTitle(), message.getContent(), true, message.getAttachments());
			updateMailSendResult(message.getLogId(), messageId, null);
		} catch (Exception e) {
			updateMailSendResult(message.getLogId(), null, e);
		}
	}

	private org.dromara.hutool.extra.mail.MailAccount buildMailAccount(MailAccountDO account, String nickname) {

		String from = StrUtil.isNotEmpty(nickname) ? nickname + " <" + account.getMail() + ">" : account.getMail();
		return new org.dromara.hutool.extra.mail.MailAccount().setFrom(from).setAuth(true).setUser(account.getUsername()).setPass(account.getPassword().toCharArray()).setHost(account.getHost()).setPort(account.getPort()).setSslEnable(account.getSslEnable()).setStarttlsEnable(account.getStarttlsEnable());
	}

	/**
	 * 校验 validate Mail Template 对应的业务规则。
	 *
	 * @param templateCode templateCode 参数
	 * @return 处理结果
	 */
	@VisibleForTesting
	MailTemplateDO validateMailTemplate(String templateCode) {

		MailTemplateDO template = getMailTemplateByCodeFromCache(templateCode);
		if (template == null) {
			throw exception(MAIL_TEMPLATE_NOT_EXISTS);
		}
		return template;
	}

	/**
	 * 校验 validate Mail Account 对应的业务规则。
	 *
	 * @param accountId accountId 参数
	 * @return 处理结果
	 */
	@VisibleForTesting
	MailAccountDO validateMailAccount(Long accountId) {

		MailAccountDO account = getMailAccountFromCache(accountId);
		if (account == null) {
			throw exception(MAIL_ACCOUNT_NOT_EXISTS);
		}
		return account;
	}

	/**
	 * 校验 validate Template Params 对应的业务规则。
	 *
	 * @param template       template 参数
	 * @param templateParams templateParams 参数
	 */
	@VisibleForTesting
	void validateTemplateParams(MailTemplateDO template, Map<String, Object> templateParams) {

		template.getParams().forEach(key -> {
			Object value = templateParams.get(key);
			if (value == null) {
				throw exception(MAIL_SEND_TEMPLATE_PARAM_MISS, key);
			}
		});
	}

	/**
	 * 创建 create Account Domain 对应的数据。
	 *
	 * @param mail           mail 参数
	 * @param username       username 参数
	 * @param password       password 参数
	 * @param host           host 参数
	 * @param port           port 参数
	 * @param sslEnable      sslEnable 参数
	 * @param starttlsEnable starttlsEnable 参数
	 * @return 处理结果
	 */
	@Transactional
	public Long createAccountDomain(String mail, String username, String password, String host, String port, Boolean sslEnable, Boolean starttlsEnable) {

		MailAccount account = MailAccount.of(null, mail).username(username).password(password).host(host).port(port).sslEnable(sslEnable).starttlsEnable(starttlsEnable);
		accountRepo.save(account);
		return account.id();
	}

	/**
	 * 更新 update Account Domain 对应的数据。
	 *
	 * @param id             id 参数
	 * @param mail           mail 参数
	 * @param username       username 参数
	 * @param password       password 参数
	 * @param host           host 参数
	 * @param port           port 参数
	 * @param sslEnable      sslEnable 参数
	 * @param starttlsEnable starttlsEnable 参数
	 */
	@Transactional
	public void updateAccountDomain(Long id, String mail, String username, String password, String host, String port, Boolean sslEnable, Boolean starttlsEnable) {

		if (accountRepo.findById(id) == null) {
			throw exception(MAIL_ACCOUNT_NOT_EXISTS);
		}
		accountRepo.save(MailAccount.of(id, mail).username(username).password(password).host(host).port(port).sslEnable(sslEnable).starttlsEnable(starttlsEnable));
	}

	/**
	 * 删除 delete Account Domain 对应的数据。
	 *
	 * @param id id 参数
	 */
	@Transactional
	public void deleteAccountDomain(Long id) {

		accountRepo.delete(id);
	}

	/**
	 * 查询 get Account Domain 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public MailAccount getAccountDomain(Long id) {

		return accountRepo.findById(id);
	}

	/**
	 * 查询 get Account Domain List 对应的数据。
	 *
	 * @return 处理结果
	 */
	public List<MailAccount> getAccountDomainList() {

		return accountRepo.findAll();
	}

	/**
	 * 查询 get Account Domain Page 对应的数据。
	 *
	 * @param mail     mail 参数
	 * @param username username 参数
	 * @param pageNo   pageNo 参数
	 * @param pageSize pageSize 参数
	 * @return 处理结果
	 */
	public PageResult<MailAccount> getAccountDomainPage(String mail, String username, Integer pageNo, Integer pageSize) {

		return accountRepo.findPage(mail, username, pageNo, pageSize);
	}

	/**
	 * 创建 create Template Domain 对应的数据。
	 *
	 * @param code      code 参数
	 * @param name      name 参数
	 * @param accountId accountId 参数
	 * @param nickname  nickname 参数
	 * @param title     title 参数
	 * @param content   content 参数
	 * @param status    status 参数
	 * @param remark    remark 参数
	 * @return 处理结果
	 */
	@Transactional
	public Long createTemplateDomain(String code, String name, Long accountId, String nickname, String title, String content, Integer status, String remark) {

		MailTemplate template = MailTemplate.of(null, code, name).accountId(accountId).nickname(nickname).title(title).content(content).status(status).remark(remark);
		templateRepo.save(template);
		return template.id();
	}

	/**
	 * 更新 update Template Domain 对应的数据。
	 *
	 * @param id        id 参数
	 * @param code      code 参数
	 * @param name      name 参数
	 * @param accountId accountId 参数
	 * @param nickname  nickname 参数
	 * @param title     title 参数
	 * @param content   content 参数
	 * @param status    status 参数
	 * @param remark    remark 参数
	 */
	@Transactional
	public void updateTemplateDomain(Long id, String code, String name, Long accountId, String nickname, String title, String content, Integer status, String remark) {

		if (templateRepo.findById(id) == null) {
			throw exception(MAIL_TEMPLATE_NOT_EXISTS);
		}
		templateRepo.save(MailTemplate.of(id, code, name).accountId(accountId).nickname(nickname).title(title).content(content).status(status).remark(remark));
	}

	/**
	 * 删除 delete Template Domain 对应的数据。
	 *
	 * @param id id 参数
	 */
	@Transactional
	public void deleteTemplateDomain(Long id) {

		templateRepo.delete(id);
	}

	/**
	 * 查询 get Template Domain 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public MailTemplate getTemplateDomain(Long id) {

		return templateRepo.findById(id);
	}

	/**
	 * 查询 get Template Domain By Code 对应的数据。
	 *
	 * @param code code 参数
	 * @return 处理结果
	 */
	public MailTemplate getTemplateDomainByCode(String code) {

		return templateRepo.findByCode(code).orElse(null);
	}

	/**
	 * 查询 get Template Domain List 对应的数据。
	 *
	 * @return 处理结果
	 */
	public List<MailTemplate> getTemplateDomainList() {

		return templateRepo.findAll();
	}

	/**
	 * 查询 get Template Domain Page 对应的数据。
	 *
	 * @param name     name 参数
	 * @param code     code 参数
	 * @param status   status 参数
	 * @param pageNo   pageNo 参数
	 * @param pageSize pageSize 参数
	 * @return 处理结果
	 */
	public PageResult<MailTemplate> getTemplateDomainPage(String name, String code, Integer status, Integer pageNo, Integer pageSize) {

		return templateRepo.findPage(name, code, status, pageNo, pageSize);
	}

	/**
	 * 查询 get Template Domain Count By Account Id 对应的数据。
	 *
	 * @param accountId accountId 参数
	 * @return 处理结果
	 */
	public long getTemplateDomainCountByAccountId(Long accountId) {

		return templateRepo.countByAccountId(accountId);
	}

}
