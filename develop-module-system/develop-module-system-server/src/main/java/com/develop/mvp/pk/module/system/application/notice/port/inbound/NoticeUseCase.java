package com.develop.mvp.pk.module.system.application.notice.port.inbound;

// DDD 角色：入站端口 — 定义 Notice 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.notice.vo.NoticePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.notice.NoticeDO;

import java.util.List;
/**
 * Notice 聚合的入站用例端口。
 */
public interface NoticeUseCase {

    /**
     * 创建 create Notice 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createNotice(NoticeSaveReqVO createReqVO);

    /**
     * 更新 update Notice 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateNotice(NoticeSaveReqVO updateReqVO);

    /**
     * 删除 delete Notice 对应的数据。
     *
     * @param id id 参数
     */
    void deleteNotice(Long id);

    /**
     * 删除 delete Notice List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteNoticeList(List<Long> ids);

    /**
     * 查询 get Notice Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    PageResult<NoticeDO> getNoticePage(NoticePageReqVO reqVO);

    /**
     * 查询 get Notice 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    NoticeDO getNotice(Long id);

    /**
     * 校验 validate Notice Exists 对应的业务规则。
     *
     * @param id id 参数
     */
    void validateNoticeExists(Long id);
}
