package com.develop.mvp.pk.module.infra.domain.file;

import com.develop.mvp.pk.module.infra.infrastructure.file.FileFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class FileTest {

    @Test
    void create_allowsTransientId() {
        File file = FileFactory.create(1L, "avatar.png", "20260523/avatar.png", "https://example.com/avatar.png", "image/png", 1024L);

        assertNull(file.id());
    }
}
