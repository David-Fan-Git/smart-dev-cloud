package com.develop.mvp.pk.module.system.application.notice.service;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.application.notice.port.inbound.NoticeUseCase;
import com.develop.mvp.pk.module.system.controller.admin.notice.vo.NoticePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.notice.NoticeDO;
import com.develop.mvp.pk.module.system.domain.notice.Notice;
import com.develop.mvp.pk.module.system.domain.notice.repository.NoticeRepository;
import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;

/**
 * Notice Application Service 应用服务。
 */
@RequiredArgsConstructor
public class NoticeApplicationService implements NoticeUseCase {

    private final NoticeRepository noticeRepository;

    /**
     * 创建 create Notice 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    @Transactional
    public Long createNotice(NoticeSaveReqVO createReqVO) {
        NoticeDO notice = noticeRepository.insert(toDomain(createReqVO));
        return notice.getId();
    }

    /**
     * 更新 update Notice 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    @Transactional
    public void updateNotice(NoticeSaveReqVO updateReqVO) {
        validateNoticeExists(updateReqVO.getId());
        noticeRepository.update(toDomain(updateReqVO));
    }

    /**
     * 删除 delete Notice 对应的数据。
     *
     * @param id id 参数
     */
    @Transactional
    public void deleteNotice(Long id) {
        validateNoticeExists(id);
        noticeRepository.delete(id);
    }

    /**
     * 删除 delete Notice List 对应的数据。
     *
     * @param ids ids 参数
     */
    @Transactional
    public void deleteNoticeList(List<Long> ids) {
        noticeRepository.deleteByIds(ids);
    }

    /**
     * 查询 get Notice Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    public PageResult<NoticeDO> getNoticePage(NoticePageReqVO reqVO) {
        return noticeRepository.findPage(reqVO);
    }

    /**
     * 查询 get Notice 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public NoticeDO getNotice(Long id) {
        return noticeRepository.findDoById(id);
    }

    /**
     * 校验 validate Notice Exists 对应的业务规则。
     *
     * @param id id 参数
     */
    @VisibleForTesting
    public void validateNoticeExists(Long id) {
        if (id == null) {
            return;
        }
        NoticeDO notice = noticeRepository.findDoById(id);
        if (notice == null) {
            throw exception(NOTICE_NOT_FOUND);
        }
    }

    /**
     * 执行 to Domain 对应的业务操作。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    private Notice toDomain(NoticeSaveReqVO reqVO) {
        return Notice.of(reqVO.getId(), reqVO.getTitle(), reqVO.getType(), reqVO.getContent(), reqVO.getStatus());
    }
}
