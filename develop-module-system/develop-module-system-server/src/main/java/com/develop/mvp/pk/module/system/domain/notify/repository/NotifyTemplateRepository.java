package com.develop.mvp.pk.module.system.domain.notify.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.domain.notify.NotifyTemplate;
import java.util.List;
import java.util.Optional;

/**
 * Notify Template Repository 领域仓储接口。
 */
public interface NotifyTemplateRepository {
    /**
     * 创建 save 对应的数据。
     *
     * @param t t 参数
     * @return 处理结果
     */
    NotifyTemplate save(NotifyTemplate t);
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
    NotifyTemplate findById(Long id);
    /**
     * 查询 find By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    Optional<NotifyTemplate> findByCode(String code);
    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    List<NotifyTemplate> findAll();
    /**
     * 查询 find Page 对应的数据。
     *
     * @param name name 参数
     * @param code code 参数
     * @param status status 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    PageResult<NotifyTemplate> findPage(String name, String code, Integer status, Integer pageNo, Integer pageSize);
}
