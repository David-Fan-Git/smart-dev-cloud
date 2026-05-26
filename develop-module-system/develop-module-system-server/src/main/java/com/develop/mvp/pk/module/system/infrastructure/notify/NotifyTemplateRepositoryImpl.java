package com.develop.mvp.pk.module.system.infrastructure.notify;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.template.NotifyTemplatePageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.notify.NotifyTemplateDO;
import com.develop.mvp.pk.module.system.dal.mysql.notify.NotifyTemplateMapper;
import com.develop.mvp.pk.module.system.domain.notify.NotifyTemplate;
import com.develop.mvp.pk.module.system.domain.notify.repository.NotifyTemplateRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

/**
 * Notify Template Repository Impl 领域仓储实现。
 */
@Repository
public class NotifyTemplateRepositoryImpl implements NotifyTemplateRepository {
    private final NotifyTemplateMapper mapper;
    /**
     * 创建 NotifyTemplateRepositoryImpl 实例。
     *
     * @param mapper mapper 参数
     */
    public NotifyTemplateRepositoryImpl(NotifyTemplateMapper mapper) { this.mapper = mapper; }

    /**
     * 创建 save 对应的数据。
     *
     * @param t t 参数
     * @return 处理结果
     */
    @Override
    public NotifyTemplate save(NotifyTemplate t) {
        NotifyTemplateDO d = new NotifyTemplateDO(); d.setId(t.id()); d.setCode(t.code()); d.setName(t.name());
        d.setNickname(t.nickname()); d.setContent(t.content()); d.setType(t.type()); d.setStatus(t.status()); d.setRemark(t.remark());
        if (mapper.selectById(t.id()) == null) mapper.insert(d); else mapper.updateById(d);
        return t;
    }
    /**
     * 删除通知模板。
     *
     * @param id 通知模板编号
     */
    @Override public void delete(Long id) { mapper.deleteById(id); }
    /**
     * 根据编号查询通知模板。
     *
     * @param id 通知模板编号
     * @return 通知模板领域对象，不存在时返回 null
     */
    @Override public NotifyTemplate findById(Long id) { NotifyTemplateDO d = mapper.selectById(id); return d != null ? toDomain(d) : null; }
    /**
     * 根据编码查询通知模板。
     *
     * @param code 通知模板编码
     * @return 通知模板领域对象
     */
    @Override public Optional<NotifyTemplate> findByCode(String code) { return Optional.ofNullable(mapper.selectOne(NotifyTemplateDO::getCode, code)).map(this::toDomain); }
    /**
     * 查询全部通知模板。
     *
     * @return 通知模板列表
     */
    @Override public List<NotifyTemplate> findAll() { return mapper.selectList().stream().map(this::toDomain).toList(); }
    /**
     * 分页查询通知模板。
     *
     * @param name 模板名称
     * @param code 模板编码
     * @param status 模板状态
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 通知模板分页结果
     */
    @Override
    public PageResult<NotifyTemplate> findPage(String name, String code, Integer status, Integer pageNo, Integer pageSize) {
        var reqVO = new NotifyTemplatePageReqVO(); reqVO.setName(name); reqVO.setCode(code); reqVO.setStatus(status); reqVO.setPageNo(pageNo); reqVO.setPageSize(pageSize);
        var dp = mapper.selectPage(reqVO); return new PageResult<>(dp.getList().stream().map(this::toDomain).toList(), dp.getTotal());
    }
    /**
     * 执行 to Domain 对应的业务操作。
     *
     * @param d d 参数
     * @return 处理结果
     */
    private NotifyTemplate toDomain(NotifyTemplateDO d) {
        return NotifyTemplate.of(d.getId(), d.getCode(), d.getName()).nickname(d.getNickname()).content(d.getContent()).type(d.getType()).status(d.getStatus()).remark(d.getRemark());
    }
}
