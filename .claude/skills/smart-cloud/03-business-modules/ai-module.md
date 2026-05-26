---
name: ai-module
description: AI/LLM integration module — Spring AI multi-model chat, image generation (Midjourney/StabilityAI), music (Suno), writing, mind maps, MCP protocol, vector stores, knowledge base
type: project
---

# develop-module-ai

## 概述

AI/LLM 集成模块。基于 **Spring AI 1.1.5** 框架，提供多模型对话、图片生成、音乐生成、AI 写作、思维导图、知识库、MCP 协议、向量存储等能力。

- **包路径**: `com.develop.mvp.pk.module.ai`
- **服务名**: `ai-server`
- **错误码区间**: [1-022-000-000 ~ 1-023-000-000)
- **多租户**: 是（Entity 继承 `TenantBaseDO`）

## 核心功能与 Controller 清单

### 1. 模型平台管理
| Controller | 路由 | 方法 |
|---|---|---|
| `AiModelController` | `/ai/model` | 模型 CRUD + `/page` |
| `AiApiKeyController` | `/ai/api-key` | API Key 管理（加密存储） |
| `AiChatRoleController` | `/ai/chat-role` | 聊天角色/提示词模板 |

**支持的平台**（Spring AI 原生接入）: OpenAI、Azure OpenAI、Anthropic Claude、Ollama、百度千帆(Qianfan)、智谱 AI(ZhipuAI)、通义千问(DashScope)、DeepSeek、Minimax、Moonshot

**自定义平台**（非 Spring AI 原生）: Google Gemini、字节豆包(Doubao)、腾讯混元(Hunyuan)、SiliconFlow、讯飞星火(Xinghuo)、百川(Baichuan)

### 2. 对话 (Chat)
| Controller | 路由 | 方法 |
|---|---|---|
| `AiChatConversationController` | `/ai/chat-conversation` | 会话 CRUD + `/page`、`/delete-my-conversation` |
| `AiChatMessageController` | `/ai/chat-message` | `/page`(历史消息)、`/send`(发送，支持 SSE 流式)、`/delete`、`/delete-by-conversation`(清空会话) |

**核心**: `AiModelFactoryImpl` 根据平台配置创建 ChatModel/ImageModel。

### 3. 图片生成
| Controller | 路由 | 方法 |
|---|---|---|
| `AiImageController` | `/ai/image` | `/create`、`/page` |

**实现**: Midjourney（自定义 `MidjourneyApi` 对接 Imagine/Upscale/Variation/Describe）、Stability AI（Spring AI 适配）、自定义平台文生图接口。

### 4. 音乐生成
| Controller | 路由 | 方法 |
|---|---|---|
| `AiMusicController` | `/ai/music` | `/create`、`/page` |

**实现**: 自定义 `SunoApi` 对接 Suno API（歌词驱动、风格驱动、参考音频）。

### 5. AI 写作
| Controller | 路由 | 方法 |
|---|---|---|
| `AiWriteController` | `/ai/write` | `/create`、`/page` |

### 6. 思维导图
| Controller | 路由 | 方法 |
|---|---|---|
| `AiMindMapController` | `/ai/mind-map` | `/create`、`/page` |

### 7. 知识库 (Knowledge Base)
| Controller | 路由 | 方法 |
|---|---|---|
| `AiKnowledgeController` | `/ai/knowledge` | 知识库 CRUD + `/page` |
| `AiKnowledgeDocumentController` | `/ai/knowledge-document` | 文档上传/分段/向量化 |
| `AiKnowledgeSegmentController` | `/ai/knowledge-segment` | 文档分段管理 |

支持 PDF、Word、Markdown、TXT 格式，分段策略包括 Token 数、段落、自定义分隔符。

### 8. 工具 (Tool)
| Controller | 路由 | 方法 |
|---|---|---|
| `AiToolController` | `/ai/tool` | AI 工具/Function Calling 管理 |

### 9. 工作流
| Controller | 路由 | 方法 |
|---|---|---|
| `AiWorkflowController` | `/ai/workflow` | AI 工作流定义和管理 |

### 10. 向量存储
- **Redis Stack**: 向量搜索
- **Qdrant**: 通过 `spring-ai-qdrant-spring-boot-starter` 集成
- **Milvus**: 通过 `spring-ai-milvus-spring-boot-starter` 集成

### 11. MCP 协议
- **MCP Server / Client**: Model Context Protocol 实现，标准化 AI 与外部工具交互

## 数据库表

| 表 | 说明 |
|---|---|
| `ai_model` | AI 模型配置 |
| `ai_api_key` | API Key（加密存储） |
| `ai_chat_role` | 聊天角色/提示词 |
| `ai_chat_conversation` | 对话会话 |
| `ai_chat_message` | 聊天消息 |
| `ai_image` | 图片生成记录 |
| `ai_music` | 音乐生成记录 |
| `ai_write` | 写作记录 |
| `ai_mind_map` | 思维导图 |
| `ai_knowledge` | 知识库 |
| `ai_knowledge_document` | 知识文档 |
| `ai_knowledge_segment` | 文档分段 |
| `ai_tool` | AI 工具定义 |
| `ai_workflow` | AI 工作流 |
| `ai_workflow_log` | 工作流执行日志 |

## 关键 Services

| Service | 职责 |
|---|---|
| `AiModelFactoryImpl` | 模型工厂，根据平台配置创建 ChatModel/ImageModel |
| `AiChatService` | 对话服务（流式 SSE 与非流式） |
| `AiImageService` | 图片生成服务 |
| `AiMusicService` | 音乐生成服务（Suno） |
| `AiWriteService` | 写作服务 |
| `AiMindMapService` | 思维导图生成 |
| `AiKnowledgeService` | 知识库管理 |
| `AiKnowledgeDocumentService` | 文档处理（分段、向量化） |

### 自定义 API 客户端
- `MidjourneyApi` — Midjourney API 适配
- `SunoApi` — Suno AI API 适配
- `SiliconFlowImageApi` — SiliconFlow 图片 API
- `XunFeiPptApi` — 讯飞星火 PPT API

## 依赖的 Starter

- `develop-spring-boot-starter-web`
- `develop-spring-boot-starter-security`
- `develop-spring-boot-starter-mybatis`
- `develop-spring-boot-starter-redis` (含向量搜索)
- `spring-ai-openai-spring-boot-starter`
- `spring-ai-anthropic-spring-boot-starter`
- `spring-ai-ollama-spring-boot-starter`
- `spring-ai-qianfan-spring-boot-starter` (百度千帆)
- `spring-ai-zhipuai-spring-boot-starter` (智谱)
- `spring-ai-dashscope-spring-boot-starter` (通义千问)
- `spring-ai-qdrant-spring-boot-starter` (向量库)
- `spring-ai-milvus-spring-boot-starter` (向量库)

## 关键点

- 基于 Spring AI 1.1.5 统一接入多模型
- 自定义平台（Gemini、豆包、混元等）通过自定义适配器实现
- 流式对话通过 SSE (Server-Sent Events) 推送
- 知识库：文档 → 分段 → 向量化 → 语义检索
- 错误码段 [1-022-000-000 ~ 1-023-000-000)
- 无独立的 Feign API 暴露给其他模块（纯管理后台功能）
