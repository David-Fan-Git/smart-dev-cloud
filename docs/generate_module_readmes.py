from pathlib import Path
import xml.etree.ElementTree as ET
from collections import defaultdict

ROOT = Path(__file__).resolve().parents[1]
NS = {'m': 'http://maven.apache.org/POM/4.0.0'}

MODULE_PURPOSES = {
    'develop-dependencies': '统一依赖版本治理 BOM，集中管理 Spring、MyBatis、Redis、Flowable、MQ、AI、IoT、工具库等第三方依赖版本。',
    'develop-framework': '通用框架与 Spring Boot Starter 集合，为业务模块提供 Web、安全、MyBatis、Redis、MQ、RPC、任务、监控、租户、数据权限等基础能力。',
    'develop-gateway': 'Spring Cloud Gateway 网关应用，负责统一入口、路由转发、服务发现、配置接入、接口文档聚合和监控接入。',
    'develop-server': '后端主启动容器，通过 Maven 依赖按需装配业务 server 模块，本身不承载核心业务逻辑。',
    'develop-module-system': '系统管理基础模块，提供用户、部门、角色、菜单、租户、认证授权、字典、日志、通知等通用能力。',
    'develop-module-infra': '基础设施模块，提供文件、代码生成、配置管理、API 日志、定时任务管理、WebSocket、数据库工具等平台能力。',
    'develop-module-member': '会员中心模块，管理会员用户、等级、积分、签到、地址、标签、分组等用户侧能力。',
    'develop-module-bpm': '工作流模块，基于 Flowable 提供流程模型、流程定义、流程实例、任务审批、表单等 BPM 能力。',
    'develop-module-pay': '支付模块，提供支付应用、渠道、订单、退款、回调通知、钱包等支付基础能力。',
    'develop-module-report': '报表模块，集成 JimuReport / JimuBI，提供报表设计、数据可视化与 BI 能力。',
    'develop-module-mp': '微信公众号模块，提供公众号账号、菜单、粉丝、标签、素材、消息、自动回复、模板通知等能力。',
    'develop-module-mall': '商城聚合模块，由商品、营销、交易、统计等子域组成。',
    'develop-module-crm': '客户关系管理模块，管理客户、联系人、商机、合同、回款、线索、跟进记录等 CRM 能力。',
    'develop-module-erp': '企业资源计划模块，提供采购、销售、库存、财务、产品、供应商、客户等企业经营能力。',
    'develop-module-iot': '物联网模块，提供产品、设备、物模型、设备消息、协议网关、设备接入等 IoT 能力。',
    'develop-module-mes': '制造执行系统模块，提供生产计划、工单、工艺、工序、设备、质量、物料、报工等 MES 能力。',
    'develop-module-wms': '仓储管理模块，提供仓库、库区、库位、库存、入库、出库、移库、盘点等 WMS 能力。',
    'develop-module-ai': 'AI 大模型模块，提供模型配置、聊天、图片、音乐、知识库、向量分段、工作流、工具调用等 AI 能力。',
}

KNOWN_DIRS = {
    'api': '跨模块 API、DTO、枚举、RPC / CommonApi 契约。',
    'controller': 'REST 控制器，包含管理端、用户端请求入口和 VO。',
    'service': '传统三层业务服务接口与实现，是 DDD 迁移的重要来源。',
    'domain': 'DDD 领域层，聚合根、值对象、领域服务、领域事件、仓储接口。',
    'application': 'DDD 应用层，用例编排、事务边界、领域对象和仓储接口调用。',
    'infrastructure': 'DDD 基础设施层，仓储实现、外部系统适配、事件发布适配。',
    'dal': '数据访问层，包含 DO、MyBatis Mapper、Redis Key 等。',
    'convert': '对象转换层，通常使用 MapStruct 处理 VO / DTO / DO / Domain 转换。',
    'enums': '模块内枚举、错误码、状态值等。',
    'framework': '模块内 Spring 配置、拦截器、扩展点。',
    'mq': '消息生产者、消费者和消息体。',
    'job': 'XXL-Job 定时任务处理器。',
    'websocket': 'WebSocket 连接、会话和消息能力。',
}

