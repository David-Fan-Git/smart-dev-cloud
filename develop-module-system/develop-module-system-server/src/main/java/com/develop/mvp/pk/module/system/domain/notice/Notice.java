package com.develop.mvp.pk.module.system.domain.notice;

import java.util.Objects;

/**
 * Notice 领域模型。
 */
public final class Notice {

    private final Long id;
    private final String title;
    private final Integer type;
    private final String content;
    private final Integer status;

    /**
     * 创建 Notice 实例。
     *
     * @param id id 参数
     * @param title title 参数
     * @param type type 参数
     * @param content content 参数
     * @param status status 参数
     */
    private Notice(Long id, String title, Integer type, String content, Integer status) {
        this.id = id;
        this.title = Objects.requireNonNull(title);
        this.type = type;
        this.content = content;
        this.status = status;
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param id id 参数
     * @param title title 参数
     * @param type type 参数
     * @param content content 参数
     * @param status status 参数
     * @return 处理结果
     */
    public static Notice of(Long id, String title, Integer type, String content, Integer status) {
        return new Notice(id, title, type, content, status);
    }

    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long id() {
        return id;
    }

    /**
     * 执行 title 对应的业务操作。
     *
     * @return 处理结果
     */
    public String title() {
        return title;
    }

    /**
     * 执行 type 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer type() {
        return type;
    }

    /**
     * 执行 content 对应的业务操作。
     *
     * @return 处理结果
     */
    public String content() {
        return content;
    }

    /**
     * 执行 status 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer status() {
        return status;
    }
}
