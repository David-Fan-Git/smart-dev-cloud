package com.develop.mvp.pk.module.infra.application.file.result;

public record FilePresignedUrlResult(
        Long configId,
        String path,
        String uploadUrl,
        String url
) {
}