ARTIFACT_LABELS = {
    'develop-spring-boot-starter-env': '环境标识与环境透传',
    'develop-spring-boot-starter-web': 'Web、统一异常、Swagger/Knife4j、Jackson',
    'develop-spring-boot-starter-security': '认证授权、登录用户上下文、操作日志',
    'develop-spring-boot-starter-mybatis': 'MyBatis Plus、多数据源、分页、数据翻译',
    'develop-spring-boot-starter-redis': 'Redis、Redisson、缓存配置',
    'develop-spring-boot-starter-rpc': 'OpenFeign、负载均衡、跨模块/跨服务调用',
    'develop-spring-boot-starter-mq': 'Redis/RabbitMQ/RocketMQ/Kafka 消息抽象',
    'develop-spring-boot-starter-job': 'XXL-Job 定时任务',
    'develop-spring-boot-starter-monitor': '链路追踪、指标、监控接入',
    'develop-spring-boot-starter-excel': 'Excel 导入导出、字典格式化',
    'develop-spring-boot-starter-test': '测试基类、断言、随机对象、测试工具',
    'develop-spring-boot-starter-websocket': 'WebSocket 会话与多节点广播',
    'develop-spring-boot-starter-biz-tenant': '多租户上下文、租户过滤、租户透传',
    'develop-spring-boot-starter-biz-data-permission': '数据权限、部门数据范围、SQL 过滤',
    'develop-spring-boot-starter-biz-ip': 'IP 区域与城市编码能力',
    'develop-spring-boot-starter-protection': '分布式锁、幂等、限流、服务保护',
}


def parse_pom(path: Path):
    root = ET.parse(path).getroot()
    artifact = root.findtext('m:artifactId', default=path.parent.name, namespaces=NS)
    packaging = root.findtext('m:packaging', default='jar', namespaces=NS)
    desc = ' '.join((root.findtext('m:description', default='', namespaces=NS) or '').split())
    modules = [m.text for m in root.findall('m:modules/m:module', NS) if m.text]
    deps = []
    for d in root.findall('.//m:dependencies/m:dependency', NS):
        artifact_id = d.findtext('m:artifactId', default='', namespaces=NS)
        group_id = d.findtext('m:groupId', default='', namespaces=NS)
        if artifact_id:
            deps.append((group_id, artifact_id))
    return artifact, packaging, desc, modules, deps


def module_source_dirs(module_dir: Path):
    result = []
    for src in sorted(module_dir.glob('**/src/main/java/com/develop/mvp/pk/**')):
        if not src.is_dir():
            continue
        rel_parts = src.relative_to(module_dir).parts
        for part in rel_parts:
            if part in KNOWN_DIRS:
                if part not in result:
                    result.append(part)
    return result


def count_java(module_dir: Path):
    return len(list(module_dir.glob('**/src/main/java/**/*.java')))


def important_deps(deps):
    selected = []
    for _, artifact in deps:
        if artifact.startswith('develop-module-') or artifact.startswith('develop-spring-boot-starter-'):
            selected.append(artifact)
        elif artifact in {
            'spring-cloud-starter-gateway-server-webflux', 'knife4j-gateway-spring-boot-starter',
            'spring-cloud-starter-loadbalancer', 'spring-cloud-starter-alibaba-nacos-discovery',
            'spring-cloud-starter-alibaba-nacos-config', 'flowable-spring-boot-starter-process',
            'flowable-spring-boot-starter-actuator', 'jimureport-spring-boot3-starter',
            'jimubi-spring-boot3-starter', 'wx-java-mp-spring-boot-starter',
            'wx-java-miniapp-spring-boot-starter', 'rocketmq-spring-boot-starter', 'spring-kafka',
            'spring-boot-starter-amqp'
        } or artifact.startswith('spring-ai') or artifact.endswith('spring-boot-starter'):
            selected.append(artifact)
    seen = []
    for item in selected:
        if item not in seen:
            seen.append(item)
    return seen


def submodule_table(modules):
    if not modules:
        return '本模块没有声明 Maven 子模块。'
    lines = ['| 子模块 | 职责 |', '|---|---|']
    for m in modules:
        if m.endswith('-api'):
            role = '跨模块契约、DTO、枚举、CommonApi / RPC API。'
        elif m.endswith('-server'):
            role = '业务实现、Controller、Service/ApplicationService、Domain、Infrastructure、DAL、MQ、Job。'
        elif m.endswith('-core'):
            role = '核心抽象、公共模型或跨运行单元复用能力。'
        elif m.endswith('-gateway'):
            role = '独立网关或协议接入运行单元。'
        else:
            role = '模块内部子工程。'
        lines.append(f'| `{m}` | {role} |')
    return '\n'.join(lines)


