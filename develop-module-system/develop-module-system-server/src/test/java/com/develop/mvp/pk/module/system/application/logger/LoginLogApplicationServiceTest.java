package com.develop.mvp.pk.module.system.application.logger;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.develop.mvp.pk.module.system.application.logger.dto.LoginLogDTO;
import com.develop.mvp.pk.module.system.application.logger.query.LoginLogPageQuery;
import com.develop.mvp.pk.module.system.application.logger.service.LoggerApplicationService;
import com.develop.mvp.pk.module.system.dal.dataobject.logger.LoginLogDO;
import com.develop.mvp.pk.module.system.dal.mysql.logger.LoginLogMapper;
import com.develop.mvp.pk.module.system.infrastructure.logger.persistence.LoginLogRepositoryImpl;
import com.develop.mvp.pk.module.system.infrastructure.logger.persistence.OperateLogRepositoryImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static com.develop.mvp.pk.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.randomPojo;
import static com.develop.mvp.pk.module.system.enums.logger.LoginResultEnum.CAPTCHA_CODE_ERROR;
import static com.develop.mvp.pk.module.system.enums.logger.LoginResultEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({LoggerApplicationService.class, LoginLogRepositoryImpl.class, OperateLogRepositoryImpl.class})
class LoginLogApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private LoggerApplicationService loggerApplicationService;

    @Resource
    private LoginLogMapper loginLogMapper;

    @Test
    void getLoginLogPage_filtersByIpUsernameStatusAndCreateTime() {
        LoginLogDO loginLogDO = randomPojo(LoginLogDO.class, o -> {
            o.setUserIp("192.168.199.16");
            o.setUsername("wang");
            o.setResult(SUCCESS.getResult());
            o.setCreateTime(buildTime(2021, 3, 6));
        });
        loginLogMapper.insert(loginLogDO);
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setResult(CAPTCHA_CODE_ERROR.getResult())));
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setUserIp("192.168.128.18")));
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setUsername("yunai")));
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setCreateTime(buildTime(2021, 2, 6))));
        LoginLogPageQuery reqVO = new LoginLogPageQuery();
        reqVO.setUsername("wang");
        reqVO.setUserIp("192.168.199");
        reqVO.setStatus(true);
        reqVO.setCreateTime(buildBetweenTime(2021, 3, 5, 2021, 3, 7));

        PageResult<LoginLogDTO> pageResult = loggerApplicationService.getLoginLogPage(reqVO);

        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(loginLogDO, pageResult.getList().get(0));
    }

    @Test
    void createLoginLog_insertsMappedLog() {
        LoginLogCreateReqDTO reqDTO = randomPojo(LoginLogCreateReqDTO.class);

        loggerApplicationService.createLoginLog(reqDTO);

        LoginLogDO loginLogDO = loginLogMapper.selectOne(null);
        assertPojoEquals(reqDTO, loginLogDO);
    }
}
