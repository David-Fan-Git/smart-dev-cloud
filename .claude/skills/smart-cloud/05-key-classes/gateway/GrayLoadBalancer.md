---
name: GrayLoadBalancer
description: Spring Cloud LoadBalancer implementation for version/tag-based gray routing
type: project
---

# GrayLoadBalancer

## 功能定位

GrayLoadBalancer 是灰度发布的负载均衡器实现，位于 `develop-gateway` 的 `filter.grey` 包下。它实现 `ReactorServiceInstanceLoadBalancer` 接口，**在 Gateway 网关层根据请求头中的版本/标签信息，将流量路由到具有匹配元数据标签的服务实例**。

核心职责：
- **版本路由**：根据 `header[version]` 匹配服务实例的 `metadata["version"]`
- **标签隔离**：根据 `header[tag]` 匹配服务实例的 `metadata["tag"]`，支持环境隔离
- **权重选择**：使用 Nacos 的权重随机算法，在匹配的实例中选择
- **降级保护**：无匹配实例时自动降级到全量实例

它与 Spring Cloud LoadBalancer 的关系：
- 替换默认的 `RoundRobinLoadBalancer`，在 `ServiceInstanceListSupplier` 拉取实例列表后，由 `GrayLoadBalancer` 做筛选和选择

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Strategy** | 实现 `ReactorServiceInstanceLoadBalancer` 接口 | 可替换默认负载均衡策略 |
| **Load Balancer** | 基于请求元数据选择目标服务实例 | `choose(request) -> Response<ServiceInstance>` |
| **Filter Chain** | 多级过滤流水线 | 版本过滤 -> tag 过滤 -> Nacos 权重选择 |
| **Fallback** | 过滤结果为空时降级到全量 | 当 version/tag 过滤后无实例时使用原始列表 |
| **Decorator** | 扩展 Nacos 权重功能 | `NacosBalancer.getHostByRandomWeight3()` |

## 核心逻辑流程

```
choose(request)
  |
  +-- 1. 从 request.getContext() 获取 HttpHeaders
  |    ((RequestDataContext) request.getContext()).getClientRequest().getHeaders()
  |    注意: 必须强转为 RequestDataContext
  |
  +-- 2. 获取 ServiceInstanceListSupplier
  |    serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new)
  |
  +-- 3. supplier.get(request).next() 获取实例列表
  |    (Reactive, Mono<List<ServiceInstance>>)
  |
  +-- 4. getInstanceResponse(list, headers)
       |
       +-- 4.0 空列表保护
       |    CollUtil.isEmpty(instances) -> return new EmptyResponse()
       |
       +-- 4.1 第一级: version 过滤
       |    headers.getFirst("version")
       |    |
       |    +-- [version 为空] -> 使用所有实例 (不过滤)
       |    |
       |    +-- [version 非空] -> 筛选 metadata["version"] == version 的实例
       |         |
       |         +-- [有匹配] -> chooseInstances = 匹配的实例
       |         +-- [无匹配] -> log.warn + 使用全量实例 (降级)
       |
       +-- 4.2 第二级: tag 过滤
       |    EnvUtils.getTag(headers)
       |    |
       |    +-- [tag 为空]
       |    |   -> 过滤掉有 tag 的实例 (防止流量打到 feature 环境)
       |    |   -> 如果全被过滤: 使用全部 (降级)
       |    |
       |    +-- [tag 非空]
       |       -> 筛选 metadata["tag"] == tag 的实例
       |       -> 如果无匹配: 使用全部 (降级)
       |
       +-- 4.3 第三级: Nacos 权重随机选择
            NacosBalancer.getHostByRandomWeight3(chooseInstances)
            -> return new DefaultResponse(instance)
```

### 版本 + Tag 组合路由场景举例

| 请求 header[version] | 请求 header[tag] | 匹配行为 |
|---------------------|------------------|----------|
| 无 | 无 | 过滤掉有 tag 的节点, 剩余节点权重选择 |
| `v1.0` | 无 | 筛选 version=v1.0 且无 tag 的节点 |
| 无 | `feat-pay` | 筛选 tag=feat-pay 的节点 |
| `v2.0` | `feat-order` | 筛选 version=v2.0 AND tag=feat-order 的节点 |

## 关键代码剖析

```java
@RequiredArgsConstructor
@Slf4j
public class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final String VERSION = "version";

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        // 从 RequestDataContext 提取 HTTP Headers
        HttpHeaders headers = ((RequestDataContext) request.getContext()).getClientRequest().getHeaders();

        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next()
                .map(list -> getInstanceResponse(list, headers));
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, HttpHeaders headers) {
        if (CollUtil.isEmpty(instances)) {
            log.warn("[getInstanceResponse][serviceId({}) 服务实例列表为空]", serviceId);
            return new EmptyResponse();
        }

        // === 第一级: version 过滤 ===
        String version = headers.getFirst(VERSION);
        List<ServiceInstance> chooseInstances;
        if (StrUtil.isEmpty(version)) {
            chooseInstances = instances;  // 不过滤
        } else {
            chooseInstances = CollectionUtils.filterList(instances,
                    instance -> version.equals(instance.getMetadata().get(VERSION)));
            if (CollUtil.isEmpty(chooseInstances)) {
                log.warn("[getInstanceResponse][serviceId({}) 没有满足版本({})的实例，降级]", serviceId, version);
                chooseInstances = instances;  // 降级!
            }
        }

        // === 第二级: tag 过滤 ===
        chooseInstances = filterTagServiceInstances(chooseInstances, headers);

        // === 第三级: Nacos 权重随机选择 ===
        return new DefaultResponse(NacosBalancer.getHostByRandomWeight3(chooseInstances));
    }

    private List<ServiceInstance> filterTagServiceInstances(List<ServiceInstance> instances, HttpHeaders headers) {
        String tag = EnvUtils.getTag(headers);
        if (StrUtil.isEmpty(tag)) {
            // 无 tag 时，过滤掉有 tag 的节点
            List<ServiceInstance> chooseInstances = CollectionUtils.filterList(instances,
                    instance -> StrUtil.isEmpty(EnvUtils.getTag(instance)));
            if (CollUtil.isEmpty(chooseInstances)) {
                log.warn("[filterTagServiceInstances][serviceId({}) 没有不带 tag 的实例，降级]", serviceId);
                chooseInstances = instances;
            }
            return chooseInstances;
        }

        // 有 tag 时，匹配 tag
        List<ServiceInstance> chooseInstances = CollectionUtils.filterList(instances,
                instance -> tag.equals(EnvUtils.getTag(instance)));
        if (CollUtil.isEmpty(chooseInstances)) {
            log.warn("[filterTagServiceInstances][serviceId({}) 没有满足 tag({})的实例，降级]", serviceId, tag);
            chooseInstances = instances;
        }
        return chooseInstances;
    }
}
```

