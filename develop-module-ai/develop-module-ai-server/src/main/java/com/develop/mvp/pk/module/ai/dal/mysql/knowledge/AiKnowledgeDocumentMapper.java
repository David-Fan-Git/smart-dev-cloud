package com.develop.mvp.pk.module.ai.dal.mysql.knowledge;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.ai.controller.admin.knowledge.vo.document.AiKnowledgeDocumentPageReqVO;
import com.develop.mvp.pk.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI 知识库文档 Mapper
 *
 * @author David
 */
@Mapper
public interface AiKnowledgeDocumentMapper extends BaseMapperX<AiKnowledgeDocumentDO> {

    default PageResult<AiKnowledgeDocumentDO> selectPage(AiKnowledgeDocumentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiKnowledgeDocumentDO>()
                .eqIfPresent(AiKnowledgeDocumentDO::getKnowledgeId, reqVO.getKnowledgeId())
                .likeIfPresent(AiKnowledgeDocumentDO::getName, reqVO.getName())
                .orderByDesc(AiKnowledgeDocumentDO::getId));
    }

    default void updateRetrievalCountIncr(Collection<Long> ids) {
        update(new LambdaUpdateWrapper<AiKnowledgeDocumentDO>()
                .setSql(" retrieval_count = retrieval_count + 1")
                .in(AiKnowledgeDocumentDO::getId, ids));
    }

    default List<AiKnowledgeDocumentDO> selectListByStatus(Integer status) {
        return selectList(AiKnowledgeDocumentDO::getStatus, status);
    }

    default List<AiKnowledgeDocumentDO> selectListByKnowledgeId(Long knowledgeId) {
        return selectList(AiKnowledgeDocumentDO::getKnowledgeId, knowledgeId);
    }

}
