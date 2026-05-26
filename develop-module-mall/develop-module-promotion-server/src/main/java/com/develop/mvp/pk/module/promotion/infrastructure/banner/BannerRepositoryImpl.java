package com.develop.mvp.pk.module.promotion.infrastructure.banner;

// Skill: AggregateRoot_Banner_Validation_Skill — 仓储实现 BannerRepositoryImpl

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.promotion.dal.dataobject.banner.BannerDO;
import com.develop.mvp.pk.module.promotion.dal.mysql.banner.BannerMapper;
import com.develop.mvp.pk.module.promotion.domain.banner.Banner;
import com.develop.mvp.pk.module.promotion.domain.banner.BannerFactory;
import com.develop.mvp.pk.module.promotion.domain.banner.repository.BannerRepository;
import com.develop.mvp.pk.module.promotion.domain.banner.valueobject.BannerId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BannerRepositoryImpl implements BannerRepository {

    private final BannerMapper bannerMapper;

    public BannerRepositoryImpl(BannerMapper bannerMapper) {
        this.bannerMapper = bannerMapper;
    }

    @Override
    @Transactional
    public Banner save(Banner banner) {
        BannerDO bannerDO = toDataObject(banner);
        if (banner.id() == null || bannerMapper.selectById(banner.id().value()) == null) {
            bannerMapper.insert(bannerDO);
            return toDomain(bannerDO);
        }
        bannerMapper.updateById(bannerDO);
        return banner;
    }

    @Override
    @Transactional
    public void delete(BannerId id) {
        bannerMapper.deleteById(id.value());
    }

    @Override
    public Banner findById(BannerId id) {
        BannerDO bannerDO = bannerMapper.selectById(id.value());
        return bannerDO != null ? toDomain(bannerDO) : null;
    }

    @Override
    public List<Banner> findByStatus(Integer status) {
        return bannerMapper.selectList(BannerDO::getStatus, status).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Banner> findByPosition(Integer position) {
        return bannerMapper.selectBannerListByPosition(position).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Banner> findAll() {
        return bannerMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<Banner> findPage(String title, Integer pageNo, Integer pageSize) {
        var reqVO = new com.develop.mvp.pk.module.promotion.controller.admin.banner.vo.BannerPageReqVO();
        reqVO.setTitle(title);
        reqVO.setPageNo(pageNo);
        reqVO.setPageSize(pageSize);
        PageResult<BannerDO> doPage = bannerMapper.selectPage(reqVO);
        List<Banner> banners = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(banners, doPage.getTotal());
    }

    private BannerDO toDataObject(Banner banner) {
        BannerDO bannerDO = new BannerDO();
        bannerDO.setId(banner.id() != null ? banner.id().value() : null);
        bannerDO.setTitle(banner.title());
        bannerDO.setUrl(banner.url());
        bannerDO.setPicUrl(banner.picUrl());
        bannerDO.setSort(banner.sort());
        bannerDO.setStatus(banner.status());
        bannerDO.setPosition(banner.position());
        bannerDO.setMemo(banner.memo());
        return bannerDO;
    }

    private Banner toDomain(BannerDO bannerDO) {
        return BannerFactory.reconstitute(
                bannerDO.getId(), bannerDO.getTitle(), bannerDO.getUrl(),
                bannerDO.getPicUrl(), bannerDO.getSort(), bannerDO.getStatus(),
                bannerDO.getPosition(), bannerDO.getMemo());
    }
}
