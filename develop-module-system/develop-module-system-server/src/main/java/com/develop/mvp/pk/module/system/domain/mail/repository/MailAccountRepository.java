package com.develop.mvp.pk.module.system.domain.mail.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.domain.mail.MailAccount;
import java.util.List;
import java.util.Optional;

/**
 * Mail Account Repository 领域仓储接口。
 */
public interface MailAccountRepository {
    /**
     * 创建 save 对应的数据。
     *
     * @param a a 参数
     * @return 处理结果
     */
    MailAccount save(MailAccount a);
    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    void delete(Long id);
    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    MailAccount findById(Long id);
    /**
     * 查询 find By Mail 对应的数据。
     *
     * @param mail mail 参数
     * @return 处理结果
     */
    Optional<MailAccount> findByMail(String mail);
    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    List<MailAccount> findAll();
    /**
     * 查询 find Page 对应的数据。
     *
     * @param mail mail 参数
     * @param username username 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    PageResult<MailAccount> findPage(String mail, String username, Integer pageNo, Integer pageSize);
}
