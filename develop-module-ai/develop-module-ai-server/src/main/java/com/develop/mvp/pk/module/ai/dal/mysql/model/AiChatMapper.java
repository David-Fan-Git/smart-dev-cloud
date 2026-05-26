package com.develop.mvp.pk.module.ai.dal.mysql.model;

import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.QueryWrapperX;
import com.develop.mvp.pk.module.ai.dal.dataobject.model.AiModelDO;
import com.develop.mvp.pk.module.ai.domain.model.repository.AiModelPageQuery;
import org.apache.ibatis.annotations.Mapper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * API 模型 Mapper
 *
 * @author David
 */
@Mapper
public interface AiChatMapper extends BaseMapperX<AiModelDO> {

    default AiModelDO selectFirstByStatus(Integer type, Integer status) {
        return selectOne(new QueryWrapperX<AiModelDO>()
                .eq("type", type)
                .eq("status", status)
                .limitN(1)
                .orderByAsc("sort"));
    }

    default PageResult<AiModelDO> selectPage(AiModelPageQuery query) {
        PageParam pageParam = new PageParam();
        if (query.pageNo() != null) {
            pageParam.setPageNo(query.pageNo());
        }
        if (query.pageSize() != null) {
            pageParam.setPageSize(query.pageSize());
        }
        return selectPage(pageParam, new LambdaQueryWrapperX<AiModelDO>()
                .likeIfPresent(AiModelDO::getName, query.name())
                .eqIfPresent(AiModelDO::getModel, query.model())
                .eqIfPresent(AiModelDO::getPlatform, query.platform())
                .eqIfPresent(AiModelDO::getType, query.type())
                .eqIfPresent(AiModelDO::getStatus, query.status())
                .orderByAsc(AiModelDO::getSort));
    }

    default List<AiModelDO> selectListByStatusAndType(Integer status, Integer type,
                                                      @Nullable String platform) {
        return selectList(new LambdaQueryWrapperX<AiModelDO>()
                .eq(AiModelDO::getStatus, status)
                .eq(AiModelDO::getType, type)
                .eqIfPresent(AiModelDO::getPlatform, platform)
                .orderByAsc(AiModelDO::getSort));
    }

    default AiModelDO selectByPlatformTypeAndModel(String platform, Integer type, String model) {
        return selectOne(new LambdaQueryWrapperX<AiModelDO>()
                .eq(AiModelDO::getPlatform, platform)
                .eq(AiModelDO::getType, type)
                .eq(AiModelDO::getModel, model));
    }

}
