---
name: member-module
description: Member center — registration/login, levels/groups/tags, addresses, points/credits, sign-in, experience values, social login
type: project
---

# develop-module-member

## 概述

会员中心模块。管理 C 端用户（会员），包含注册登录、会员等级、分组、标签、收货地址、积分、签到、经验值等功能。

- **包路径**: `com.develop.mvp.pk.module.member`
- **服务名**: `member-server`
- **错误码区间**: [1-004-000-000 ~ 1-005-000-000)
- **数据库表前缀**: `member_`
- **多租户**: 否（会员是跨租户的 C 端用户，多数 Entity 继承 `BaseDO`，不继承 `TenantBaseDO`）

## 核心功能与 Controller 清单

### 1. 会员认证（移动端）
| Controller | 路由 | 方法 |
|---|---|---|
| `AppAuthController` | `/member/auth`(app) | `/login`(密码登录)、`/login-mobile`(手机验证码)、`/login-wechat-mini-app`(小程序)、`/register`、`/reset-password`、`/logout`、`/refresh-token` |

支持：手机号 + 验证码、微信小程序一键登录、第三方社交账号登录。

### 2. 会员管理
| Controller | 路由 | 方法 |
|---|---|---|
| `MemberUserController` | `/member/user` | 管理端：CRUD + `/page` |
| `AppMemberUserController` | `/member/user`(app) | 移动端：个人信息查看/修改 |
| `AppSocialUserController` | `/member/social-user`(app) | 社交账号绑定/解绑 |

### 3. 会员等级
| Controller | 路由 | 方法 |
|---|---|---|
| `MemberLevelController` | `/member/level` | 等级配置 CRUD |
| `MemberLevelRecordController` | `/member/level-record` | 等级变更记录 |
| `MemberExperienceRecordController` | `/member/experience-record` | 经验值变动记录 |
| `AppMemberLevelController` | `/member/level`(app) | 我的等级信息 |

### 4. 会员分组
| Controller | 路由 | 方法 |
|---|---|---|
| `MemberGroupController` | `/member/group` | 分组配置 CRUD（按条件自动分组） |

### 5. 会员标签
| Controller | 路由 | 方法 |
|---|---|---|
| `MemberTagController` | `/member/tag` | 标签管理 CRUD |

### 6. 收货地址
| Controller | 路由 | 方法 |
|---|---|---|
| `AppAddressController` | `/member/address`(app) | 新增、删除、默认地址设置 |
| `AddressController` | `/member/address` | 管理端地址查询 |

### 7. 积分管理
| Controller | 路由 | 方法 |
|---|---|---|
| `MemberPointRecordController` | `/member/point-record` | 管理端积分记录查询 |
| `AppMemberPointRecordController` | `/member/point-record`(app) | 我的积分 |

### 8. 签到
| Controller | 路由 | 方法 |
|---|---|---|
| `MemberSignInConfigController` | `/member/sign-in-config` | 签到规则配置（连续签到奖励） |
| `MemberSignInRecordController` | `/member/sign-in-record` | 签到记录查询 |
| `AppMemberSignInRecordController` | `/member/sign-in-record`(app) | 用户签到操作 |

### 9. 配置
| Controller | 路由 | 方法 |
|---|---|---|
| `MemberConfigController` | `/member/config` | 会员中心全局配置 |

## 数据库表

| 表 | 说明 | 关键字段 |
|---|---|---|
| `member_user` | 会员用户 | mobile, nickname, avatar, level_id, point, experience |
| `member_level` | 会员等级 | name, experience(门槛), discount_percent(权益) |
| `member_level_record` | 等级变更 | user_id, level_id, reason |
| `member_experience_record` | 经验值变动 | user_id, experience, source |
| `member_group` | 会员分组 | name, rule_config |
| `member_tag` | 会员标签 | name, color |
| `member_address` | 收货地址 | user_id, region_id, detail, defaulted |
| `member_point_record` | 积分记录 | user_id, point, source, biz_id |
| `member_sign_in_config` | 签到配置 | day(连续天数), point(奖励积分数) |
| `member_sign_in_record` | 签到记录 | user_id, sign_in_date, point |
| `member_config` | 会员配置 | register_point, sign_in_point_enabled |

## 关键 Services

| Service | 职责 |
|---|---|
| `MemberUserService` / `MemberUserServiceImpl` | 会员核心业务 |
| `MemberAuthService` / `MemberAuthServiceImpl` | 认证逻辑（多端登录） |
| `MemberLevelService` / `MemberLevelServiceImpl` | 等级管理（升级/降级） |
| `MemberPointRecordService` / `MemberPointRecordServiceImpl` | 积分变动 |
| `MemberSignInRecordService` / `MemberSignInRecordServiceImpl` | 签到 |
| `MemberAddressService` / `MemberAddressServiceImpl` | 收货地址 |

## Feign API 清单

| API | 方法 | 用途 |
|---|---|---|
| `MemberUserApi` | `getUser`, `getUserList`, `validateUserList` | 会员用户查询 |
| `MemberLevelApi` | `getLevel` | 会员等级查询 |
| `MemberConfigApi` | `getConfig` | 会员配置查询 |
| `MemberPointApi` | `getPoint`, `addPoint`, `reducePoint` | 积分操作 |
| `MemberAddressApi` | `getAddress`, `getDefaultAddress` | 地址查询 |

## 依赖的 Starter

- `develop-spring-boot-starter-web`
- `develop-spring-boot-starter-security`（会员端 JWT Token）
- `develop-spring-boot-starter-mybatis`
- `develop-spring-boot-starter-redis`

## 关键点

- 会员是跨租户 C 端用户，多数 Entity 继承 `BaseDO` 而非 `TenantBaseDO`
- 会员认证与系统用户认证分离（会员端 JWT Token vs 管理端 Token）
- 积分变动通过 MQ 消息解耦（订单完成时异步加积分）
- 错误码段 [1-004-000-000 ~ 1-005-000-000)
