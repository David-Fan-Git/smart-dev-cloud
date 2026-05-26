package com.develop.mvp.pk.module.infra.domain.file;

import com.develop.mvp.pk.module.infra.framework.file.core.client.local.LocalFileClientConfig;
import com.develop.mvp.pk.module.infra.infrastructure.file.FileConfigFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FileConfigTest {

    @Test
    void create_allowsTransientId() {
        FileConfig config = FileConfigFactory.create("本地存储", 10, false, localConfig(), "remark");

        assertNull(config.id());
    }

    @Test
    void create_preservesClientConfig() {
        LocalFileClientConfig clientConfig = localConfig();

        FileConfig config = FileConfigFactory.create("本地存储", 10, false, clientConfig, "remark");

        assertEquals(clientConfig, config.config());
    }

    private static LocalFileClientConfig localConfig() {
        LocalFileClientConfig config = new LocalFileClientConfig();
        config.setBasePath("/tmp/uploads");
        config.setDomain("https://static.example.com");
        return config;
    }
}
