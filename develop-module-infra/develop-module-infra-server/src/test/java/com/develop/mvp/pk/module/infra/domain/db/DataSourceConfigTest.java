package com.develop.mvp.pk.module.infra.domain.db;

import com.develop.mvp.pk.module.infra.infrastructure.db.DataSourceConfigFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class DataSourceConfigTest {

    @Test
    void create_allowsTransientId() {
        DataSourceConfig config = DataSourceConfigFactory.create("slave", "jdbc:mysql://127.0.0.1:3306/slave", "root", "123456");

        assertNull(config.id());
    }
}
