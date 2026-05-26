package com.develop.mvp.pk.module.system.application.mail.port.inbound;

// DDD 角色：入站端口 — 定义 Mail 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.account.MailAccountSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.template.MailTemplateSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.mail.MailAccountDO;
import com.develop.mvp.pk.module.system.dal.dataobject.mail.MailLogDO;
import com.develop.mvp.pk.module.system.dal.dataobject.mail.MailTemplateDO;
import com.develop.mvp.pk.module.system.domain.mail.MailAccount;
import com.develop.mvp.pk.module.system.domain.mail.MailTemplate;
import com.develop.mvp.pk.module.system.mq.message.mail.MailSendMessage;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Mail 聚合的入站用例端口。
 */
public interface MailUseCase {

	/**
	 * 创建 create Mail Account 对应的数据。
	 *
	 * @param createReqVO createReqVO 参数
	 * @return 处理结果
	 */
	Long createMailAccount(MailAccountSaveReqVO createReqVO);

	/**
	 * 更新 update Mail Account 对应的数据。
	 *
	 * @param updateReqVO updateReqVO 参数
	 */
	void updateMailAccount(MailAccountSaveReqVO updateReqVO);

	/**
	 * 删除 delete Mail Account 对应的数据。
	 *
	 * @param id id 参数
	 */
	void deleteMailAccount(Long id);

	/**
	 * 删除 delete Mail Account List 对应的数据。
	 *
	 * @param ids ids 参数
	 */
	void deleteMailAccountList(List<Long> ids);

	/**
	 * 查询 get Mail Account 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	MailAccountDO getMailAccount(Long id);

	/**
	 * 查询 get Mail Account From Cache 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	MailAccountDO getMailAccountFromCache(Long id);

	/**
	 * 查询 get Mail Account Page 对应的数据。
	 *
	 * @param pageReqVO pageReqVO 参数
	 * @return 处理结果
	 */
	PageResult<MailAccountDO> getMailAccountPage(MailAccountPageReqVO pageReqVO);

	/**
	 * 查询 get Mail Account List 对应的数据。
	 *
	 * @return 处理结果
	 */
	List<MailAccountDO> getMailAccountList();

	/**
	 * 创建 create Mail Template 对应的数据。
	 *
	 * @param createReqVO createReqVO 参数
	 * @return 处理结果
	 */
	Long createMailTemplate(MailTemplateSaveReqVO createReqVO);

	/**
	 * 更新 update Mail Template 对应的数据。
	 *
	 * @param updateReqVO updateReqVO 参数
	 */
	void updateMailTemplate(MailTemplateSaveReqVO updateReqVO);

	/**
	 * 删除 delete Mail Template 对应的数据。
	 *
	 * @param id id 参数
	 */
	void deleteMailTemplate(Long id);

	/**
	 * 删除 delete Mail Template List 对应的数据。
	 *
	 * @param ids ids 参数
	 */
	void deleteMailTemplateList(List<Long> ids);

	/**
	 * 查询 get Mail Template 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	MailTemplateDO getMailTemplate(Long id);

	/**
	 * 查询 get Mail Template By Code From Cache 对应的数据。
	 *
	 * @param code code 参数
	 * @return 处理结果
	 */
	MailTemplateDO getMailTemplateByCodeFromCache(String code);

	/**
	 * 查询 get Mail Template Page 对应的数据。
	 *
	 * @param pageReqVO pageReqVO 参数
	 * @return 处理结果
	 */
	PageResult<MailTemplateDO> getMailTemplatePage(MailTemplatePageReqVO pageReqVO);

	/**
	 * 查询 get Mail Template List 对应的数据。
	 *
	 * @return 处理结果
	 */
	List<MailTemplateDO> getMailTemplateList();

	/**
	 * 执行 format Mail Template Content 对应的业务操作。
	 *
	 * @param content content 参数
	 * @param params  params 参数
	 * @return 处理结果
	 */
	String formatMailTemplateContent(String content, Map<String, Object> params);

