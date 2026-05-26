package com.develop.mvp.pk.module.bpm.application.definition;
// DDD 角色：BPM流程分类应用服务 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.definition.BpmCategory;
import com.develop.mvp.pk.module.bpm.domain.definition.BpmCategoryFactory;
import com.develop.mvp.pk.module.bpm.domain.definition.event.CategoryDomainEvent;
import com.develop.mvp.pk.module.bpm.domain.definition.repository.BpmCategoryRepository;
import com.develop.mvp.pk.module.bpm.domain.definition.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.bpm.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
public class BpmCategoryApplicationService {
    private final BpmCategoryRepository repo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(String name, String code, Integer status, Integer sort) {
        CategoryName categoryName = CategoryName.of(name);
        CategoryCode categoryCode = CategoryCode.of(code);
        // 校验唯一
        if (repo.findByName(categoryName).isPresent()) throw exception(CATEGORY_NAME_DUPLICATE, name);
        if (repo.findByCode(categoryCode).isPresent()) throw exception(CATEGORY_CODE_DUPLICATE, code);
        BpmCategory c = BpmCategoryFactory.create(null, name, code, sort);
        repo.save(c);
        publishEvents(c);
        return c.id().value();
    }

    @Transactional
    public void update(Long id, String name, String code, Integer status, Integer sort) {
        BpmCategory existing = findExisting(id);
        // 校验唯一
        CategoryName categoryName = CategoryName.of(name);
        CategoryCode categoryCode = CategoryCode.of(code);
        Optional<BpmCategory> byName = repo.findByName(categoryName);
        if (byName.isPresent() && !byName.get().id().value().equals(id)) throw exception(CATEGORY_NAME_DUPLICATE, name);
        Optional<BpmCategory> byCode = repo.findByCode(categoryCode);
        if (byCode.isPresent() && !byCode.get().id().value().equals(id)) throw exception(CATEGORY_CODE_DUPLICATE, code);
        BpmCategory saved = BpmCategoryFactory.reconstitute(id, name, code, sort, status);
        repo.save(saved);
        publishEvents(saved);
    }

    @Transactional
    public void delete(Long id) {
        BpmCategory c = findExisting(id);
        // 校验是否被流程模型使用
        long count = repo.getModelCountByCategory(c.code().value());
        if (count > 0) throw exception(CATEGORY_DELETE_FAIL_MODEL_USED, c.name().value());
        c.markDeleted();
        repo.delete(c.id());
        publishEvents(c);
    }

    @Transactional
    public void updateSortBatch(List<Long> ids) {
        List<BpmCategory> categories = ids.stream()
                .map(id -> repo.findById(CategoryId.of(id)))
                .collect(Collectors.toList());
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i) == null) throw exception(CATEGORY_NOT_EXISTS);
        }
        for (int i = 0; i < ids.size(); i++) {
            BpmCategory c = BpmCategoryFactory.reconstitute(ids.get(i),
                    categories.get(i).name().value(), categories.get(i).code().value(),
                    i, categories.get(i).status().code());
            repo.save(c);
        }
    }

    public BpmCategory get(Long id) { return repo.findById(CategoryId.of(id)); }
    public List<BpmCategory> getByStatus(Integer status) { return repo.findByStatus(CategoryStatus.of(status)); }
    public List<BpmCategory> getAll() { return repo.findAll(); }
    public PageResult<BpmCategory> getPage(String name, String code, Integer status, Integer pageNo, Integer pageSize) {
        return repo.findPage(name, code, status, pageNo, pageSize);
    }

    private BpmCategory findExisting(Long id) {
        BpmCategory c = repo.findById(CategoryId.of(id));
        if (c == null) throw exception(CATEGORY_NOT_EXISTS);
        return c;
    }

    private void publishEvents(BpmCategory category) {
        for (CategoryDomainEvent event : category.pullEvents()) eventPublisher.publishEvent(event);
    }
}
