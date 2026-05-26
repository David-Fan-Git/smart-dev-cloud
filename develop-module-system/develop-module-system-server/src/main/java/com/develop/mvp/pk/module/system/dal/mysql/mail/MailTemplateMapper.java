package com.develop.mvp.pk.module.system.dal.mysql.mail;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.mail.MailTemplateDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mail Template Mapper 持久化 Mapper。
 */
@Mapper
public interface MailTemplateMapper extends BaseMapperX<MailTemplateDO> {

    /**
     * 查询 select Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    default PageResult<MailTemplateDO> selectPage(MailTemplatePageReqVO pageReqVO){
        return selectPage(pageReqVO , new LambdaQueryWrapperX<MailTemplateDO>()
                .eqIfPresent(MailTemplateDO::getStatus, pageReqVO.getStatus())
                .likeIfPresent(MailTemplateDO::getCode, pageReqVO.getCode())
                .likeIfPresent(MailTemplateDO::getName, pageReqVO.getName())
                .eqIfPresent(MailTemplateDO::getAccountId, pageReqVO.getAccountId())
                .betweenIfPresent(MailTemplateDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(MailTemplateDO::getId));
    }

    /**
     * 查询 select Count By Account Id 对应的数据。
     *
     * @param accountId accountId 参数
     * @return 处理结果
     */
    default Long selectCountByAccountId(Long accountId) {
        return selectCount(MailTemplateDO::getAccountId, accountId);
    }

    /**
     * 查询 select By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    default MailTemplateDO selectByCode(String code) {
        return selectOne(MailTemplateDO::getCode, code);
    }

}
