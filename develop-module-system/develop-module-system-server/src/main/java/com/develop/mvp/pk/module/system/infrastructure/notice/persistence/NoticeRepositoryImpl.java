package com.develop.mvp.pk.module.system.infrastructure.notice.persistence;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.notice.vo.NoticePageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.notice.NoticeDO;
import com.develop.mvp.pk.module.system.dal.mysql.notice.NoticeMapper;
import com.develop.mvp.pk.module.system.domain.notice.Notice;
import com.develop.mvp.pk.module.system.domain.notice.repository.NoticeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Notice Repository Impl 领域仓储实现。
 */
@Repository
public class NoticeRepositoryImpl implements NoticeRepository {

    private final NoticeMapper mapper;

    /**
     * 创建 NoticeRepositoryImpl 实例。
     *
     * @param mapper mapper 参数
     */
    public NoticeRepositoryImpl(NoticeMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 创建 insert 对应的数据。
     *
     * @param notice notice 参数
     * @return 处理结果
     */
    @Override
    public NoticeDO insert(Notice notice) {
        NoticeDO noticeDO = toDataObject(notice);
        mapper.insert(noticeDO);
        return noticeDO;
    }

    /**
     * 更新 update 对应的数据。
     *
     * @param notice notice 参数
     */
    @Override
    public void update(Notice notice) {
        mapper.updateById(toDataObject(notice));
    }

    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /**
     * 删除 delete By Ids 对应的数据。
     *
     * @param ids ids 参数
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        mapper.deleteByIds(ids);
    }

    /**
     * 查询 find Do By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public NoticeDO findDoById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 查询 find Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @Override
    public PageResult<NoticeDO> findPage(NoticePageReqVO reqVO) {
        return mapper.selectPage(reqVO);
    }

    /**
     * 执行 to Data Object 对应的业务操作。
     *
     * @param notice notice 参数
     * @return 处理结果
     */
    private NoticeDO toDataObject(Notice notice) {
        NoticeDO noticeDO = new NoticeDO();
        noticeDO.setId(notice.id());
        noticeDO.setTitle(notice.title());
        noticeDO.setType(notice.type());
        noticeDO.setContent(notice.content());
        noticeDO.setStatus(notice.status());
        return noticeDO;
    }
}