	/**
	 * 查询 get Mail Template Count By Account Id 对应的数据。
	 *
	 * @param accountId accountId 参数
	 * @return 处理结果
	 */
	long getMailTemplateCountByAccountId(Long accountId);

	/**
	 * 查询 get Mail Log Page 对应的数据。
	 *
	 * @param pageVO pageVO 参数
	 * @return 处理结果
	 */
	PageResult<MailLogDO> getMailLogPage(MailLogPageReqVO pageVO);

	/**
	 * 查询 get Mail Log 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	MailLogDO getMailLog(Long id);

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
	Long createMailLog(Long userId, Integer userType, Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails, MailAccountDO account, MailTemplateDO template, String templateContent, Map<String, Object> templateParams, Boolean isSend);

	/**
	 * 更新 update Mail Send Result 对应的数据。
	 *
	 * @param logId     logId 参数
	 * @param messageId messageId 参数
	 * @param exception exception 参数
	 */
	void updateMailSendResult(Long logId, String messageId, Exception exception);

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
	Long sendSingleMailToAdmin(Long userId, Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails, String templateCode, Map<String, Object> templateParams, File... attachments);

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
	Long sendSingleMailToMember(Long userId, Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails, String templateCode, Map<String, Object> templateParams, File... attachments);

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
	Long sendSingleMail(Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails, Long userId, Integer userType, String templateCode, Map<String, Object> templateParams, File... attachments);

	/**
	 * 执行 do Send Mail 对应的业务操作。
	 *
	 * @param message message 参数
	 */
	void doSendMail(MailSendMessage message);

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
	Long createAccountDomain(String mail, String username, String password, String host, String port, Boolean sslEnable, Boolean starttlsEnable);

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
	void updateAccountDomain(Long id, String mail, String username, String password, String host, String port, Boolean sslEnable, Boolean starttlsEnable);

	/**
	 * 删除 delete Account Domain 对应的数据。
	 *
	 * @param id id 参数
	 */
	void deleteAccountDomain(Long id);

	/**
	 * 查询 get Account Domain 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	MailAccount getAccountDomain(Long id);

	/**
	 * 查询 get Account Domain List 对应的数据。
	 *
	 * @return 处理结果
	 */
	List<MailAccount> getAccountDomainList();

	/**
	 * 查询 get Account Domain Page 对应的数据。
	 *
	 * @param mail     mail 参数
	 * @param username username 参数
	 * @param pageNo   pageNo 参数
	 * @param pageSize pageSize 参数
	 * @return 处理结果
	 */
	PageResult<MailAccount> getAccountDomainPage(String mail, String username, Integer pageNo, Integer pageSize);

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
	Long createTemplateDomain(String code, String name, Long accountId, String nickname, String title, String content, Integer status, String remark);

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
	void updateTemplateDomain(Long id, String code, String name, Long accountId, String nickname, String title, String content, Integer status, String remark);

	/**
	 * 删除 delete Template Domain 对应的数据。
	 *
	 * @param id id 参数
	 */
	void deleteTemplateDomain(Long id);

	/**
	 * 查询 get Template Domain 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	MailTemplate getTemplateDomain(Long id);

	/**
	 * 查询 get Template Domain By Code 对应的数据。
	 *
	 * @param code code 参数
	 * @return 处理结果
	 */
	MailTemplate getTemplateDomainByCode(String code);

	/**
	 * 查询 get Template Domain List 对应的数据。
	 *
	 * @return 处理结果
	 */
	List<MailTemplate> getTemplateDomainList();

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
	PageResult<MailTemplate> getTemplateDomainPage(String name, String code, Integer status, Integer pageNo, Integer pageSize);

	/**
	 * 查询 get Template Domain Count By Account Id 对应的数据。
	 *
	 * @param accountId accountId 参数
	 * @return 处理结果
	 */
	long getTemplateDomainCountByAccountId(Long accountId);

}