### EnvUtils 工具类

`EnvUtils` 负责从请求头或服务实例元数据中提取 tag 信息。它是 Gateway 模块的工具类，封装了 tag 的提取逻辑。

### Nacos 权重依赖

最终选择使用 Nacos 的 `NacosBalancer.getHostByRandomWeight3()`：
- Nacos 注册的服务实例可以配置 `nacos.weight` 元数据
- 高权重的实例获得更多流量
- 如果不使用 Nacos 注册中心，需要替换此方法

## 调用链

```
[Upstream - 请求进入 Gateway]
  客户端请求 (带 header[version] / header[tag])
    -> Gateway Handler Mapping
       -> 路由匹配
          -> ReactiveLoadBalancerClientFilter
             |
             +-- 根据 serviceId 获取 LoadBalancer
             |    GrayLoadBalancer (本类, 替换默认 RoundRobin)
             |
             +-- choose(request)
                  |
                  +-- ServiceInstanceListSupplier.get(request)
                  |    -> NacosServiceInstanceListSupplier
                  |    -> 从 Nacos 获取该服务的所有实例
                  |
                  +-- getInstanceResponse() 过滤+选择
                       |
                       +-- NacosBalancer.getHostByRandomWeight3()
                       +-- return DefaultResponse(instance)

[Downstream - 路由到具体实例]
  选择的 ServiceInstance (host:port)
    -> Gateway 转发 HTTP 请求到目标实例
```

### Bean 注册

```java
// 灰度负载均衡器配置
@Configuration
public class GrayLoadBalancerConfiguration {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer(
            Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new GrayLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
                name);
    }
}
```

需要为每个希望启用灰度路由的服务单独配置此 Bean（或者使用默认配置全局生效）。

## 配置与条件

| 请求头 | 服务元数据 Key | 说明 |
|--------|---------------|------|
| `version` | `version` | 灰度版本号，如 `v1.0.1` |
| `tag` | `tag`（通过 EnvUtils 解析） | 环境标签，如 `feat-pay` |
| - | `nacos.weight` | Nacos 权重（默认 1） |

### 服务实例配置

需要灰度路由的服务实例在 Nacos 中注册时添加元数据：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: v2.0
          tag: feat-pay
```

## 生产级关注点

### 1. 降级策略的安全设计

当 version 或 tag 过滤后没有匹配实例时，自动降级到全量实例。这种策略确保了：
- 即使灰度配置错误，也不会导致服务不可用
- 灰度版本发布初期实例数少，流量多时自动回退
- 但会打印 `log.warn`，方便监控发现配置问题

### 2. 无 tag 保护机制

当请求没有 tag 头时，自动过滤掉注册了 tag 的实例。这防止了：
- 开发者在本地启动带 tag 的实例，被测试流量误入
- feature 分支的实例被基线流量打到

**重要**: 如果所有实例都有 tag（如全量部署 feature 环境），会降级到全量实例。注释中给出了可选配置：
```java
// 【重要】如果希望在 chooseInstances 为空时，不允许打到有 tag 的实例
// 可以取消注释下面的代码
```

### 3. Reactive 响应式约束

- 基于 Reactor（`Mono`/`Flux`），适用于 Spring Cloud Gateway 的 WebFlux 架构
- 不能在此 LoadBalancer 中使用阻塞操作（如 Feign、JPA）
- `ServiceInstanceListSupplier.get(request)` 必须返回 Mono，支持异步拉取

### 4. 注册中心耦合

`NacosBalancer.getHostByRandomWeight3()` 直接耦合了 Nacos：
- 使用 Nacos 注册中心：直接可用
- 使用其他注册中心（Consul/Eureka）：需要重新实现权重选择逻辑
- 替代方案：实现自己的加权随机算法，读取实例元数据中的 `weight` 属性

### 5. 性能考虑

- 每次请求都执行此过滤逻辑（虽然操作很轻量）
- `CollectionUtils.filterList` 遍历所有实例，实例数通常不多（一般 < 100）
- `NacosBalancer.getHostByRandomWeight3()` 是 O(n) 复杂度的权重随机

### 6. 调试建议

当灰度路由不符合预期时：
1. 检查请求是否携带了正确的 `version`/`tag` 请求头
2. 检查目标服务的 Nacos 元数据是否配置正确
3. 查看 Gateway 日志中的 `log.warn` 信息（是否有降级）
4. 验证 `EnvUtils.getTag()` 的解析逻辑是否与请求头匹配