def deps_section(deps):
    deps = important_deps(deps)
    if not deps:
        return '未发现需要在 README 中强调的直接业务 API 或 Starter 依赖。'
    lines = ['| 依赖 | 说明 |', '|---|---|']
    for dep in deps:
        if dep in ARTIFACT_LABELS:
            label = ARTIFACT_LABELS[dep]
        elif dep.startswith('develop-module-'):
            label = '业务模块 API / server / core 依赖。'
        elif 'nacos' in dep:
            label = 'Nacos 注册发现或配置中心。'
        elif 'flowable' in dep:
            label = 'Flowable 工作流引擎能力。'
        elif 'jimu' in dep:
            label = 'JimuReport / JimuBI 报表能力。'
        elif 'wx-java' in dep:
            label = '微信生态 SDK 能力。'
        elif dep.startswith('spring-ai'):
            label = 'Spring AI 模型或向量存储能力。'
        elif dep in {'rocketmq-spring-boot-starter', 'spring-kafka', 'spring-boot-starter-amqp'}:
            label = '消息队列客户端能力。'
        else:
            label = '外部框架或组件依赖。'
        lines.append(f'| `{dep}` | {label} |')
    return '\n'.join(lines)


def dirs_section(module_dir):
    dirs = module_source_dirs(module_dir)
    if not dirs:
        return '当前目录未检测到标准业务源码分层目录，主要通过 Maven POM 进行依赖或聚合管理。'
    lines = ['| 目录 | 说明 |', '|---|---|']
    for d in dirs:
        lines.append(f'| `{d}` | {KNOWN_DIRS[d]} |')
    return '\n'.join(lines)


def build_commands(module_dir, artifact, packaging):
    rel = module_dir.relative_to(ROOT).as_posix()
    if packaging == 'pom':
        return f'''```bash
# 编译该聚合模块及其子模块
mvn compile -pl {rel} -am

# 打包该聚合模块及其子模块
mvn clean package -pl {rel} -am -Dmaven.test.skip=true
```'''
    if artifact in {'develop-server', 'develop-gateway'}:
        return f'''```bash
# 编译
mvn compile -pl {rel} -am

# 打包
mvn clean package -pl {rel} -am -Dmaven.test.skip=true

# 启动
mvn spring-boot:run -pl {rel} -am
```'''
    return f'''```bash
# 编译该模块及其依赖
mvn compile -pl {rel} -am

# 运行该模块测试
mvn test -pl {rel}

# 打包该模块及其依赖
mvn clean package -pl {rel} -am -Dmaven.test.skip=true
```'''


def classify_module(module_dir: Path, artifact: str, packaging: str):
    rel = module_dir.relative_to(ROOT).as_posix()
    if artifact == 'develop-dependencies':
        return 'dependencies'
    if artifact == 'develop-framework':
        return 'framework-aggregate'
    if rel.startswith('develop-framework/'):
        return 'framework-starter'
    if artifact == 'develop-gateway':
        return 'gateway'
    if artifact == 'develop-server':
        return 'server-container'
    if rel.startswith('develop-module-'):
        if artifact.endswith('-api'):
            return 'business-api'
        if artifact.endswith('-server'):
            return 'business-server'
        if artifact.endswith('-core'):
            return 'business-core'
        if artifact.endswith('-gateway'):
            return 'business-gateway'
        if packaging == 'pom':
            return 'business-aggregate'
    return 'module'


