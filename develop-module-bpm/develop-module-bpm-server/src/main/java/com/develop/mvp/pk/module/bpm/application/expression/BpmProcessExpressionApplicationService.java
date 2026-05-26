package com.develop.mvp.pk.module.bpm.application.expression;
// DDD 角色：BPM流程表达式应用服务 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.expression.BpmProcessExpression;
import com.develop.mvp.pk.module.bpm.domain.expression.BpmProcessExpressionFactory;
import com.develop.mvp.pk.module.bpm.domain.expression.event.ExpressionDomainEvent;
import com.develop.mvp.pk.module.bpm.domain.expression.repository.BpmProcessExpressionRepository;
import com.develop.mvp.pk.module.bpm.domain.expression.valueobject.ExpressionId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.bpm.enums.ErrorCodeConstants.PROCESS_EXPRESSION_NOT_EXISTS;

@Service
@RequiredArgsConstructor
public class BpmProcessExpressionApplicationService {
    private final BpmProcessExpressionRepository repo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(String name, Integer status, String expression) {
        BpmProcessExpression e = BpmProcessExpressionFactory.create(null, name, expression);
        repo.save(e);
        publishEvents(e);
        return e.id().value();
    }

    @Transactional
    public void update(Long id, String name, Integer status, String expression) {
        findExisting(id);
        BpmProcessExpression e = BpmProcessExpressionFactory.reconstitute(id, name, status, expression);
        repo.save(e);
        publishEvents(e);
    }

    @Transactional
    public void delete(Long id) {
        findExisting(id);
        BpmProcessExpression e = BpmProcessExpressionFactory.reconstitute(id, "", 0, null);
        e.markDeleted();
        repo.delete(e.id());
        publishEvents(e);
    }

    public BpmProcessExpression get(Long id) { return repo.findById(ExpressionId.of(id)); }
    public PageResult<BpmProcessExpression> getPage(String name, Integer status, Integer pageNo, Integer pageSize) {
        return repo.findPage(name, status, pageNo, pageSize);
    }

    private BpmProcessExpression findExisting(Long id) {
        BpmProcessExpression e = repo.findById(ExpressionId.of(id));
        if (e == null) throw exception(PROCESS_EXPRESSION_NOT_EXISTS);
        return e;
    }

    private void publishEvents(BpmProcessExpression expression) {
        for (ExpressionDomainEvent event : expression.pullEvents()) eventPublisher.publishEvent(event);
    }
}
