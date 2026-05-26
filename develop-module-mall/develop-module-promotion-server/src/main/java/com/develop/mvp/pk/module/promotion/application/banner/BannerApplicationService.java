package com.develop.mvp.pk.module.promotion.application.banner;

// Skill: AggregateRoot_Banner_Validation_Skill — 应用服务 BannerApplicationService

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.promotion.domain.banner.Banner;
import com.develop.mvp.pk.module.promotion.domain.banner.BannerFactory;
import com.develop.mvp.pk.module.promotion.domain.banner.repository.BannerRepository;
import com.develop.mvp.pk.module.promotion.domain.banner.valueobject.BannerId;
import com.develop.mvp.pk.module.promotion.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.promotion.enums.ErrorCodeConstants.*;

@Service
public class BannerApplicationService {

    private final BannerRepository bannerRepository;
    private final DomainEventPublisher eventPublisher;

    public BannerApplicationService(BannerRepository bannerRepository, DomainEventPublisher eventPublisher) {
        this.bannerRepository = bannerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createBanner(Long id, String title, String url, String picUrl,
                              Integer sort, Integer status, Integer position, String memo) {
        Banner banner = BannerFactory.create(id, title, url, picUrl, sort, status, position, memo);
        banner = bannerRepository.save(banner);
        publishEvents(banner);
        return banner.id().value();
    }

    @Transactional
    public void updateBanner(Long id, String title, String url, String picUrl,
                              Integer sort, Integer position, String memo) {
        Banner banner = findExistingBanner(BannerId.of(id));
        banner.updateProfile(title, url, picUrl, sort, position, memo);
        bannerRepository.save(banner);
        publishEvents(banner);
    }

    @Transactional
    public void deleteBanner(Long id) {
        Banner banner = findExistingBanner(BannerId.of(id));
        bannerRepository.delete(banner.id());
        publishEvents(banner);
    }

    @Transactional
    public void updateBannerStatus(Long id, Integer status) {
        Banner banner = findExistingBanner(BannerId.of(id));
        if (com.develop.mvp.pk.framework.common.enums.CommonStatusEnum.ENABLE.getStatus().equals(status)) {
            banner.enable();
        } else {
            banner.disable();
        }
        bannerRepository.save(banner);
        publishEvents(banner);
    }

    // ── 查询 ──

    public Banner getBanner(Long id) {
        return bannerRepository.findById(BannerId.of(id));
    }

    public List<Banner> getBannerListByStatus(Integer status) {
        return bannerRepository.findByStatus(status);
    }

    public List<Banner> getBannerListByPosition(Integer position) {
        return bannerRepository.findByPosition(position);
    }

    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    public PageResult<Banner> getBannerPage(String title, Integer pageNo, Integer pageSize) {
        return bannerRepository.findPage(title, pageNo, pageSize);
    }

    // ── 私有方法 ──

    private Banner findExistingBanner(BannerId id) {
        Banner banner = bannerRepository.findById(id);
        if (banner == null) throw exception(BANNER_NOT_EXISTS);
        return banner;
    }

    private void publishEvents(Banner banner) {
        for (var event : banner.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
