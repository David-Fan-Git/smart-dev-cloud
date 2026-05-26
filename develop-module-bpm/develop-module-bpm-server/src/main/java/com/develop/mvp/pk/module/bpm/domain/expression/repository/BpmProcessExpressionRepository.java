package com.develop.mvp.pk.module.bpm.domain.expression.repository;
// DDD 角色：BPM流程表达式仓储接口 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.expression.BpmProcessExpression;
import com.develop.mvp.pk.module.bpm.domain.expression.valueobject.ExpressionId;

public interface BpmProcessExpressionRepository {
    void save(BpmProcessExpression expression);
    void delete(ExpressionId id);
    BpmProcessExpression findById(ExpressionId id);
    PageResult<BpmProcessExpression> findPage(String name, Integer status, Integer pageNo, Integer pageSize);
}
