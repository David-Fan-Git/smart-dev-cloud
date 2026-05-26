package com.develop.mvp.pk.module.system.domain.notify.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.domain.notify.NotifyMessage;
import java.util.*;

/**
 * Notify Message Repository 领域仓储接口。
 */
public interface NotifyMessageRepository {
    /**
     * 创建 save 对应的数据。
     *
     * @param m m 参数
     * @return 处理结果
     */
    NotifyMessage save(NotifyMessage m);
    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    NotifyMessage findById(Long id);
    /**
     * 查询 find Page 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param readStatus readStatus 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    PageResult<NotifyMessage> findPage(Long userId, Integer userType, Integer readStatus, Integer pageNo, Integer pageSize);
    /**
     * 更新 update Read Status 对应的数据。
     *
     * @param ids ids 参数
     * @param readStatus readStatus 参数
     */
    void updateReadStatus(Collection<Long> ids, Integer readStatus);
}
