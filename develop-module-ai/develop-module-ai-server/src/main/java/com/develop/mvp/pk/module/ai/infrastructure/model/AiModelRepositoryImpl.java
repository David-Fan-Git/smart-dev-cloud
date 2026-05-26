package com.develop.mvp.pk.module.ai.infrastructure.model;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.ai.dal.dataobject.model.AiModelDO;
import com.develop.mvp.pk.module.ai.dal.mysql.model.AiChatMapper;
import com.develop.mvp.pk.module.ai.domain.model.AiModel;
import com.develop.mvp.pk.module.ai.domain.model.AiModelFactory;
import com.develop.mvp.pk.module.ai.domain.model.repository.AiModelPageQuery;
import com.develop.mvp.pk.module.ai.domain.model.repository.AiModelRepository;
import com.develop.mvp.pk.module.ai.domain.model.valueobject.AiModelId;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AiModelRepositoryImpl implements AiModelRepository {

    private final AiChatMapper aiChatMapper;

    public AiModelRepositoryImpl(AiChatMapper aiChatMapper) {
        this.aiChatMapper = aiChatMapper;
    }

    @Override
    public AiModel save(AiModel model) {
        AiModelDO modelDO = toDataObject(model);
        if (model.id() != null && aiChatMapper.selectById(model.id()) != null) {
            aiChatMapper.updateById(modelDO);
        } else {
            aiChatMapper.insert(modelDO);
            return toDomain(modelDO);
        }
        return model;
    }

    @Override
    public void delete(AiModelId id) {
        aiChatMapper.deleteById(id.value());
    }

    @Override
    public AiModel findById(AiModelId id) {
        AiModelDO modelDO = aiChatMapper.selectById(id.value());
        return modelDO != null ? toDomain(modelDO) : null;
    }

    @Override
    public List<AiModel> findAll() {
        return aiChatMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<AiModel> findByStatusAndType(Integer status, Integer type, @Nullable String platform) {
        return aiChatMapper.selectListByStatusAndType(status, type, platform).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public AiModel findFirstByStatus(Integer type, Integer status) {
        AiModelDO modelDO = aiChatMapper.selectFirstByStatus(type, status);
        return modelDO != null ? toDomain(modelDO) : null;
    }

    @Override
    public PageResult<AiModel> findPage(AiModelPageQuery query) {
        PageResult<AiModelDO> doPage = aiChatMapper.selectPage(query);
        List<AiModel> models = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(models, doPage.getTotal());
    }

    private AiModelDO toDataObject(AiModel model) {
        AiModelDO modelDO = new AiModelDO();
        if (model.id() != null) modelDO.setId(model.id());
        modelDO.setKeyId(model.keyId());
        modelDO.setName(model.name());
        modelDO.setModel(model.model());
        modelDO.setPlatform(model.platform());
        modelDO.setType(model.type());
        modelDO.setSort(model.sort());
        modelDO.setStatus(model.status() != null ? model.status().code() : null);
        modelDO.setTemperature(model.temperature());
        modelDO.setMaxTokens(model.maxTokens());
        modelDO.setMaxContexts(model.maxContexts());
        return modelDO;
    }

    private AiModel toDomain(AiModelDO modelDO) {
        return AiModelFactory.reconstitute(
                modelDO.getId(), modelDO.getKeyId(), modelDO.getName(), modelDO.getModel(),
                modelDO.getPlatform(), modelDO.getType(), modelDO.getSort(),
                modelDO.getStatus(), modelDO.getTemperature(),
                modelDO.getMaxTokens(), modelDO.getMaxContexts()
        );
    }
}
