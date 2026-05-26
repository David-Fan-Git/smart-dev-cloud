package com.develop.mvp.pk.module.system.application.notice;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.system.application.notice.service.NoticeApplicationService;
import com.develop.mvp.pk.module.system.controller.admin.notice.vo.NoticePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.notice.NoticeDO;
import com.develop.mvp.pk.module.system.dal.mysql.notice.NoticeMapper;
import com.develop.mvp.pk.module.system.infrastructure.notice.persistence.NoticeRepositoryImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static com.develop.mvp.pk.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.randomLongId;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.randomPojo;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

@Import({NoticeApplicationService.class, NoticeRepositoryImpl.class})
class NoticeApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private NoticeApplicationService noticeApplicationService;

    @Resource
    private NoticeMapper noticeMapper;

    @Test
    void getNoticePage_success() {
        NoticeDO dbNotice = randomPojo(NoticeDO.class, o -> {
            o.setTitle("尼古拉斯赵四来啦！");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        noticeMapper.insert(dbNotice);
        noticeMapper.insert(cloneIgnoreId(dbNotice, o -> o.setTitle("尼古拉斯凯奇也来啦！")));
        noticeMapper.insert(cloneIgnoreId(dbNotice, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        NoticePageReqVO reqVO = new NoticePageReqVO();
        reqVO.setTitle("尼古拉斯赵四来啦！");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        PageResult<NoticeDO> pageResult = noticeApplicationService.getNoticePage(reqVO);

        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbNotice, pageResult.getList().get(0));
    }

    @Test
    void getNotice_success() {
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        NoticeDO notice = noticeApplicationService.getNotice(dbNotice.getId());

        assertNotNull(notice);
        assertPojoEquals(dbNotice, notice);
    }

    @Test
    void createNotice_success() {
        NoticeSaveReqVO reqVO = randomPojo(NoticeSaveReqVO.class).setId(null);

        Long noticeId = noticeApplicationService.createNotice(reqVO);

        assertNotNull(noticeId);
        NoticeDO notice = noticeMapper.selectById(noticeId);
        assertPojoEquals(reqVO, notice, "id");
    }

    @Test
    void updateNotice_success() {
        NoticeDO dbNoticeDO = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNoticeDO);
        NoticeSaveReqVO reqVO = randomPojo(NoticeSaveReqVO.class, o -> o.setId(dbNoticeDO.getId()));

        noticeApplicationService.updateNotice(reqVO);

        NoticeDO notice = noticeMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, notice);
    }

    @Test
    void deleteNotice_success() {
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        noticeApplicationService.deleteNotice(dbNotice.getId());

        assertNull(noticeMapper.selectById(dbNotice.getId()));
    }

    @Test
    void validateNoticeExists_success() {
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        noticeApplicationService.validateNoticeExists(dbNotice.getId());
    }

    @Test
    void validateNoticeExists_noExists() {
        assertServiceException(() -> noticeApplicationService.validateNoticeExists(randomLongId()), NOTICE_NOT_FOUND);
    }
}
