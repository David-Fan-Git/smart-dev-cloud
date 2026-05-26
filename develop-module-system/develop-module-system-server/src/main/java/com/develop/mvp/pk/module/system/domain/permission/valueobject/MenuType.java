package com.develop.mvp.pk.module.system.domain.permission.valueobject;

import com.develop.mvp.pk.module.system.enums.permission.MenuTypeEnum;
import java.util.Objects;

/**
 * Menu Type 值对象。
 */
public final class MenuType {
    public static final MenuType DIR = new MenuType(MenuTypeEnum.DIR.getType());
    public static final MenuType MENU = new MenuType(MenuTypeEnum.MENU.getType());
    public static final MenuType BUTTON = new MenuType(MenuTypeEnum.BUTTON.getType());
    private final Integer code;
    /**
     * 创建 MenuType 实例。
     *
     * @param code code 参数
     */
    private MenuType(Integer code) { this.code = Objects.requireNonNull(code); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param code code 参数
     * @return 处理结果
     */
    public static MenuType of(Integer code) {
        if (MenuTypeEnum.DIR.getType().equals(code)) return DIR;
        if (MenuTypeEnum.MENU.getType().equals(code)) return MENU;
        if (MenuTypeEnum.BUTTON.getType().equals(code)) return BUTTON;
        throw new IllegalArgumentException("无效菜单类型: " + code);
    }
    /**
     * 判断 is Button 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isButton() { return code.equals(MenuTypeEnum.BUTTON.getType()); }
    /**
     * 判断 is Dir Or Menu 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isDirOrMenu() { return code.equals(MenuTypeEnum.DIR.getType()) || code.equals(MenuTypeEnum.MENU.getType()); }
    /**
     * 执行 code 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer code() { return code; }
    @Override public boolean equals(Object o) { return o instanceof MenuType m && code.equals(m.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
