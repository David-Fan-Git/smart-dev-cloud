---
name: layered-architecture
description: Standard 4-layer architecture for Smart Cloud business modules — Controller, Service, DAL, API — with data flow, error handling, and production patterns
type: project
---

# Layered Architecture

## Overview

Smart Cloud 的每个业务模块遵循标准的四层架构：Controller 层（接口表现）、Service 层（业务逻辑）、DAL 层（数据访问）、API 层（远程调用契约）。各层职责清晰，通过 VO/DO/DTO 进行数据流转，配合统一异常体系处理错误边界。

## Data Request Flow

```
HTTP Request
    |
    v
Controller 层 (@RestController)
    |  接收请求参数 (SaveReqVO / PageReqVO)
    |  权限校验 (@PreAuthorize("@ss.hasPermission(...)"))
    |  参数校验 (@Valid / @Validated)
    |  调用 Service
    v
Service 层 (@Service)
    |  业务校验 (validate* 方法)
    |  对象转换 (VO -> DO, 通过 BeanUtils.toBean)
    |  调用 Mapper / Feign Api
    |  事务管理 (@Transactional 声明式)
    v
DAL 层 (Mapper)
    |  MyBatis Plus SQL 执行
    |  自动填充 (@TableField fill)
    |  逻辑删除过滤
    v
Database
```

Response flow:

```
Controller -> CommonResult<T> -> JSON (Jackson) -> HTTP Response
```

## 1. Controller Layer

Controller 层接收 HTTP 请求、校验参数、调用 Service、返回统一响应。

**典型模式:**

```java
@Tag(name = "管理后台 - 岗位")
@RestController
@RequestMapping("/system/post")
@Validated
public class PostController {

    @Resource
    private PostService postService;

    @PostMapping("/create")
    @Operation(summary = "创建岗位")
    @PreAuthorize("@ss.hasPermission('system:post:create')")
    public CommonResult<Long> createPost(@Valid @RequestBody PostSaveReqVO createReqVO) {
        Long postId = postService.createPost(createReqVO);
        return success(postId);
    }

    @PutMapping("/update")
    @Operation(summary = "修改岗位")
    @PreAuthorize("@ss.hasPermission('system:post:update')")
    public CommonResult<Boolean> updatePost(@Valid @RequestBody PostSaveReqVO updateReqVO) {
        postService.updatePost(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除岗位")
    @PreAuthorize("@ss.hasPermission('system:post:delete')")
    public CommonResult<Boolean> deletePost(@RequestParam("id") Long id) {
        postService.deletePost(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得岗位信息")
    @PreAuthorize("@ss.hasPermission('system:post:query')")
    public CommonResult<PostRespVO> getPost(@RequestParam("id") Long id) {
        PostDO post = postService.getPost(id);
        return success(BeanUtils.toBean(post, PostRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得岗位分页列表")
    @PreAuthorize("@ss.hasPermission('system:post:query')")
    public CommonResult<PageResult<PostRespVO>> getPostPage(@Validated PostPageReqVO pageReqVO) {
        PageResult<PostDO> pageResult = postService.getPostPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PostRespVO.class));
    }
}
```

**关键注解:**
- `@Tag` (Swagger) — 接口分组说明，格式如 `"管理后台 - XX"` 或 `"APP - XX"`
- `@RestController` — RESTful 控制器
- `@RequestMapping("/module/entity")` — 类级别路径
- `@Validated` — 类级别参数校验（Hibernate Validator）
- `@PreAuthorize("@ss.hasPermission('module:entity:action')")` — 权限控制（ss Bean 为 `SecurityFrameworkService`）
- `@Operation` — Swagger 接口说明
- 返回类型统一为 `CommonResult<T>`，通过静态 `success(data)` 快速构建

**VO 类命名规范:**
- `{Entity}SaveReqVO` — 创建/修改共用请求体（id 字段区分 create/update）
- `{Entity}PageReqVO` — 分页查询请求体（extends PageParam）
- `{Entity}RespVO` — 响应体
- `{Entity}SimpleRespVO` — 简化响应体（下拉选项等）

