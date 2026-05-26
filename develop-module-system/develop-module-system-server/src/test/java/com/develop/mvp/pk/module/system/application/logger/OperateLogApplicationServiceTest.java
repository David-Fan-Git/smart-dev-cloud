package com.develop.mvp.pk.module.system.application.logger;

import com.develop.mvp.pk.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.framework.test.core.util.RandomUtils;
import com.develop.mvp.pk.module.system.application.logger.dto.OperateLogDTO;
import com.develop.mvp.pk.module.system.application.logger.query.OperateLogPageQuery;
import com.develop.mvp.pk.module.system.application.logger.service.LoggerApplicationService;
import com.develop.mvp.pk.module.system.dal.dataobject.logger.OperateLogDO;
import com.develop.mvp.pk.module.system.dal.mysql.logger.OperateLogMapper;
import com.develop.mvp.pk.module.system.infrastructure.logger.persistence.LoginLogRepositoryImpl;
import com.develop.mvp.pk.module.system.infrastructure.logger.persistence.OperateLogRepositoryImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static com.develop.mvp.pk.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertPojoEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({LoggerApplicationService.class, LoginLogRepositoryImpl.class, OperateLogRepositoryImpl.class})
class OperateLogApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private LoggerApplicationService loggerApplicationService;

    @Resource
    private OperateLogMapper operateLogMapper;

    @Test
    void createOperateLog_insertsMappedLog() {
        OperateLogCreateReqDTO reqVO = RandomUtils.randomPojo(OperateLogCreateReqDTO.class);

        loggerApplicationService.createOperateLog(reqVO);

        OperateLogDO operateLogDO = operateLogMapper.selectOne(null);
        assertPojoEquals(reqVO, operateLogDO);
    }

    @Test
    void getOperateLogPageByVo_filtersByAdminQueryFields() {
        OperateLogDO operateLogDO = RandomUtils.randomPojo(OperateLogDO.class, o -> {
            o.setUserId(2048L);
            o.setBizId(999L);
            o.setType("订单");
            o.setSubType("创建订单");
            o.setAction("修改编号为 1 的用户信息");
            o.setCreateTime(buildTime(2021, 3, 6));
        });
        operateLogMapper.insert(operateLogDO);
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setUserId(1024L)));
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setBizId(888L)));
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setType("退款")));
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setSubType("创建退款")));
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setAction("修改编号为 1 退款信息")));
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setCreateTime(buildTime(2021, 2, 6))));
        OperateLogPageQuery reqVO = new OperateLogPageQuery();
        reqVO.setUserId(2048L);
        reqVO.setBizId(999L);
        reqVO.setType("订");
        reqVO.setSubType("订单");
        reqVO.setAction("用户信息");
        reqVO.setCreateTime(buildBetweenTime(2021, 3, 5, 2021, 3, 7));

        PageResult<OperateLogDTO> pageResult = loggerApplicationService.getOperateLogPage(reqVO);

        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(operateLogDO, pageResult.getList().get(0));
    }

    @Test
    void getOperateLogPageByDto_filtersByRpcQueryFields() {
        OperateLogDO operateLogDO = RandomUtils.randomPojo(OperateLogDO.class, o -> {
            o.setUserId(2048L);
            o.setBizId(999L);
            o.setType("订单");
        });
        operateLogMapper.insert(operateLogDO);
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setUserId(1024L)));
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setBizId(888L)));
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setType("退款")));
        OperateLogPageQuery reqDTO = new OperateLogPageQuery();
        reqDTO.setUserId(2048L);
        reqDTO.setBizId(999L);
        reqDTO.setType("订单");
        reqDTO.setExactType(true);

        PageResult<OperateLogDTO> pageResult = loggerApplicationService.getOperateLogPage(reqDTO);

        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(operateLogDO, pageResult.getList().get(0));
    }
}
