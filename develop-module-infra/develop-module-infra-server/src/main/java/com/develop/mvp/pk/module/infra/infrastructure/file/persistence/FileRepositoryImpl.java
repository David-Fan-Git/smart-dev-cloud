package com.develop.mvp.pk.module.infra.infrastructure.file.persistence;

// DDD 角色：FileRepository 的 MyBatis 实现

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.dal.dataobject.file.FileDO;
import com.develop.mvp.pk.module.infra.dal.mysql.file.FileMapper;
import com.develop.mvp.pk.module.infra.domain.file.File;
import com.develop.mvp.pk.module.infra.domain.file.repository.FilePageQuery;
import com.develop.mvp.pk.module.infra.domain.file.repository.FileRepository;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileId;
import com.develop.mvp.pk.module.infra.infrastructure.file.FileFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FileRepositoryImpl implements FileRepository {

    private final FileMapper fileMapper;

    public FileRepositoryImpl(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    @Override
    public File save(File file) {
        FileDO fileDO = toDataObject(file);
        if (file.id() == null || fileMapper.selectById(file.id().value()) == null) {
            fileMapper.insert(fileDO);
            return toDomain(fileDO);
        }
        fileMapper.updateById(fileDO);
        return file;
    }

    @Override
    public void delete(FileId id) {
        fileMapper.deleteById(id.value());
    }

    @Override
    public void deleteByIds(Collection<FileId> ids) {
        fileMapper.deleteByIds(ids.stream().map(FileId::value).collect(Collectors.toList()));
    }

    @Override
    public File findById(FileId id) {
        FileDO fileDO = fileMapper.selectById(id.value());
        return fileDO != null ? toDomain(fileDO) : null;
    }

    @Override
    public PageResult<File> findPage(FilePageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.infra.controller.admin.file.vo.file.FilePageReqVO();
        reqVO.setPath(query.path());
        reqVO.setType(query.type());
        reqVO.setCreateTime(query.createTime());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<FileDO> doPage = fileMapper.selectPage(reqVO);
        List<File> files = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(files, doPage.getTotal());
    }

    @Override
    public List<File> findByIds(Collection<FileId> ids) {
        if (ids.isEmpty()) return Collections.emptyList();
        List<Long> rawIds = ids.stream().map(FileId::value).collect(Collectors.toList());
        return fileMapper.selectByIds(rawIds).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    private FileDO toDataObject(File file) {
        FileDO fileDO = new FileDO();
        fileDO.setId(file.id() != null ? file.id().value() : null);
        fileDO.setConfigId(file.configId() != null ? file.configId().value() : null);
        fileDO.setName(file.name());
        fileDO.setPath(file.path());
        fileDO.setUrl(file.url());
        fileDO.setType(file.type());
        fileDO.setSize(file.size());
        return fileDO;
    }

    private File toDomain(FileDO fileDO) {
        return FileFactory.reconstitute(
                fileDO.getId(),
                fileDO.getConfigId(),
                fileDO.getName(),
                fileDO.getPath(),
                fileDO.getUrl(),
                fileDO.getType(),
                fileDO.getSize()
        );
    }
}