**错误处理:**
- Controller 本身不做业务异常处理，全部由 `GlobalExceptionHandler` 统一拦截
- 参数校验失败由 Spring 自动返回 400 错误码
- 权限不足由 Spring Security 自动返回 403

## 2. Service Layer

Service 层包含业务逻辑，采用 **Interface + Impl** 模式，接口和实现同包。

**Service Interface:**

```java
public interface PostService {
    Long createPost(PostSaveReqVO createReqVO);
    void updatePost(PostSaveReqVO updateReqVO);
    void deletePost(Long id);
    void deletePostList(List<Long> ids);
    PostDO getPost(Long id);
    List<PostDO> getPostList(Collection<Long> ids, Collection<Integer> statuses);
    PageResult<PostDO> getPostPage(PostPageReqVO reqVO);
    void validatePostList(Collection<Long> ids);
}
```

**Service Implementation:**

```java
@Service
@Validated
public class PostServiceImpl implements PostService {

    @Resource
    private PostMapper postMapper;

    @Override
    public Long createPost(PostSaveReqVO createReqVO) {
        // 1. 校验业务规则
        validatePostForCreateOrUpdate(null, createReqVO.getName(), createReqVO.getCode());
        // 2. VO -> DO 转换
        PostDO post = BeanUtils.toBean(createReqVO, PostDO.class);
        // 3. 执行插入
        postMapper.insert(post);
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(PostSaveReqVO updateReqVO) {
        validatePostForCreateOrUpdate(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getCode());
        PostDO updateObj = BeanUtils.toBean(updateReqVO, PostDO.class);
        postMapper.updateById(updateObj);
    }

    // 私有校验方法 — 集中管理业务校验规则
    private void validatePostForCreateOrUpdate(Long id, String name, String code) {
        validatePostExists(id);
        validatePostNameUnique(id, name);
        validatePostCodeUnique(id, code);
    }

    private void validatePostNameUnique(Long id, String name) {
        PostDO post = postMapper.selectByName(name);
        if (post == null) return;
        if (id == null || !post.getId().equals(id)) {
            throw exception(POST_NAME_DUPLICATE);
        }
    }

    private void validatePostExists(Long id) {
        if (id == null) return;
        if (postMapper.selectById(id) == null) {
            throw exception(POST_NOT_FOUND);
        }
    }
}
```

**关键模式:**
- `@Service` + `@Validated` — 声明 Service 并启用方法级参数校验
- `@Resource` — 注入 Mapper（优先使用 Java 标准注解，避免 @Autowired）
- `BeanUtils.toBean()` — Hutool 封装的属性拷贝工具，用于 VO/DO 转换（性能：单次 < 1ms，千条批量 < 50ms）
- `validate*` 私有方法 — 业务校验逻辑，失败时抛出 `ServiceException`（通过 `ServiceExceptionUtil.exception()` 创建）
- `@Transactional(rollbackFor = Exception.class)` — 事务注解，默认回滚异常类型
- 方法命名: `create*`, `update*`, `delete*`, `get*`, `get*List`, `get*Page`, `validate*`

**错误码使用:**
```java
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
throw exception(POST_NAME_DUPLICATE);                    // 无参
throw exception(POST_NOT_ENABLE, post.getName());        // 带格式化参数
```

### DAO 批量操作优化

```java
// 批量插入 — 使用 MyBatis Plus 的 saveBatch
@Service
public class ProductServiceImpl implements ProductService {
    @Resource
    private ProductMapper productMapper;

    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(List<ProductSaveReqVO> list) {
        List<ProductDO> products = BeanUtils.toBean(list, ProductDO.class);
        // 100 条一批插入，避免长事务和 OOM
        MyBatisBatchUtils.execute(products, 100, productMapper::insert);
    }
}
```

### Feign 调用错误处理

