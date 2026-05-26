package com.develop.mvp.pk.module.system.controller.admin.permission;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.menu.MenuRespVO;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.menu.MenuSaveVO;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.menu.MenuSimpleRespVO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.MenuUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Menu Controller 控制器。
 */
@Tag(name = "管理后台 - 菜单")
@RestController
@RequestMapping("/system/menu")
@Validated
public class MenuController {

    @Resource
    private MenuUseCase menuService;

    /**
     * 创建 create Menu 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建菜单")
    @PreAuthorize("@ss.hasPermission('system:menu:create')")
    public CommonResult<Long> createMenu(@Valid @RequestBody MenuSaveVO createReqVO) {
        Long menuId = menuService.createMenu(createReqVO);
        return success(menuId);
    }

    /**
     * 更新 update Menu 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     * @return 处理结果
     */
    @PutMapping("/update")
    @Operation(summary = "修改菜单")
    @PreAuthorize("@ss.hasPermission('system:menu:update')")
    public CommonResult<Boolean> updateMenu(@Valid @RequestBody MenuSaveVO updateReqVO) {
        menuService.updateMenu(updateReqVO);
        return success(true);
    }

    /**
     * 删除 delete Menu 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除菜单")
    @Parameter(name = "id", description = "菜单编号", required= true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:menu:delete')")
    public CommonResult<Boolean> deleteMenu(@RequestParam("id") Long id) {
        menuService.deleteMenu(id);
        return success(true);
    }

    /**
     * 删除 delete Menu List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除菜单")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('system:menu:delete')")
    public CommonResult<Boolean> deleteMenuList(@RequestParam("ids") List<Long> ids) {
        menuService.deleteMenuList(ids);
        return success(true);
    }

    /**
     * 查询 get Menu List 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @GetMapping("/list")
    @Operation(summary = "获取菜单列表", description = "用于【菜单管理】界面")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public CommonResult<List<MenuRespVO>> getMenuList(MenuListReqVO reqVO) {
        List<MenuDO> list = menuService.getMenuList(reqVO);
        list.sort(Comparator.comparing(MenuDO::getSort));
        return success(BeanUtils.toBean(list, MenuRespVO.class));
    }

    @GetMapping({"/list-all-simple", "simple-list"})
    @Operation(summary = "获取菜单精简信息列表",
            description = "只包含被开启的菜单，用于【角色分配菜单】功能的选项。在多租户的场景下，会只返回租户所在套餐有的菜单")
    /**
     * 查询 get Simple Menu List 对应的数据。
     *
     * @return 处理结果
     */
    public CommonResult<List<MenuSimpleRespVO>> getSimpleMenuList() {
        List<MenuDO> list = menuService.getMenuListByTenant(
                new MenuListReqVO().setStatus(CommonStatusEnum.ENABLE.getStatus()));
        list = menuService.filterDisableMenus(list);
        list.sort(Comparator.comparing(MenuDO::getSort));
        return success(BeanUtils.toBean(list, MenuSimpleRespVO.class));
    }

    /**
     * 查询 get Menu 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "获取菜单信息")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public CommonResult<MenuRespVO> getMenu(Long id) {
        MenuDO menu = menuService.getMenu(id);
        return success(BeanUtils.toBean(menu, MenuRespVO.class));
    }

}
