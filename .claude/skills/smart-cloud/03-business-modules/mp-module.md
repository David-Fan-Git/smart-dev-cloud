---
name: mp-module
description: WeChat Official Account platform — account management, template messages, custom menus, auto-reply, fans/tags management, drafts/publishing, enterprise WeChat SCRM
type: project
---

# develop-module-mp

## 概述

微信公众号管理模块。提供公众号的完整管理能力，包括账号绑定、模板消息、自定义菜单、自动回复、粉丝管理、素材管理、图文草稿、发布管理。基于 `weixin-java-mp` 4.8.2。

- **包路径**: `com.develop.mvp.pk.module.mp`
- **服务名**: `mp-server`
- **错误码区间**: [1-006-000-000 ~ 1-007-000-000)
- **数据库表前缀**: `mp_`
- **多租户**: 否（Entity 继承 `BaseDO`）

## 核心功能与 Controller 清单

### 1. 公众号账号管理
| Controller | 路由 | 方法 |
|---|---|---|
| `MpAccountController` | `/mp/account` | 账号 CRUD + `/page`、`/generate-url`(生成回调 URL) |

### 2. 模板消息
| Controller | 路由 | 方法 |
|---|---|---|
| `MpTemplateController` | `/mp/template` | 模板同步（从微信拉取）、模板消息发送 |

### 3. 自定义菜单
| Controller | 路由 | 方法 |
|---|---|---|
| `MpMenuController` | `/mp/menu` | 菜单创建/发布/删除/同步 |

### 4. 自动回复
| Controller | 路由 | 方法 |
|---|---|---|
| `MpAutoReplyController` | `/mp/auto-reply` | 关注回复、关键词回复、默认回复 |

### 5. 粉丝管理
| Controller | 路由 | 方法 |
|---|---|---|
| `MpUserController` | `/mp/user` | 粉丝列表同步 + `/page` |

### 6. 标签管理
| Controller | 路由 | 方法 |
|---|---|---|
| `MpTagController` | `/mp/tag` | 标签 CRUD + 同步 |

### 7. 素材管理
| Controller | 路由 | 方法 |
|---|---|---|
| `MpMaterialController` | `/mp/material` | 永久素材上传（图片、语音、视频、缩略图） |

### 8. 图文草稿
| Controller | 路由 | 方法 |
|---|---|---|
| `MpDraftController` | `/mp/draft` | 图文草稿 CRUD + `/page`、`/create-draft-in-wechat`(同步到微信) |
| `MpFreePublishController` | `/mp/free-publish` | 发布管理 |

### 9. 消息管理
| Controller | 路由 | 方法 |
|---|---|---|
| `MpMessageController` | `/mp/message` | 粉丝消息记录 |
| `MpOpenController` | 回调端点 | 微信服务器回调（事件/消息推送） |

### 10. 统计分析
| Controller | 路由 | 方法 |
|---|---|---|
| `MpStatisticsController` | `/mp/statistics` | 用户增减、消息量、接口调用量 |

### 11. 企业微信 SCRM
| Controller | 路由 | 方法 |
|---|---|---|
| 企微相关 Controller | — | 企业微信 SCRM 集成 |

## 数据库表

| 表 | 说明 |
|---|---|
| `mp_account` | 公众号账号 |
| `mp_template` | 模板消息 |
| `mp_menu` | 自定义菜单 |
| `mp_auto_reply` | 自动回复规则 |
| `mp_user` | 粉丝用户 |
| `mp_tag` | 粉丝标签 |
| `mp_material` | 永久素材 |
| `mp_draft` | 图文草稿 |
| `mp_draft_article` | 草稿文章 |
| `mp_free_publish` | 发布记录 |
| `mp_message` | 粉丝消息 |
| `mp_statistics` | 统计数据 |

## 关键 Services

| Service | 职责 |
|---|---|
| `MpAccountService` / `MpAccountServiceImpl` | 公众号账号管理 |
| `MpMessageService` / `MpMessageServiceImpl` | 消息处理（微信回调） |
| `MpMenuService` / `MpMenuServiceImpl` | 自定义菜单 |
| `MpAutoReplyService` / `MpAutoReplyServiceImpl` | 自动回复 |
| `MpUserService` / `MpUserServiceImpl` | 粉丝管理 |
| `MpMaterialService` / `MpMaterialServiceImpl` | 素材 |
| `MpDraftService` / `MpDraftServiceImpl` | 图文草稿 |

## 依赖的 Starter

- `develop-spring-boot-starter-web`
- `develop-spring-boot-starter-security`
- `develop-spring-boot-starter-mybatis`
- `develop-spring-boot-starter-redis`
- `weixin-java-mp` 4.8.2 — 微信公众号 SDK
- `weixin-java-cp` 4.8.2 — 企业微信 SDK

## 关键点

- Entity 继承 `BaseDO`（非 `TenantBaseDO`）— 公众号配置不隔离租户
- 错误码段 [1-006-000-000 ~ 1-007-000-000)
- `MpOpenController` 处理微信服务器回调（消息/事件推送）
- 基于 `weixin-java-mp` 封装微信 API 调用
- 无独立的 Feign API 暴露给其他业务模块（纯管理后台）