```java
@Resource
private RoleApi roleApi;

public void someMethod() {
    // 方式一: 手动检查
    CommonResult<Boolean> result = roleApi.validRoleList(ids);
    result.checkError(); // code != 0 时抛出 ServiceException

    // 方式二: 自动解包（code != 0 直接抛异常）
    List<DeptRespDTO> depts = deptApi.getDeptList(Arrays.asList(id)).getCheckedData();
}
```

**生产注意:**
- Feign 调用超时: 默认 connectTimeout=5000ms, readTimeout=10000ms，可在 feign.client.config 中按模块定制
- `result.getCheckedData()` 在 code 非 0 时抛出 `ServiceException`，不能再链式调用
- 批处理场景需要关注事务边界，避免长事务导致锁等待或连接池耗尽

## 3. DAL Layer (Data Access Layer)

包含三部分: Entity (DO), Mapper, Query Wrapper。

**Entity (DO):**

```java
@TableName("system_post")
@KeySequence("system_post_seq") // Oracle/PostgreSQL/Kingbase/DB2/H2 主键序列
@Data
@EqualsAndHashCode(callSuper = true)
public class PostDO extends BaseDO {
    @TableId
    private Long id;
    private String name;
    private String code;
    private Integer sort;
    private Integer status; // 关联 CommonStatusEnum
    private String remark;
}
```

**BaseDO 基类** (`com.develop.mvp.pk.framework.mybatis.core.dataobject`):

```java
public abstract class BaseDO implements Serializable, TransPojo {
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private String creator;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updater;
    @TableLogic
    private Boolean deleted;      // 0=未删除, 1=已删除

    public void clean() { /* 清空审计字段，用于单元测试 */ }
}
```

**TenantBaseDO（多租户实体基类，在 starter-biz-tenant 模块中）:**

```java
public abstract class TenantBaseDO extends BaseDO {
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
```

**BaseMapperX<T>** (`com.develop.mvp.pk.framework.mybatis.core.mapper`):

```java
@Mapper
public interface PostMapper extends BaseMapperX<PostDO> {

    default List<PostDO> selectList(Collection<Long> ids, Collection<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<PostDO>()
                .inIfPresent(PostDO::getId, ids)
                .inIfPresent(PostDO::getStatus, statuses));
    }

    default PageResult<PostDO> selectPage(PostPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PostDO>()
                .likeIfPresent(PostDO::getCode, reqVO.getCode())
                .likeIfPresent(PostDO::getName, reqVO.getName())
                .eqIfPresent(PostDO::getStatus, reqVO.getStatus())
                .orderByDesc(PostDO::getId));
    }

    default PostDO selectByName(String name) {
        return selectOne(PostDO::getName, name);
    }

    default PostDO selectByCode(String code) {
        return selectOne(PostDO::getCode, code);
    }
}
```

**LambdaQueryWrapperX** 增强方法:
```java
likeIfPresent(column, val)      // LIKE %val% (val 非空)
eqIfPresent(column, val)        // = val (val 非空)
inIfPresent(column, values)     // IN values (集合非空)
betweenIfPresent(column, start, end) // BETWEEN (两端非空)
orderByIfPresent(column, isAsc) // ORDER BY (条件化排序)
```

**N+1 查询预防原则:**
- 循环查询单条数据 → 改为 `selectByIds(Collection)` 批量查询
- 循环中调用 Feign → 改为批量调用后 local cache
- 多条 `selectOne` 串联 → 合并为一次 `selectList` + 内存 Map 转换

**Mapper 方法命名:**
- `selectByXxx` — 单条查询
- `selectList` — 多条查询
- `selectPage` — 分页查询
- `selectCount` — 计数查询
- `existXxx` — 是否存在查询
- `insert` / `updateById` / `deleteById` — 继承 BaseMapperX

## 4. API Layer (Feign RPC Contract)

API 层定义跨模块远程调用的接口契约，放在 `-api` 模块中，**不包含任何实现代码**。