def architecture_section(module_type):
    sections = {
        'dependencies': '''- 本模块是全仓库 Maven BOM，集中治理 Spring、Spring Cloud、数据库、缓存、MQ、AI、IoT 与工具库版本。\n- 业务模块和框架模块应通过父 POM / dependencyManagement 继承版本，避免在子模块重复声明版本。\n- 调整依赖版本时，应优先验证 `develop-server`、`develop-gateway` 与受影响业务模块的编译结果。''',
        'framework-aggregate': '''- 本模块聚合通用框架能力与 Spring Boot Starter，不直接承载业务用例。\n- 各 Starter 面向业务模块输出可复用基础设施能力，例如 Web、安全、MyBatis、Redis、MQ、RPC、任务、监控与租户能力。\n- 框架能力应保持领域无关，避免反向依赖具体业务模块。''',
        'framework-starter': '''- 本模块是框架 Starter，面向业务模块提供可复用技术能力或自动配置。\n- Starter 应保持业务无关，只暴露稳定配置、拦截器、工具类、模板类或扩展点。\n- 修改 Starter 时需要关注所有依赖该 Starter 的业务模块，避免引入跨模块业务耦合。''',
        'gateway': '''- 本模块是独立网关运行单元，负责统一入口、路由转发、认证透传、跨域、灰度路由和接口文档聚合。\n- Gateway 只处理入口层协议与路由职责，不承载业务规则。\n- 后端业务能力由 `develop-server` 或独立业务服务提供，网关通过路由规则转发请求。''',
        'server-container': '''- 本模块是后端主启动容器，通过 Maven 依赖装配需要启用的业务 `server` 模块。\n- 容器本身不承载核心业务逻辑，业务实现应位于各 `develop-module-*-server` 模块。\n- 运行时启用哪些业务模块，由 `develop-server/pom.xml` 中声明的 server 依赖决定。''',
        'business-aggregate': '''- 本模块是业务域聚合 POM，用于组织该业务域下的 API、Server 或子域模块。\n- 聚合模块只负责 Maven reactor 编排和统一构建，不应放置业务代码。\n- 跨模块调用应优先依赖 API 子模块，业务实现应收敛在对应 Server 子模块。''',
        'business-api': '''- 本模块提供跨模块稳定契约，包括 DTO、枚举、CommonApi / RPC API 和调用适配接口。\n- API 模块应避免依赖业务实现层，保持轻量、稳定、可被其他模块安全引用。\n- 修改契约时需要检查所有调用方兼容性，并同步本地/远程调用适配。''',
        'business-server': '''- 本模块承载具体业务实现，包括 REST 入口、应用服务、领域模型、基础设施适配、DAL、MQ 和 Job。\n- 新增或迁移核心业务逻辑时，应优先落到 `domain`、`application`、`infrastructure`、`convert` 分层。\n- `service` 与 `dal` 中的旧逻辑是 DDD 迁移来源，不应作为新增核心业务规则的最终归宿。''',
        'business-core': '''- 本模块提供业务域内部可复用核心抽象、公共模型或协议接入支撑能力。\n- Core 模块应避免依赖具体运行入口，保持可复用和可测试。\n- 修改公共抽象时需要检查同业务域内 server、gateway 或 adapter 模块的兼容性。''',
        'business-gateway': '''- 本模块是业务域专属网关或协议接入运行单元，负责外部协议接入和请求转发。\n- 网关层不应承载核心业务规则，业务规则应下沉到对应领域或应用服务。\n- 修改协议接入能力时需要同步检查 server 模块和外部设备 / 客户端兼容性。''',
        'module': '''- 本模块是 Maven 子模块，职责以当前 POM、源码目录和依赖关系为准。\n- 模块内部应保持职责清晰，避免跨层直接依赖和业务规则泄漏。\n- 修改公共能力时需要检查依赖方的编译与运行兼容性。''',
    }
    return sections[module_type]


def ddd_note(module_dir, module_type):
    if (module_dir / '.keep').exists() or module_type in {'dependencies', 'framework-aggregate', 'gateway', 'server-container', 'business-api'}:
        return ''
    dirs = module_source_dirs(module_dir)
    if any(d in dirs for d in ('domain', 'application', 'infrastructure')):
        return '''\n## DDD / 分层说明\n\n本模块已出现 DDD / 六边形相关目录。新增或迁移核心业务逻辑时，应优先遵循以下方向：\n\n- `domain` 保持纯 Java，承载聚合根、值对象、领域服务、领域事件和仓储接口。\n- `application` 负责编排用例、事务边界和领域对象协作。\n- `infrastructure` 负责仓储实现、MyBatis / Redis / 外部系统适配。\n- `service` 与 `dal` 中的旧业务逻辑是迁移来源，不应作为新增核心业务规则的最终归宿。\n'''
    if module_type in {'business-aggregate', 'business-server', 'business-core', 'business-gateway'}:
        return '''\n## DDD / 分层说明\n\n本模块当前以传统三层或聚合 POM 管理为主。后续新增核心业务逻辑或进行重构时，应按仓库 DDD 标准逐步收敛到 `domain`、`application`、`infrastructure`、`convert` 分层。\n'''
    return ''


