package com.develop.mvp.pk.module.system.domain.sms.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.domain.sms.SmsChannel;
import java.util.List;

/**
 * Sms Channel Repository 领域仓储接口。
 */
public interface SmsChannelRepository {
    /**
     * 创建 save 对应的数据。
     *
     * @param c c 参数
     * @return 处理结果
     */
    SmsChannel save(SmsChannel c);
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
    SmsChannel findById(Long id);
    /**
     * 查询 find By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    SmsChannel findByCode(String code);
    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    List<SmsChannel> findAll();
    /**
     * 查询 find Page 对应的数据。
     *
     * @param signature signature 参数
     * @param status status 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    PageResult<SmsChannel> findPage(String signature, Integer status, Integer pageNo, Integer pageSize);
}