**Feign 接口定义 (api 模块):**

```java
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 角色")
public interface RoleApi {
    String PREFIX = ApiConstants.PREFIX + "/role";

    @GetMapping(PREFIX + "/valid")
    @Operation(summary = "校验角色是否合法")
    CommonResult<Boolean> validRoleList(@RequestParam("ids") Collection<Long> ids);
}
```

**ApiConstants 模式:**

```java
public class ApiConstants {
    public static final String NAME = "system-server";          // spring.application.name
    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/system";
    public static final String VERSION = "1.0.0";
}
```

**API 实现 (server 模块):**

```java
@RestController // 同时作为 REST 端点暴露
public class RoleApiImpl implements RoleApi {
    @Resource
    private RoleService roleService;

    @Override
    public CommonResult<Boolean> validRoleList(Collection<Long> ids) {
        roleService.validateRoleList(ids);
        return success(true);
    }
}
```

**单体模式下 RPC 的降级:**
- OpenFeign 依赖被排除（通过 starter-rpc 的 exclusion）
- 所有 `DevelopXxxRpcAutoConfiguration` 通过 `spring.autoconfigure.exclude` 禁用
- 服务间调用退化为本地方法调用（同一 JVM）

## 5. Data Conversion System

支持两种对象转换方式:

**方式一: BeanUtils（推荐日常使用，基于 Hutool BeanUtil）**
```java
PostDO post = BeanUtils.toBean(createReqVO, PostDO.class);
List<PostRespVO> list = BeanUtils.toBean(postList, PostRespVO.class);
PageResult<PostRespVO> page = BeanUtils.toBean(pageResult, PostRespVO.class);
```

**方式二: MapStruct（复杂转换场景，编译期生成）**
```java
@Mapper
public interface UserConvert {
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    default UserProfileRespVO convert(AdminUserDO user, List<RoleDO> userRoles,
                                       DeptDO dept, List<PostDO> posts) {
        UserProfileRespVO userVO = BeanUtils.toBean(user, UserProfileRespVO.class);
        userVO.setRoles(BeanUtils.toBean(userRoles, RoleSimpleRespVO.class));
        userVO.setDept(BeanUtils.toBean(dept, DeptSimpleRespVO.class));
        userVO.setPosts(BeanUtils.toBean(posts, PostSimpleRespVO.class));
        return userVO;
    }
}
```

**性能选择:**
- 简单字段拷贝（无类型转换）→ `BeanUtils.toBean`（毫秒级，适用于大多数场景）
- 复杂字段映射/多源合并 → `MapStruct`（编译期生成无反射代码，性能最优，适用于高频转换）
- 不可混用两种方式同对象转换，保持项目一致性

## 6. VO Class Detailed Patterns

**SaveReqVO（创建/修改共用）:**
```java
@Schema(description = "管理后台 - 岗位创建/修改 Request VO")
@Data
public class PostSaveReqVO {
    @Schema(description = "岗位编号", example = "1024")
    private Long id;  // null = 创建, non-null = 修改

    @Schema(description = "岗位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小土豆")
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 50, message = "岗位名称长度不能超过 50 个字符")
    private String name;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull @InEnum(CommonStatusEnum.class)
    private Integer status;
}
```

**PageReqVO（分页查询）:**
```java
@Schema(description = "管理后台 - 岗位分页 Request VO")
@Data @EqualsAndHashCode(callSuper = true)
public class PostPageReqVO extends PageParam {
    @Schema(description = "岗位编码，模糊匹配", example = "develop")
    private String code;
    @Schema(description = "岗位名称，模糊匹配", example = "芋道")
    private String name;
    @Schema(description = "展示状态，参见 CommonStatusEnum", example = "1")
    private Integer status;
}
```

## 7. CommonResult Unified Response

