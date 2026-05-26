---
name: FileClientFactory
description: Factory and strategy for creating file storage clients (Local/DB/S3/FTP/SFTP)
type: project
---

# FileClientFactory

## 功能定位

FileClientFactory 是文件存储客户端的工厂接口，位于 `develop-module-infra` 的 `framework.file.core.client` 包下。它**管理文件客户端的注册中心和生命周期**，支持 5 种文件存储后端：

1. **LocalFileClient** -- 本地文件系统
2. **DBFileClient** -- 数据库 BLOB 存储
3. **S3FileClient** -- AWS S3 兼容对象存储（MinIO、阿里云 OSS、腾讯云 COS）
4. **FtpFileClient** -- FTP 协议
5. **SftpFileClient** -- SFTP 协议

实际实现类 `FileClientFactoryImpl` 内部维护一个 `ConcurrentMap<Long, AbstractFileClient<?>>` 作为客户端注册中心，支持运行时的动态创建和热更新（无需重启服务）。

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Factory Method** | 根据 storage 类型反射创建对应的客户端实例 | `createFileClient()` 使用 `ReflectUtil.newInstance()` |
| **Strategy** | 统一接口，多种存储后端实现 | `FileClient` 接口 + 5 种实现 |
| **Registry** | 维护 configId -> FileClient 的映射 | `ConcurrentMap<Long, AbstractFileClient<?>> clients` |
| **Template Method** | 基类定义初始化/刷新模板 | `AbstractFileClient.init()` / `AbstractFileClient.refresh()` |
| **Flyweight** | 客户端实例复用 | 同一 configId 的客户端只创建一次 |

## 核心逻辑流程

### 创建/更新客户端

```
createOrUpdateFileClient(configId, storage, config)
  |
  +-- 1. 查询 clients 中是否已有该 configId 的客户端
  |    |
  |    +-- [不存在] 创建新客户端:
  |    |    +-- createFileClient(configId, storage, config)
  |    |    |    +-- FileStorageEnum.getByStorage(storage) 获取枚举
  |    |    |    +-- ReflectUtil.newInstance(storageEnum.getClientClass(), configId, config)
  |    |    |        反射创建: new LocalFileClient(configId, config)
  |    |    |                     / new DBFileClient(configId, config)
  |    |    |                     / new S3FileClient(configId, config)
  |    |    |                     / new FtpFileClient(configId, config)
  |    |    |                     / new SftpFileClient(configId, config)
  |    |    +-- client.init() 初始化（如创建本地目录、FTP 连接测试）
  |    |    +-- clients.put(client.getId(), client)
  |    |
  |    +-- [已存在] 热更新配置:
  |         +-- client.refresh(config) 更新客户端内部配置
  |         +-- 无需重建客户端
```

### 获取客户端

```
getFileClient(configId)
  |
  +-- clients.get(configId) 从 Map 获取
  +-- 如果不存在: log.error("配置编号(xxx)找不到客户端")
  +-- return FileClient (可能为 null)
```

### 文件上传流程 (通过 FileClient)

```
FileClient.upload(content, path, type) -> String (URL)
  |
  +-- LocalFileClient: 写入本地 basePath + path, 返回文件 URL
  +-- DBFileClient:    INSERT BLOB 到数据库, 返回 ID-based URL
  +-- S3FileClient:    PutObject 到 S3 bucket, 返回对象 URL
  +-- FtpFileClient:   FTP put 到远程文件
  +-- SftpFileClient:  SFTP put 到远程文件
```

## 关键代码剖析

```java
// 工厂接口
public interface FileClientFactory {
    FileClient getFileClient(Long configId);
    <Config extends FileClientConfig> void createOrUpdateFileClient(
            Long configId, Integer storage, Config config);
}

// 工厂实现
@Slf4j
public class FileClientFactoryImpl implements FileClientFactory {

    private final ConcurrentMap<Long, AbstractFileClient<?>> clients = new ConcurrentHashMap<>();

    @Override
    public <Config extends FileClientConfig> void createOrUpdateFileClient(
            Long configId, Integer storage, Config config) {
        AbstractFileClient<Config> client = (AbstractFileClient<Config>) clients.get(configId);
        if (client == null) {
            // 创建新客户端
            client = this.createFileClient(configId, storage, config);
            client.init();                          // 初始化
            clients.put(client.getId(), client);    // 注册
        } else {
            client.refresh(config);                 // 热更新
        }
    }

    @SuppressWarnings("unchecked")
    private <Config extends FileClientConfig> AbstractFileClient<Config> createFileClient(
            Long configId, Integer storage, Config config) {
        FileStorageEnum storageEnum = FileStorageEnum.getByStorage(storage);
        Assert.notNull(storageEnum, String.format("文件配置(%s) 为空", storageEnum));
        // 反射创建客户端实例
        return (AbstractFileClient<Config>) ReflectUtil.newInstance(
                storageEnum.getClientClass(), configId, config);
    }
}
```

### 反射创建机制剖析

反射创建的关键在于 `FileStorageEnum` 枚举中定义的映射关系：

```java
public enum FileStorageEnum {
    LOCAL(10, LocalFileClient.class),
    DB(20, DBFileClient.class),
    S3(100, S3FileClient.class),
    FTP(110, FtpFileClient.class),
    SFTP(120, SftpFileClient.class);

    private final Integer storage;
    private final Class<? extends AbstractFileClient> clientClass;
}
```