def maintenance_section(module_type):
    if module_type == 'dependencies':
        return '''- 升级依赖版本时，优先确认上游兼容矩阵，并运行受影响模块的 Maven 编译或测试。\n- 不要在业务模块中绕过 BOM 单独固定版本，除非存在明确兼容性原因。\n- 修改基础依赖后，应同步检查根 POM、框架 Starter 和启动模块。'''
    if module_type in {'framework-aggregate', 'framework-starter'}:
        return '''- 修改 Starter 能力时，优先保证配置项、自动配置条件和默认行为向调用方清晰可控。\n- 框架模块不应引入具体业务模块依赖；需要扩展业务行为时优先通过接口、SPI 或配置完成。\n- 至少运行当前 Starter 的 `mvn compile`，必要时补充依赖该 Starter 的业务模块编译验证。'''
    if module_type == 'gateway':
        return '''- 修改路由、认证透传或跨域配置时，应同步检查 `develop-gateway/src/main/resources` 下的环境配置。\n- 网关只维护入口层能力，不应把业务规则写入 Filter 或路由配置。\n- 调整接口文档聚合时，需要确认后端服务 OpenAPI 地址仍然可访问。'''
    if module_type == 'server-container':
        return '''- 启停业务模块时，优先修改 `develop-server/pom.xml` 中的 server 依赖，并检查根 POM reactor 是否包含对应模块。\n- 容器配置变更应同步检查 `application.yaml` 与各 profile 配置。\n- 修改启动依赖后，至少运行 `mvn compile -pl develop-server -am`。'''
    if module_type == 'business-api':
        return '''- 修改跨模块契约时，必须检查所有调用方兼容性。\n- DTO、枚举和 API 接口应保持稳定，不应依赖 server 内部实现类。\n- 涉及远程调用时，应同步检查 local / remote 适配器和 Feign 契约。'''
    if module_type in {'business-aggregate', 'business-server', 'business-core', 'business-gateway'}:
        return '''- 修改跨模块契约时，优先更新 `api` 子模块，并检查所有调用方兼容性。\n- 修改业务实现时，优先补充或更新模块级测试，至少运行当前模块的 `mvn test` 或 `mvn compile`。\n- 涉及 DDD 聚合、模块结构或 API 契约调整时，同时更新本 README 与根目录架构文档。\n- 不要在聚合 POM 模块中放置业务逻辑；业务逻辑应位于具体 `server` 子模块。'''
    return '''- 修改模块能力时，应同步检查依赖方兼容性。\n- 修改后至少运行当前模块的 Maven 编译或测试。\n- 模块职责变化时应同步更新本 README。'''


def write_readme(module_dir: Path):
    pom = module_dir / 'pom.xml'
    artifact, packaging, desc, modules, deps = parse_pom(pom)
    purpose = MODULE_PURPOSES.get(module_dir.name, desc or f'{artifact} 模块。')
    java_count = count_java(module_dir)
    rel = module_dir.relative_to(ROOT).as_posix()
    title = artifact
    module_type = classify_module(module_dir, artifact, packaging)
    content = f'''# {title}

## 模块定位

{purpose}

## 基本信息

| 项目 | 内容 |
|---|---|
| 模块路径 | `{rel}` |
| Maven Artifact | `{artifact}` |
| Packaging | `{packaging}` |
| Java 源文件数量 | {java_count} |
| 模块说明 | {desc or purpose} |

## 子模块结构

{submodule_table(modules)}

## 主要目录职责

{dirs_section(module_dir)}

## 关键依赖

{deps_section(deps)}

## 架构职责

{architecture_section(module_type)}
{ddd_note(module_dir, module_type)}
## 构建与验证

{build_commands(module_dir, artifact, packaging)}

## 维护建议

{maintenance_section(module_type)}
'''
    (module_dir / 'README.md').write_text(content, encoding='utf-8')


def main():
    targets = [
        ROOT / 'develop-dependencies',
        ROOT / 'develop-framework',
        ROOT / 'develop-gateway',
        ROOT / 'develop-server',
    ]
    targets.extend(sorted(ROOT.glob('develop-module-*')))
    # Also document framework starters and all api/server/core/gateway submodules.
    targets.extend(sorted((ROOT / 'develop-framework').glob('develop-*')))
    for parent in sorted(ROOT.glob('develop-module-*')):
        targets.extend(sorted(p for p in parent.iterdir() if p.is_dir() and (p / 'pom.xml').exists()))
    seen = []
    for target in targets:
        if target in seen or not (target / 'pom.xml').exists():
            continue
        seen.append(target)
        write_readme(target)
    print(f'Generated {len(seen)} module README files')

if __name__ == '__main__':
    main()
