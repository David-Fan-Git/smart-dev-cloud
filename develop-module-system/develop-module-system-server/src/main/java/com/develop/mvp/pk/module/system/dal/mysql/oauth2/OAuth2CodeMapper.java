package com.develop.mvp.pk.module.system.dal.mysql.oauth2;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OAuth2 Code Mapper 持久化 Mapper。
 */
@Mapper
public interface OAuth2CodeMapper extends BaseMapperX<OAuth2CodeDO> {

    /**
     * 查询 select By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    default OAuth2CodeDO selectByCode(String code) {
        return selectOne(OAuth2CodeDO::getCode, code);
    }

}
