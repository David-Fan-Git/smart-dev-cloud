package com.develop.mvp.pk.module.system.application.module.dto;

import java.util.List;

public record SystemModuleDTO(
        String code,
        String name,
        String version,
        boolean enabled,
        List<String> dependencies,
        int order,
        String description,
        String state,
        String failureReason) {
}