```java
public class CommonResult<T> implements Serializable {
    private Integer code;  // 0 = success, non-zero = error
    private String msg;    // 错误提示
    private T data;        // 返回数据

    public static <T> CommonResult<T> success(T data) { ... }
    public static <T> CommonResult<T> error(Integer code, String message) { ... }
    public static <T> CommonResult<T> error(ErrorCode errorCode, Object... params) { ... }
    public static <T> CommonResult<T> error(CommonResult<?> result) { ... }
    public static <T> CommonResult<T> error(ServiceException serviceException) { ... }

    public boolean isSuccess() { ... }
    public boolean isError() { ... }
    public void checkError() throws ServiceException { ... }
    public T getCheckedData() { ... }
}
```

## Cross-Cutting Concerns

| Concern | Layer | Mechanism |
|---|---|---|
| Authentication | Controller + Filter | TokenAuthenticationFilter + @PreAuthorize |
| Authorization | Controller | @PreAuthorize("@ss.hasPermission(...)") |
| Transaction | Service | @Transactional(rollbackFor = Exception.class) |
| Audit | DAL | BaseDO 自动填充 (creator/updater/createTime/updateTime) |
| Logical Delete | DAL | @TableLogic on BaseDO.deleted |
| Multi-Tenant | DAL | TenantBaseDO + TenantLineInnerInterceptor |
| Data Permission | DAL | @DataPermission + DeptDataPermissionRule |
| Validation | Controller | @Valid/@Validated + Hibernate Validator |
| Exception | Controller | GlobalExceptionHandler 统一映射 |
| Cache | Service | @Cacheable(cacheNames = "key#30m") |
| Idempotent | Controller | @Idempotent + Redis SET NX |
| Rate Limit | Controller | @RateLimiter + Redisson RRateLimiter |
| Distributed Lock | Service | @Lock4j + Redisson |
| API Log | Filter | ApiAccessLogFilter (order = -103) |
| XSS | Filter | XssFilter (order = -102) |

## Error Handling Strategy

| Exception Type | HTTP Status | Code Prefix | Handling |
|---|---|---|---|
| ServiceException | 200 (业务错误) | 1_xxx_xxx_xxx | GlobalExceptionHandler 取 code/msg 返回 |
| ConstraintViolationException | 400 | 参数校验 | 提取字段错误信息 |
| MethodArgumentNotValidException | 400 | 参数校验 | 提取 BindingResult 错误 |
| NoHandlerFoundException | 404 | 资源不存 | 返回 NOT_FOUND |
| AccessDeniedException | 403 | 无权限 | Spring Security 处理 |
| AuthenticationException | 401 | 未认证 | authenticationEntryPoint |
| DataSourceException / TransactionException | 500 | 数据异常 | WARN 日志 + 500 |
| Exception (fallback) | 500 | 未知异常 | ERROR 日志 + 500 + TraceId |

## 注意事项

- `@Resource` 优先于 `@Autowired`，这是项目代码规范
- SaveReqVO 的 `id` 字段为 null 表示创建，非 null 表示修改，由 Service 层根据 id 判断
- Controller 层不直接操作 DO，必须通过 `BeanUtils.toBean` 转换为 VO 后返回
- `@PreAuthorize` 中的权限表达式格式为 `'module:entity:action'`（如 `'system:post:create'`）
- 校验异常通过 `ServiceExceptionUtil.exception(ErrorCode)` 抛出，由全局异常处理器统一捕获并格式化为 `CommonResult`
- 多租户实体必须继承 `TenantBaseDO`（而非 `BaseDO`），MyBatis Plus 拦截器自动注入 `tenant_id` 过滤条件
- Feign 接口在单体模式下被排除（排除 OpenFeign starter 依赖），RPC AutoConfiguration 通过 `application-local.yaml` 的 `spring.autoconfigure.exclude` 禁用
- N+1 查询是最常见的性能问题，审查代码时关注循环内的数据库和 Feign 调用
- `@Transactional` 的 `rollbackFor` 默认仅回滚 `RuntimeException`，建议显式声明 `rollbackFor = Exception.class`
