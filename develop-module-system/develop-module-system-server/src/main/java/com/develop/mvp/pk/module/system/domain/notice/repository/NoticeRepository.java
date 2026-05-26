package com.develop.mvp.pk.module.system.domain.notice.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.notice.vo.NoticePageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.notice.NoticeDO;
import com.develop.mvp.pk.module.system.domain.notice.Notice;

import java.util.List;

/**
 * Notice Repository 领域仓储接口。
 */
public interface NoticeRepository {

    /**
     * 创建 insert 对应的数据。
     *
     * @param notice notice 参数
     * @return 处理结果
     */
    NoticeDO insert(Notice notice);

    /**
     * 更新 update 对应的数据。
     *
     * @param notice notice 参数
     */
    void update(Notice notice);

    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    void delete(Long id);

    /**
     * 删除 delete By Ids 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteByIds(List<Long> ids);

    /**
     * 查询 find Do By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    NoticeDO findDoById(Long id);

    /**
     * 查询 find Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    PageResult<NoticeDO> findPage(NoticePageReqVO reqVO);

}
