package com.develop.mvp.pk.module.system.domain.permission;

import com.develop.mvp.pk.module.system.domain.permission.valueobject.*;

/**
 * Menu Factory 工厂。
 */
public final class MenuFactory {
    /**
     * 创建 MenuFactory 实例。
     */
    private MenuFactory() {}

    /**
     * 创建 create 对应的数据。
     *
     * @param id id 参数
     * @param name name 参数
     * @param permission permission 参数
     * @param type type 参数
     * @param sort sort 参数
     * @param parentId parentId 参数
     * @param path path 参数
     * @param icon icon 参数
     * @param component component 参数
     * @param componentName componentName 参数
     * @param status status 参数
     * @param visible visible 参数
     * @param keepAlive keepAlive 参数
     * @param alwaysShow alwaysShow 参数
     * @return 处理结果
     */
    public static Menu create(Long id, String name, String permission, Integer type, Integer sort,
                               Long parentId, String path, String icon, String component,
                               String componentName, Integer status, Boolean visible,
                               Boolean keepAlive, Boolean alwaysShow) {
        return new Menu(MenuId.of(id), MenuName.of(name), MenuPermission.of(permission),
                MenuType.of(type), sort, MenuId.of(parentId != null ? parentId : MenuId.ROOT_ID),
                path, icon, component, componentName, status, visible, keepAlive, alwaysShow);
    }

    /**
     * 执行 reconstitute 对应的业务操作。
     *
     * @param id id 参数
     * @param name name 参数
     * @param permission permission 参数
     * @param type type 参数
     * @param sort sort 参数
     * @param parentId parentId 参数
     * @param path path 参数
     * @param icon icon 参数
     * @param component component 参数
     * @param componentName componentName 参数
     * @param status status 参数
     * @param visible visible 参数
     * @param keepAlive keepAlive 参数
     * @param alwaysShow alwaysShow 参数
     * @return 处理结果
     */
    public static Menu reconstitute(Long id, String name, String permission, Integer type,
                                     Integer sort, Long parentId, String path, String icon,
                                     String component, String componentName, Integer status,
                                     Boolean visible, Boolean keepAlive, Boolean alwaysShow) {
        return new Menu(MenuId.of(id), MenuName.of(name),
                permission != null ? MenuPermission.of(permission) : MenuPermission.empty(),
                MenuType.of(type), sort,
                MenuId.of(parentId != null ? parentId : MenuId.ROOT_ID),
                path, icon, component, componentName, status, visible, keepAlive, alwaysShow);
    }
}
