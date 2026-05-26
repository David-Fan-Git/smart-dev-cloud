package com.develop.mvp.pk.module.system.controller.admin.user;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
// Skill: AggregateRoot_User_Validation_Skill — 接口层 UserController
// DDD 角色：接口层，仅处理 HTTP 请求/响应，调用 UserApplicationService

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.UserUseCase;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.*;
import com.develop.mvp.pk.module.system.convert.user.UserConvert;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import com.develop.mvp.pk.module.system.domain.user.User;
import com.develop.mvp.pk.module.system.domain.user.repository.UserPageQuery;
import com.develop.mvp.pk.module.system.enums.common.SexEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * User Controller 控制器。
 */
@Tag(name = "管理后台 - 用户")
@RestController
@RequestMapping("/system/user")
@Validated
public class UserController {

    @Resource
    private UserUseCase userApplicationService;
    @Resource
    private AdminUserUseCase adminUserService; // 导入功能暂保留旧服务
    @Resource
    private DeptUseCase deptUseCase;

    /**
     * 创建 create User 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @PostMapping("/create")
    @Operation(summary = "新增用户")
    @PreAuthorize("@ss.hasPermission('system:user:create')")
    public CommonResult<Long> createUser(@Valid @RequestBody UserSaveReqVO reqVO) {
        Long id = userApplicationService.createUser(
                null, reqVO.getUsername(), reqVO.getPassword(), null,
                reqVO.getDeptId(), reqVO.getEmail(), reqVO.getMobile(),
                reqVO.getNickname(), reqVO.getAvatar(), reqVO.getSex(),
                reqVO.getRemark(), reqVO.getPostIds());
        return success(id);
    }

    /**
     * 更新 update User 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @PutMapping("update")
    @Operation(summary = "修改用户")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody UserSaveReqVO reqVO) {
        userApplicationService.updateUser(
                reqVO.getId(), reqVO.getUsername(), reqVO.getEmail(), reqVO.getMobile(),
                reqVO.getNickname(), reqVO.getAvatar(), reqVO.getSex(), reqVO.getRemark(),
                reqVO.getDeptId(), reqVO.getPostIds());
        return success(true);
    }

    /**
     * 删除 delete User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:user:delete')")
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
        userApplicationService.deleteUser(id);
        return success(true);
    }

    /**
     * 删除 delete User List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @Operation(summary = "批量删除用户")
    @PreAuthorize("@ss.hasPermission('system:user:delete')")
    public CommonResult<Boolean> deleteUserList(@RequestParam("ids") List<Long> ids) {
        userApplicationService.deleteUserList(ids);
        return success(true);
    }

    /**
     * 更新 update User Password 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @PutMapping("/update-password")
    @Operation(summary = "重置用户密码")
    @PreAuthorize("@ss.hasPermission('system:user:update-password')")
    public CommonResult<Boolean> updateUserPassword(@Valid @RequestBody UserUpdatePasswordReqVO reqVO) {
        userApplicationService.resetPassword(reqVO.getId(), reqVO.getPassword());
        return success(true);
    }

    /**
     * 更新 update User Status 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @PutMapping("/update-status")
    @Operation(summary = "修改用户状态")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> updateUserStatus(@Valid @RequestBody UserUpdateStatusReqVO reqVO) {
        userApplicationService.updateUserStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    /**
     * 查询 get User Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    @GetMapping("/page")
    @Operation(summary = "获得用户分页列表")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO pageReqVO) {
        Set<Long> deptIds = userApplicationService.getDeptCondition(pageReqVO.getDeptId());
        PageResult<User> pageResult = userApplicationService.getUserPage(
                UserPageQuery.builder()
                        .username(pageReqVO.getUsername()).mobile(pageReqVO.getMobile())
                        .status(pageReqVO.getStatus()).deptIds(deptIds)
                        .pageNo(pageReqVO.getPageNo()).pageSize(pageReqVO.getPageSize())
                        .build());
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        Map<Long, DeptDO> deptMap = deptUseCase.getDeptMap(
                convertList(pageResult.getList(), User::deptId));
        return success(new PageResult<>(
                UserConvert.INSTANCE.convertUserList(pageResult.getList(), deptMap),
                pageResult.getTotal()));
    }

    /**
     * 查询 get User List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @GetMapping("/list")
    @Operation(summary = "获得用户详情列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "[1024]")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<List<UserRespVO>> getUserList(@RequestParam("ids") List<Long> ids) {
        List<User> users = userApplicationService.getUserList(ids);
        if (CollUtil.isEmpty(users)) return success(Collections.emptyList());
        Map<Long, DeptDO> deptMap = deptUseCase.getDeptMap(convertSet(users, User::deptId));
        return success(UserConvert.INSTANCE.convertUserList(users, deptMap));
    }

    /**
     * 查询 get Simple User List 对应的数据。
     *
     * @return 处理结果
     */
    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "获取用户精简信息列表")
    public CommonResult<List<UserSimpleRespVO>> getSimpleUserList() {
        List<User> users = userApplicationService.getUserListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        Map<Long, DeptDO> deptMap = deptUseCase.getDeptMap(convertList(users, User::deptId));
        return success(UserConvert.INSTANCE.convertUserSimpleList(users, deptMap));
    }

    /**
     * 查询 get User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "获得用户详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<UserRespVO> getUser(@RequestParam("id") Long id) {
        User user = userApplicationService.getUser(id);
        if (user == null) return success(null);
        DeptDO dept = deptUseCase.getDept(user.deptId());
        return success(UserConvert.INSTANCE.convertUser(user, dept));
    }

    /**
     * 执行 export User List 对应的业务操作。
     *
     * @param exportReqVO exportReqVO 参数
     * @param response response 参数
     */
    @GetMapping("/export-excel")
    @Operation(summary = "导出用户")
    @PreAuthorize("@ss.hasPermission('system:user:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUserList(@Validated UserPageReqVO exportReqVO,
                               HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        Set<Long> deptIds = userApplicationService.getDeptCondition(exportReqVO.getDeptId());
        PageResult<User> pageResult = userApplicationService.getUserPage(
                UserPageQuery.builder()
                        .username(exportReqVO.getUsername()).mobile(exportReqVO.getMobile())
                        .status(exportReqVO.getStatus()).deptIds(deptIds)
                        .pageNo(exportReqVO.getPageNo()).pageSize(exportReqVO.getPageSize())
                        .build());
        Map<Long, DeptDO> deptMap = deptUseCase.getDeptMap(
                convertList(pageResult.getList(), User::deptId));
        ExcelUtils.write(response, "用户数据.xls", "数据", UserRespVO.class,
                UserConvert.INSTANCE.convertUserList(pageResult.getList(), deptMap));
    }

    /**
     * 执行 import Template 对应的业务操作。
     *
     * @param response response 参数
     */
    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入用户模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        List<UserImportExcelVO> list = Arrays.asList(
                UserImportExcelVO.builder().username("yunai").deptId(1L).email("yunai@iocoder.cn")
                        .mobile("15601691300").nickname("David")
                        .status(CommonStatusEnum.ENABLE.getStatus()).sex(SexEnum.MALE.getSex()).build(),
                UserImportExcelVO.builder().username("yuanma").deptId(2L).email("yuanma@iocoder.cn")
                        .mobile("15601701300").nickname("源码")
                        .status(CommonStatusEnum.DISABLE.getStatus()).sex(SexEnum.FEMALE.getSex()).build());
        ExcelUtils.write(response, "用户导入模板.xls", "用户列表", UserImportExcelVO.class, list);
    }

    /**
     * 执行 import Excel 对应的业务操作。
     *
     * @param file file 参数
     * @param updateSupport updateSupport 参数
     * @return 处理结果
     */
    @PostMapping("/import")
    @Operation(summary = "导入用户")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支持更新", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('system:user:import')")
    public CommonResult<UserImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false")
            Boolean updateSupport) throws Exception {
        List<UserImportExcelVO> list = ExcelUtils.read(file, UserImportExcelVO.class);
        return success(adminUserService.importUserList(list, updateSupport));
    }
}
