package com.develop.mvp.pk.module.bpm.domain.expression;
// DDD 角色：BPM流程表达式工厂 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.module.bpm.domain.expression.valueobject.*;

public final class BpmProcessExpressionFactory {
    private BpmProcessExpressionFactory() {}
    public static BpmProcessExpression create(Long id, String name, String expression) {
        return new BpmProcessExpression(ExpressionId.of(id), ExpressionName.of(name), ExpressionStatus.ENABLED, expression);
    }
    public static BpmProcessExpression reconstitute(Long id, String name, Integer status, String expression) {
        return new BpmProcessExpression(ExpressionId.of(id), ExpressionName.of(name), ExpressionStatus.of(status), expression);
    }
}