当新增一种存储类型时，需要：
1. 在 `FileStorageEnum` 中添加新枚举值，指定 `storage` 编码和 `clientClass`
2. 创建新的 `AbstractFileClient` 子类，实现 `FileClient` 接口
3. 创建对应的 `FileClientConfig` 配置类

整个过程不需要修改工厂代码，符合**开闭原则**。

### 客户端生命周期

```
[创建]  AbstractFileClient<Config>
  |-- 构造方法: 保存 configId 和 config
  |
  |-- init(): 客户端专属初始化
  |     LocalFileClient: 创建 basePath 目录
  |     S3FileClient:   创建 bucket
  |     FtpFileClient:  建立初始连接
  |
  |-- [使用] upload/download/delete
  |
  |-- refresh(config): 热更新配置
  |     更新内部配置
  |     重新连接 (如 FTP/SFTP)
  |
  |-- [销毁] 随应用生命周期，或手动 remove
```

### 客户端选择机制 (FileController 中)

```java
FileClient client = fileClientFactory.getFileClient(configId);
if (client == null) {
    throw exception(FILE_CLIENT_NOT_EXISTS);
}
String url = client.upload(content, path, type);
```

`configId` 从请求参数中获取，允许前端在上传时指定使用哪个存储配置。系统默认配置在 `FileConfigDO` 中管理。

## 调用链

```
[Upstream]
  |-- FileController (REST API)
  |     |-- POST /file/upload -> FileService.uploadFile()
  |     |   -> fileClientFactory.getFileClient(configId)
  |     |   -> client.upload(content, path, type)
  |     |
  |     |-- FileConfigController (存储配置管理)
  |           |-- PUT /file-config/update
  |               -> FileConfigService.updateFileConfig()
  |                  -> fileClientFactory.createOrUpdateFileClient(configId, storage, config)
  |--
  |-- FileService (业务层)
        -> getFileClient(configId).getFileContent(p path)
        -> 文件下载/预览

[Downstream]
  FileClient 接口 (5 种实现):
    LocalFileClient  -> 本地文件系统 IO
    DBFileClient     -> MySQL BLOB 字段读写
    S3FileClient     -> AWS SDK (HTTP)
    FtpFileClient    -> Apache Commons Net (FTP)
    SftpFileClient   -> JSch (SFTP)
```

## 配置与条件

| 存储类型 | storage 编码 | 客户端类 | 所需配置 |
|----------|-------------|----------|----------|
| Local | 10 | LocalFileClient | basePath (文件存储根路径) |
| DB | 20 | DBFileClient | 无额外配置 (复用数据源) |
| S3 | 100 | S3FileClient | endpoint, bucket, accessKey, secretKey, region |
| FTP | 110 | FtpFileClient | host, port, username, password, basePath |
| SFTP | 120 | SftpFileClient | host, port, username, password, basePath, privateKey |

客户端工厂 Bean 在 `DevelopFileAutoConfiguration` 中注册:
```java
@Configuration(proxyBeanMethods = false)
public class DevelopFileAutoConfiguration {
    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }
}
```

## 生产级关注点

### 1. 线程安全

`ConcurrentHashMap` 保证 `clients` 的并发安全：
- `get()` 和 `put()` 不会阻塞
- `createOrUpdateFileClient()` 在 `client == null` 的分支中，存在"先检查后写入"的竞态。第一次创建时，如果两个线程同时进入 null 分支，可能创建两个客户端实例。但最终 `put()` 会覆盖，后写入的生效。对于文件客户端这种配置级对象，启动后不会频繁变更，此竞态影响极小。

### 2. 热更新

`refresh()` 方法使得配置变更后无需重启服务：
- 文件服务配置管理界面修改配置 -> 调用 `createOrUpdateFileClient()` -> 已存在的客户端直接 `refresh()`
- 这意味着文件客户端可以做到"配置即改即用"

### 3. 延迟初始化

客户端在首次 `createOrUpdateFileClient` 时创建，不会在启动时预创建所有客户端。这意味着：
- 未使用到的存储类型不会占用连接资源
- 首次上传操作可能稍慢（因为需要初始化连接）

### 4. 错误处理

- `getFileClient()` 返回 null 时，调用方（FileController）抛出 `FILE_CLIENT_NOT_EXISTS` 业务异常
- 文件上传失败时由具体的 Client 实现抛出异常，由 `GlobalExceptionHandler` 统一处理
- `createFileClient()` 中 `ReflectUtil.newInstance()` 如果反射失败会抛出运行时异常

### 5. 连接管理

不同客户端的连接管理策略不同：
- **LocalFileClient**: 无连接，直接文件 IO，最稳定
- **DBFileClient**: 复用 MyBatis 数据源，无额外连接
- **S3FileClient**: 每次上传使用 HTTP 请求，SDK 管理连接池
- **FtpFileClient/SftpFileClient**: 每次操作建立连接，简单场景下使用

### 6. 扩展新存储

新增存储类型需要：
1. 定义 Config 类实现 `FileClientConfig`
2. 定义 Client 类继承 `AbstractFileClient<Config>`
3. 在 `FileStorageEnum` 中添加枚举值
4. 无需修改工厂代码

### 7. 监控建议

- 可对每种存储类型的上传/下载成功率做监控
- S3 客户端的请求延迟受网络影响较大
- 本地文件系统需关注磁盘空间告警
