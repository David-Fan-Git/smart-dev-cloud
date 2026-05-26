package com.develop.mvp.pk.module.system.controller.admin.module.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "管理后台 - 系统模块响应 VO")
public class SystemModuleRespVO {

    private String code;
    private String name;
    private String version;
    private Boolean enabled;
    private List<String> dependencies;
    private Integer order;
    private String description;
    private String state;
    private String failureReason;

    public String getCode() {
        return code;
    }

    public SystemModuleRespVO setCode(String code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public SystemModuleRespVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public SystemModuleRespVO setVersion(String version) {
        this.version = version;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public SystemModuleRespVO setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public SystemModuleRespVO setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public Integer getOrder() {
        return order;
    }

    public SystemModuleRespVO setOrder(Integer order) {
        this.order = order;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SystemModuleRespVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getState() {
        return state;
    }

    public SystemModuleRespVO setState(String state) {
        this.state = state;
        return this;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public SystemModuleRespVO setFailureReason(String failureReason) {
        this.failureReason = failureReason;
        return this;
    }
}
