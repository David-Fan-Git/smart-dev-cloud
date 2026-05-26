package com.develop.mvp.pk.module.promotion.domain.banner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class BannerTest {

    @Test
    void create_allowsTransientId() {
        Banner banner = BannerFactory.create(null, "首页", "https://example.com", "pic.png", 1, 0, 1, null);

        assertNull(banner.id());
    }
}
