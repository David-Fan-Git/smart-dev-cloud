package com.develop.mvp.pk.module.bpm.infrastructure.expression;
// DDD 角色：BPM流程表达式仓储实现 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.expression.BpmProcessExpressionPageReqVO;
import com.develop.mvp.pk.module.bpm.dal.dataobject.definition.BpmProcessExpressionDO;
import com.develop.mvp.pk.module.bpm.dal.mysql.definition.BpmProcessExpressionMapper;
import com.develop.mvp.pk.module.bpm.domain.expression.BpmProcessExpression;
import com.develop.mvp.pk.module.bpm.domain.expression.BpmProcessExpressionFactory;
import com.develop.mvp.pk.module.bpm.domain.expression.repository.BpmProcessExpressionRepository;
import com.develop.mvp.pk.module.bpm.domain.expression.valueobject.ExpressionId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Repository
public class BpmProcessExpressionRepositoryImpl implements BpmProcessExpressionRepository {
    private final BpmProcessExpressionMapper mapper;
    public BpmProcessExpressionRepositoryImpl(BpmProcessExpressionMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public void save(BpmProcessExpression expression) {
        BpmProcessExpressionDO d = toDO(expression);
        if (mapper.selectById(expression.id().value()) == null) mapper.insert(d);
        else mapper.updateById(d);
    }

    @Override @Transactional
    public void delete(ExpressionId id) { mapper.deleteById(id.value()); }

    @Override
    public BpmProcessExpression findById(ExpressionId id) { return fromDO(mapper.selectById(id.value())); }

    @Override
    public PageResult<BpmProcessExpression> findPage(String name, Integer status, Integer pageNo, Integer pageSize) {
        BpmProcessExpressionPageReqVO req = new BpmProcessExpressionPageReqVO().setName(name).setStatus(status);
        req.setPageNo(pageNo); req.setPageSize(pageSize);
        PageResult<BpmProcessExpressionDO> page = mapper.selectPage(req);
        return new PageResult<>(page.getList().stream().map(this::fromDO).collect(Collectors.toList()), page.getTotal());
    }

    private BpmProcessExpressionDO toDO(BpmProcessExpression e) {
        BpmProcessExpressionDO d = new BpmProcessExpressionDO();
        d.setId(e.id().value()); d.setName(e.name().value());
        d.setStatus(e.status().code()); d.setExpression(e.expression());
        return d;
    }

    private BpmProcessExpression fromDO(BpmProcessExpressionDO d) {
        if (d == null) return null;
        return BpmProcessExpressionFactory.reconstitute(d.getId(), d.getName(), d.getStatus(), d.getExpression());
    }
}
