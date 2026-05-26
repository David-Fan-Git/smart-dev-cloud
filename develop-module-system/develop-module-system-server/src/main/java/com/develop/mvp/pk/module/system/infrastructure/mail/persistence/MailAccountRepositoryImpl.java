package com.develop.mvp.pk.module.system.infrastructure.mail.persistence;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.mail.MailAccountDO;
import com.develop.mvp.pk.module.system.dal.mysql.mail.MailAccountMapper;
import com.develop.mvp.pk.module.system.domain.mail.MailAccount;
import com.develop.mvp.pk.module.system.domain.mail.repository.MailAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Mail Account Repository Impl 领域仓储实现。
 */
@Repository
public class MailAccountRepositoryImpl implements MailAccountRepository {
    private final MailAccountMapper mapper;
    /**
     * 创建 MailAccountRepositoryImpl 实例。
     *
     * @param mapper mapper 参数
     */
    public MailAccountRepositoryImpl(MailAccountMapper mapper) { this.mapper = mapper; }

    /**
     * 创建 save 对应的数据。
     *
     * @param a a 参数
     * @return 处理结果
     */
    @Override
    public MailAccount save(MailAccount a) {
        MailAccountDO d = new MailAccountDO();
        d.setId(a.id());
        d.setMail(a.mail());
        d.setUsername(a.username());
        d.setPassword(a.password());
        d.setHost(a.host());
        if (a.port() != null) d.setPort(Integer.valueOf(a.port()));
        d.setSslEnable(a.sslEnable());
        d.setStarttlsEnable(a.starttlsEnable());
        if (mapper.selectById(a.id()) == null) mapper.insert(d);
        else mapper.updateById(d);
        return a;
    }

    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    @Override
    public void delete(Long id) { mapper.deleteById(id); }

    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public MailAccount findById(Long id) {
        MailAccountDO d = mapper.selectById(id);
        return d != null ? toDomain(d) : null;
    }

    /**
     * 查询 find By Mail 对应的数据。
     *
     * @param mail mail 参数
     * @return 处理结果
     */
    @Override
    public Optional<MailAccount> findByMail(String mail) {
        return Optional.ofNullable(mapper.selectOne(MailAccountDO::getMail, mail)).map(this::toDomain);
    }

    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    public List<MailAccount> findAll() {
        return mapper.selectList().stream().map(this::toDomain).toList();
    }

    /**
     * 查询 find Page 对应的数据。
     *
     * @param mail mail 参数
     * @param username username 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    @Override
    public PageResult<MailAccount> findPage(String mail, String username, Integer pageNo, Integer pageSize) {
        var reqVO = new MailAccountPageReqVO();
        reqVO.setMail(mail);
        reqVO.setUsername(username);
        reqVO.setPageNo(pageNo);
        reqVO.setPageSize(pageSize);
        var dp = mapper.selectPage(reqVO);
        return new PageResult<>(dp.getList().stream().map(this::toDomain).toList(), dp.getTotal());
    }

    /**
     * 执行 to Domain 对应的业务操作。
     *
     * @param d d 参数
     * @return 处理结果
     */
    private MailAccount toDomain(MailAccountDO d) {
        return MailAccount.of(d.getId(), d.getMail())
                .username(d.getUsername())
                .password(d.getPassword())
                .host(d.getHost())
                .port(d.getPort() != null ? String.valueOf(d.getPort()) : null)
                .sslEnable(d.getSslEnable())
                .starttlsEnable(d.getStarttlsEnable());
    }
}
