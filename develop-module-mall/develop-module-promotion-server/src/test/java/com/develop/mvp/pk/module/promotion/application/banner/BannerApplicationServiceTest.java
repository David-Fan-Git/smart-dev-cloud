package com.develop.mvp.pk.module.promotion.application.banner;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.promotion.domain.banner.Banner;
import com.develop.mvp.pk.module.promotion.domain.banner.BannerFactory;
import com.develop.mvp.pk.module.promotion.domain.banner.repository.BannerRepository;
import com.develop.mvp.pk.module.promotion.domain.banner.valueobject.BannerId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BannerApplicationServiceTest {

    @Test
    void createBanner_returnsPersistedId() {
        BannerApplicationService applicationService = new BannerApplicationService(new StubBannerRepository(), event -> {});

        Long id = applicationService.createBanner(null, "首页", "https://example.com", "pic.png", 1, 0, 1, null);

        assertEquals(100L, id);
    }

    private static final class StubBannerRepository implements BannerRepository {
        @Override
        public Banner save(Banner banner) {
            return BannerFactory.reconstitute(100L, banner.title(), banner.url(), banner.picUrl(),
                    banner.sort(), banner.status(), banner.position(), banner.memo());
        }

        @Override public void delete(BannerId id) {}
        @Override public Banner findById(BannerId id) { return null; }
        @Override public List<Banner> findByStatus(Integer status) { return List.of(); }
        @Override public List<Banner> findByPosition(Integer position) { return List.of(); }
        @Override public List<Banner> findAll() { return List.of(); }
        @Override public PageResult<Banner> findPage(String title, Integer pageNo, Integer pageSize) { return PageResult.empty(); }
    }
}
