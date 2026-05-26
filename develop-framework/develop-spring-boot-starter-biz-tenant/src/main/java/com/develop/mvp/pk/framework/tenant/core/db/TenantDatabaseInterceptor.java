package com.develop.mvp.pk.framework.tenant.core.db;

import com.develop.mvp.pk.framework.tenant.config.TenantProperties;
import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnore;
import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.toolkit.SqlParserUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于 MyBatis Plus 多租户能力，实现 DB 层面的租户 SQL 改写。
 *
 * <p>本类不是 MyBatis 插件本身，而是 {@link com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor}
 * 使用的 {@link TenantLineHandler}：当 MyBatis Plus 解析 SQL 时，会回调本类判断某张表是否忽略租户，
 * 并在需要拦截的 SQL 中取得当前租户编号表达式。租户编号来自 {@link TenantContextHolder}，通常由 Web、MQ 或 Job
 * 等入口提前写入。</p>
 *
 * <p>边界需要特别明确：它只参与 MyBatis Plus 能解析并处理的 SQL；对配置忽略的表、非项目实体表、
 * 标记了 {@link TenantIgnore} 的实体，或当前上下文显式 ignore 的场景，会跳过租户条件。它不会校验租户是否存在或有效，
 * 也不会在缺少 tenantId 时自动放行。</p>
 *
 * @author David
 */
public class TenantDatabaseInterceptor implements TenantLineHandler {

    /**
     * 忽略的表
     *
     * KEY：表名
     * VALUE：是否忽略
     */
    private final Map<String, Boolean> ignoreTables = new HashMap<>();

    public TenantDatabaseInterceptor(TenantProperties properties) {
        // 不同 DB 下，大小写的习惯不同，所以需要都添加进去
        properties.getIgnoreTables().forEach(table -> {
            addIgnoreTable(table, true);
        });
        // 在 OracleKeyGenerator 中，生成主键时，会查询这个表，查询这个表后，会自动拼接 TENANT_ID 导致报错
        addIgnoreTable("DUAL", true);
    }

    /**
     * 返回要拼到 SQL 中的租户编号表达式。
     *
     * <p>MyBatis Plus 在需要追加租户条件时调用本方法。tenantId 必须已经由上游入口写入
     * {@link TenantContextHolder}；如果缺失，{@link TenantContextHolder#getRequiredTenantId()} 会直接抛错，
     * 这样可以区分“没有租户编号的错误请求”和“显式 ignore tenant 的跨租户逻辑”。</p>
     */
    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContextHolder.getRequiredTenantId());
    }

    /**
     * 判断指定表是否跳过租户条件。
     *
     * <p>判断顺序体现了拦截边界：如果当前上下文已经显式 ignore，则本次 SQL 不拼租户条件；否则再看表级规则。
     * 表名会先去掉数据库方言包裹符号，然后命中配置缓存或根据实体元数据计算。该方法只返回“是否跳过 SQL 改写”，
     * 不会修改上下文，也不会补充 tenantId。</p>
     */
    @Override
    public boolean ignoreTable(String tableName) {
        // 情况一，全局忽略多租户
        if (TenantContextHolder.isIgnore()) {
            return true;
        }
        // 情况二，忽略多租户的表
        tableName = SqlParserUtils.removeWrapperSymbol(tableName);
        Boolean ignore = ignoreTables.get(tableName.toLowerCase());
        if (ignore == null) {
            ignore = computeIgnoreTable(tableName);
            synchronized (ignoreTables) {
                addIgnoreTable(tableName, ignore);
            }
        }
        return ignore;
    }

    private void addIgnoreTable(String tableName, boolean ignore) {
        ignoreTables.put(tableName.toLowerCase(), ignore);
        ignoreTables.put(tableName.toUpperCase(), ignore);
    }

    private boolean computeIgnoreTable(String tableName) {
        // 找不到的表，说明不是 develop 项目里的，不进行拦截（忽略租户）
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
        if (tableInfo == null) {
            return true;
        }
        // 如果继承了 TenantBaseDO 基类，显然不忽略租户
        if (TenantBaseDO.class.isAssignableFrom(tableInfo.getEntityType())) {
            return false;
        }
        // 如果添加了 @TenantIgnore 注解，则忽略租户
        TenantIgnore tenantIgnore = tableInfo.getEntityType().getAnnotation(TenantIgnore.class);
        return tenantIgnore != null;
    }

}
