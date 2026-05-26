from pathlib import Path
import html

OUT = Path(__file__).parent

STYLE = """
<style>
  .title { font: 700 24px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill: #111827; }
  .subtitle { font: 500 14px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill: #4b5563; }
  .box-title { font: 700 15px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill: #111827; }
  .box-text { font: 12px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill: #374151; }
  .small { font: 11px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill: #4b5563; }
  .line { stroke: #64748b; stroke-width: 1.8; fill: none; marker-end: url(#arrow); }
  .dash { stroke: #94a3b8; stroke-width: 1.6; stroke-dasharray: 6 5; fill: none; marker-end: url(#arrow); }
  .section { fill: #f8fafc; stroke: #cbd5e1; stroke-width: 1.2; rx: 18; }
  .blue { fill: #dbeafe; stroke: #2563eb; }
  .green { fill: #dcfce7; stroke: #16a34a; }
  .yellow { fill: #fef3c7; stroke: #d97706; }
  .purple { fill: #ede9fe; stroke: #7c3aed; }
  .red { fill: #fee2e2; stroke: #dc2626; }
  .cyan { fill: #cffafe; stroke: #0891b2; }
  .gray { fill: #f1f5f9; stroke: #64748b; }
  .dark { fill: #111827; stroke: #111827; }
</style>
<defs>
  <marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
    <path d="M0,0 L0,6 L9,3 z" fill="#64748b" />
  </marker>
</defs>
"""

def esc(s):
    return html.escape(s)

def text_lines(lines, x, y, cls="box-text", line_height=18, anchor="middle"):
    return "\n".join(
        f'<text x="{x}" y="{y + i * line_height}" text-anchor="{anchor}" class="{cls}">{esc(line)}</text>'
        for i, line in enumerate(lines)
    )

def box(x, y, w, h, title, lines=(), cls="blue"):
    title_y = y + 26
    body_y = y + 48
    return f'''
<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="12" class="{cls}"/>
<text x="{x + w/2}" y="{title_y}" text-anchor="middle" class="box-title">{esc(title)}</text>
{text_lines(lines, x + w/2, body_y)}
'''

def line(x1, y1, x2, y2, cls="line", label=None, lx=None, ly=None):
    label_svg = ""
    if label:
        label_svg = f'<text x="{lx or (x1+x2)/2}" y="{ly or (y1+y2)/2 - 6}" text-anchor="middle" class="small">{esc(label)}</text>'
    return f'<path d="M{x1},{y1} L{x2},{y2}" class="{cls}"/>\n{label_svg}'

def svg(name, width, height, body):
    content = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">
{STYLE}
<rect width="100%" height="100%" fill="#ffffff"/>
{body}
</svg>
'''
    (OUT / name).write_text(content, encoding="utf-8")

svg("overall-architecture.svg", 1400, 940, f'''
<text x="700" y="42" text-anchor="middle" class="title">Smart Cloud 整体运行时架构</text>
<text x="700" y="68" text-anchor="middle" class="subtitle">模块化单体优先，保留网关、注册配置、RPC、MQ、任务、监控等微服务演进能力</text>
<rect x="40" y="95" width="1320" height="150" class="section"/>
{box(90, 130, 220, 80, "前端 / 外部系统", ["Web / App / 第三方调用"], "cyan")}
{box(410, 130, 220, 80, "develop-gateway", ["Gateway / 路由 / 文档聚合"], "blue")}
{box(730, 130, 220, 80, "develop-server", ["后端主容器", "按依赖装配模块"], "green")}
{line(310,170,410,170,label="HTTP / WebSocket")}
{line(630,170,730,170,label="路由转发")}
<path d="M310,200 C470,270 640,270 730,200" class="dash"/>
<text x="520" y="278" text-anchor="middle" class="small">本地开发可直连 develop-server</text>

<rect x="40" y="300" width="1320" height="265" class="section"/>
<text x="70" y="330" class="box-title">业务模块层</text>
{box(80, 360, 150, 58, "system", ["系统管理"], "purple")}
{box(250, 360, 150, 58, "infra", ["基础设施"], "purple")}
{box(420, 360, 150, 58, "member", ["会员中心"], "gray")}
{box(590, 360, 150, 58, "bpm", ["工作流"], "gray")}
{box(760, 360, 150, 58, "pay", ["支付"], "gray")}
{box(930, 360, 190, 58, "mall", ["product / promotion", "trade / statistics"], "gray")}
{box(1140, 360, 150, 58, "crm / erp", ["客户 / 企业资源"], "gray")}
{box(80, 455, 150, 58, "iot", ["server / core / gateway"], "gray")}
{box(250, 455, 150, 58, "mes", ["制造执行"], "gray")}
{box(420, 455, 150, 58, "wms", ["仓储管理"], "gray")}
{box(590, 455, 150, 58, "ai", ["大模型能力"], "gray")}
{box(760, 455, 150, 58, "mp", ["微信公众号"], "gray")}
{box(930, 455, 150, 58, "report", ["报表 BI"], "gray")}
<path d="M840,210 L840,300" class="line"/>

<rect x="40" y="615" width="1320" height="150" class="section"/>
<text x="70" y="645" class="box-title">通用框架层 develop-framework</text>
{box(80, 670, 130, 58, "Web", ["REST / OpenAPI"], "blue")}
{box(230, 670, 130, 58, "Security", ["认证 / 授权"], "blue")}
{box(380, 670, 130, 58, "MyBatis", ["ORM / 多数据源"], "blue")}
{box(530, 670, 130, 58, "Redis", ["缓存 / 锁"], "blue")}
{box(680, 670, 130, 58, "MQ", ["消息抽象"], "blue")}
{box(830, 670, 130, 58, "RPC", ["Feign / API"], "blue")}
{box(980, 670, 130, 58, "Job", ["XXL-Job"], "blue")}
{box(1130, 670, 160, 58, "Governance", ["租户 / 数据权限 / 监控"], "blue")}
<path d="M700,565 L700,615" class="line"/>

<rect x="40" y="810" width="1320" height="90" class="section"/>
<text x="70" y="840" class="box-title">基础设施与外部系统</text>
{box(125, 855, 140, 38, "Database", [], "yellow")}
{box(300, 855, 120, 38, "Redis", [], "yellow")}
{box(455, 855, 120, 38, "Nacos", [], "yellow")}
{box(610, 855, 180, 38, "MQ / Broker", [], "yellow")}
{box(825, 855, 150, 38, "XXL-Job", [], "yellow")}
{box(1010, 855, 160, 38, "对象存储", [], "yellow")}
{box(1205, 855, 120, 38, "第三方", [], "yellow")}
<path d="M700,765 L700,810" class="line"/>
''')

svg("module-internal-architecture.svg", 1300, 820, f'''
<text x="650" y="42" text-anchor="middle" class="title">标准业务模块内部架构</text>
<text x="650" y="68" text-anchor="middle" class="subtitle">api 暴露跨模块契约，server 承载入站适配、应用编排、领域模型、基础设施适配与旧三层迁移源</text>
<rect x="60" y="105" width="330" height="250" class="section"/>
<text x="85" y="138" class="box-title">develop-module-xxx-api</text>
{box(105, 165, 230, 55, "DTO / Enum", ["跨模块传输对象"], "cyan")}
{box(105, 240, 230, 55, "CommonApi / RPC API", ["稳定调用契约"], "cyan")}

<rect x="460" y="105" width="780" height="650" class="section"/>
<text x="485" y="138" class="box-title">develop-module-xxx-server</text>
{box(500, 165, 200, 62, "Controller", ["admin / app", "参数校验 / 权限"], "blue")}
{box(500, 260, 200, 62, "ApplicationService", ["用例编排", "事务边界"], "green")}
{box(780, 260, 200, 62, "ServiceImpl", ["旧三层业务逻辑", "迁移来源"], "gray")}
{box(500, 370, 200, 70, "Domain", ["聚合根 / 值对象", "领域服务 / 事件"], "purple")}
{box(500, 485, 200, 62, "Repository Port", ["领域仓储接口"], "purple")}
{box(780, 485, 200, 62, "Infrastructure", ["仓储实现", "外部系统适配"], "yellow")}
{box(1010, 485, 170, 62, "DAL", ["DO / Mapper", "Redis Key"], "yellow")}
{box(780, 165, 200, 62, "Convert", ["VO / DTO / DO", "Domain 转换"], "cyan")}
{box(1010, 165, 170, 62, "MQ / Job", ["消息 / 定时任务"], "red")}

{line(335,268,460,268,label="依赖契约")}
{line(700,196,780,196)}
{line(600,227,600,260)}
{line(700,291,780,291,label="旧代码并存")}
{line(600,322,600,370)}
{line(600,440,600,485)}
{line(700,516,780,516)}
{line(980,516,1010,516)}
{line(880,227,880,260)}
{line(980,196,1010,196)}
''')

svg("local-deployment.svg", 1200, 720, f'''
<text x="600" y="42" text-anchor="middle" class="title">本地开发部署拓扑</text>
<text x="600" y="68" text-anchor="middle" class="subtitle">默认 local profile：develop-server 端口 48080，本地关闭 Nacos 注册发现与配置中心</text>
{box(80, 130, 220, 80, "开发者本机", ["IDE / Terminal", "Java 17 / Maven"], "cyan")}
{box(410, 110, 260, 80, "Maven", ["compile / test / package"], "blue")}
{box(410, 230, 260, 90, "develop-server", ["Spring Boot", "默认端口 48080"], "green")}
{box(760, 230, 260, 90, "develop-gateway", ["可选启动", "Gateway 路由"], "blue")}

{line(300,170,410,150)}
{line(300,170,410,275)}
{line(670,275,760,275,label="可选转发")}

<rect x="70" y="410" width="1060" height="220" class="section"/>
<text x="100" y="445" class="box-title">本地依赖服务</text>
{box(120, 485, 180, 70, "MySQL", ["ruoyi-vue-pro", "业务数据库"], "yellow")}
{box(340, 485, 180, 70, "Redis", ["127.0.0.1:6379", "缓存 / Token / 锁"], "yellow")}
{box(560, 485, 180, 70, "Nacos", ["本地默认关闭", "可按需开启"], "gray")}
{box(780, 485, 180, 70, "MQ / XXL-Job", ["默认按需启用", "异步 / 定时任务"], "gray")}

{line(540,320,210,485)}
{line(540,320,430,485)}
<path d="M540,320 L650,485" class="dash"/>
<path d="M540,320 L870,485" class="dash"/>
''')

svg("gateway-routing.svg", 1300, 760, f'''
<text x="650" y="42" text-anchor="middle" class="title">网关路由与文档聚合</text>
<text x="650" y="68" text-anchor="middle" class="subtitle">develop-gateway 基于 Path 路由到各业务服务，并通过 Knife4j 聚合 OpenAPI 文档</text>
{box(70, 320, 180, 80, "前端 / 外部调用", ["HTTP 请求"], "cyan")}
{box(360, 300, 230, 120, "develop-gateway", ["Spring Cloud Gateway", "Path 路由", "Knife4j 文档聚合"], "blue")}
{line(250,360,360,360,label="/admin-api/** / app-api/**")}

{box(760, 100, 180, 58, "system-server", ["/system/**"], "purple")}
{box(1000, 100, 180, 58, "infra-server", ["/infra/**"], "purple")}
{box(760, 200, 180, 58, "member-server", ["/member/**"], "gray")}
{box(1000, 200, 180, 58, "bpm-server", ["/bpm/**"], "gray")}
{box(760, 300, 180, 58, "pay-server", ["/pay/**"], "gray")}
{box(1000, 300, 180, 58, "mall 子域", ["/product / trade"], "gray")}
{box(760, 400, 180, 58, "crm / erp", ["/crm / erp"], "gray")}
{box(1000, 400, 180, 58, "ai / iot", ["/ai / iot"], "gray")}
{box(880, 560, 180, 58, "OpenAPI", ["/v3/api-docs"], "green")}

{line(590,360,760,129)}
{line(590,360,1000,129)}
{line(590,360,760,229)}
{line(590,360,1000,229)}
{line(590,360,760,329)}
{line(590,360,1000,329)}
{line(590,360,760,429)}
{line(590,360,1000,429)}
<path d="M590,390 C700,560 760,590 880,590" class="dash"/>
''')

svg("component-matrix.svg", 1300, 900, f'''
<text x="650" y="42" text-anchor="middle" class="title">业务模块与基础组件调用关系</text>
<text x="650" y="68" text-anchor="middle" class="subtitle">基于各 *-server/pom.xml 直接依赖整理：业务模块通过 api 契约与 Framework Starter 获得平台能力</text>
<rect x="40" y="100" width="1220" height="730" class="section"/>
<text x="70" y="132" class="box-title">业务模块</text>
{box(80, 165, 120, 42, "system", [], "purple")}
{box(80, 225, 120, 42, "infra", [], "purple")}
{box(80, 285, 120, 42, "member", [], "gray")}
{box(80, 345, 120, 42, "bpm", [], "gray")}
{box(80, 405, 120, 42, "pay", [], "gray")}
{box(80, 465, 120, 42, "mall", [], "gray")}
{box(80, 525, 120, 42, "crm / erp", [], "gray")}
{box(80, 585, 120, 42, "iot / mes / wms", [], "gray")}
{box(80, 645, 120, 42, "ai", [], "gray")}

<text x="395" y="132" text-anchor="middle" class="box-title">Framework Starter</text>
{box(280, 165, 95, 42, "Web", [], "blue")}
{box(390, 165, 95, 42, "Security", [], "blue")}
{box(500, 165, 95, 42, "MyBatis", [], "blue")}
{box(610, 165, 95, 42, "Redis", [], "blue")}
{box(720, 165, 95, 42, "RPC", [], "blue")}
{box(280, 235, 95, 42, "MQ", [], "blue")}
{box(390, 235, 95, 42, "Job", [], "blue")}
{box(500, 235, 95, 42, "Tenant", [], "blue")}
{box(610, 235, 95, 42, "Excel", [], "blue")}
{box(720, 235, 95, 42, "Monitor", [], "blue")}

<text x="1035" y="132" text-anchor="middle" class="box-title">外部组件</text>
{box(910, 165, 110, 42, "Database", [], "yellow")}
{box(1040, 165, 110, 42, "Redis", [], "yellow")}
{box(1170, 165, 70, 42, "Nacos", [], "yellow")}
{box(910, 235, 110, 42, "MQ", [], "yellow")}
{box(1040, 235, 110, 42, "XXL-Job", [], "yellow")}
{box(1170, 235, 70, 42, "SDK", [], "yellow")}

<path d="M200,186 C250,186 250,186 280,186" class="line"/>
<path d="M200,246 C250,246 250,246 280,246" class="line"/>
<path d="M200,306 C420,306 420,210 500,186" class="line"/>
<path d="M200,366 C420,366 420,210 500,186" class="line"/>
<path d="M200,426 C530,426 530,210 610,186" class="line"/>
<path d="M200,486 C640,486 640,210 720,186" class="line"/>
<path d="M200,546 C530,546 530,260 500,256" class="line"/>
<path d="M200,606 C530,606 530,210 610,186" class="line"/>
<path d="M200,666 C760,666 760,260 720,256" class="line"/>

<path d="M595,186 C760,186 760,186 910,186" class="line"/>
<path d="M705,186 C850,186 850,186 1040,186" class="line"/>
<path d="M815,186 C930,186 930,186 1170,186" class="line"/>
<path d="M375,256 C640,256 640,256 910,256" class="line"/>
<path d="M485,256 C750,256 750,256 1040,256" class="line"/>
<path d="M815,256 C950,256 950,256 1170,256" class="line"/>

<text x="650" y="780" text-anchor="middle" class="subtitle">说明：不同模块按需依赖 Starter 与外部组件；表格详见 README 中“业务模块组件调用矩阵”。</text>
''')

svg("request-sequence.svg", 1300, 760, f'''
<text x="650" y="42" text-anchor="middle" class="title">后台管理接口请求时序</text>
<text x="650" y="68" text-anchor="middle" class="subtitle">展示从前端请求、网关转发、鉴权、业务编排、持久化到统一响应的完整链路</text>
{box(65, 110, 130, 55, "Client", ["前端"], "cyan")}
{box(245, 110, 150, 55, "Gateway", ["路由"], "blue")}
{box(445, 110, 150, 55, "Controller", ["HTTP 入口"], "blue")}
{box(645, 110, 150, 55, "Security", ["鉴权 / 租户"], "purple")}
{box(845, 110, 150, 55, "Service", ["用例编排"], "green")}
{box(1045, 110, 150, 55, "Repository", ["持久化"], "yellow")}

<path d="M130,165 L130,690" stroke="#cbd5e1" stroke-width="1"/>
<path d="M320,165 L320,690" stroke="#cbd5e1" stroke-width="1"/>
<path d="M520,165 L520,690" stroke="#cbd5e1" stroke-width="1"/>
<path d="M720,165 L720,690" stroke="#cbd5e1" stroke-width="1"/>
<path d="M920,165 L920,690" stroke="#cbd5e1" stroke-width="1"/>
<path d="M1120,165 L1120,690" stroke="#cbd5e1" stroke-width="1"/>

{line(130,220,320,220,label="1 HTTP 请求",ly=210)}
{line(320,285,520,285,label="2 Path 路由",ly=275)}
{line(520,350,720,350,label="3 Token / 权限 / 租户",ly=340)}
{line(720,415,920,415,label="4 放行并注入上下文",ly=405)}
{line(920,480,1120,480,label="5 查询 / 保存",ly=470)}
<path d="M1120,545 L920,545" class="line"/>
<text x="1020" y="535" text-anchor="middle" class="small">6 返回 DO / Domain</text>
<path d="M920,610 L520,610" class="line"/>
<text x="720" y="600" text-anchor="middle" class="small">7 Convert 为 VO / DTO</text>
<path d="M520,675 L130,675" class="line"/>
<text x="325" y="665" text-anchor="middle" class="small">8 CommonResult 响应</text>
''')

svg("auth-sequence.svg", 1300, 760, f'''
<text x="650" y="42" text-anchor="middle" class="title">登录认证与权限校验时序</text>
<text x="650" y="68" text-anchor="middle" class="subtitle">system 模块提供用户、角色、菜单、OAuth2 与 Token 能力，Security Starter 在业务请求中解析并校验上下文</text>
{box(60, 110, 130, 55, "Client", ["前端"], "cyan")}
{box(230, 110, 160, 55, "Auth API", ["system 登录接口"], "blue")}
{box(440, 110, 160, 55, "Auth Service", ["OAuth2 / Token"], "green")}
{box(650, 110, 150, 55, "Redis", ["Token Store"], "yellow")}
{box(850, 110, 170, 55, "Permission", ["角色 / 菜单"], "purple")}
{box(1070, 110, 160, 55, "Business API", ["业务接口"], "blue")}

<path d="M125,165 L125,690" stroke="#cbd5e1" stroke-width="1"/>
<path d="M310,165 L310,690" stroke="#cbd5e1" stroke-width="1"/>
<path d="M520,165 L520,690" stroke="#cbd5e1" stroke-width="1"/>
<path d="M725,165 L725,690" stroke="#cbd5e1" stroke-width="1"/>
<path d="M935,165 L935,690" stroke="#cbd5e1" stroke-width="1"/>
<path d="M1150,165 L1150,690" stroke="#cbd5e1" stroke-width="1"/>

{line(125,220,310,220,label="1 登录请求")}
{line(310,285,520,285,label="2 校验账号密码")}
{line(520,350,935,350,label="3 查询用户角色权限")}
{line(520,415,725,415,label="4 写入 Token")}
<path d="M520,480 L125,480" class="line"/>
<text x="322" y="470" text-anchor="middle" class="small">5 返回 Token</text>
{line(125,545,1150,545,label="6 携带 Token 请求业务接口")}
<path d="M1150,610 L725,610" class="line"/>
<text x="938" y="600" text-anchor="middle" class="small">7 读取 Token / 用户上下文</text>
<path d="M1150,665 L935,665" class="line"/>
<text x="1042" y="655" text-anchor="middle" class="small">8 校验权限</text>
''')

svg("ddd-write-flow.svg", 1200, 760, f'''
<text x="600" y="42" text-anchor="middle" class="title">DDD 聚合写入流程</text>
<text x="600" y="68" text-anchor="middle" class="subtitle">ApplicationService 编排用例，Domain 执行业务不变量，Infrastructure 适配持久化与事件发布</text>
{box(90, 110, 190, 60, "Controller", ["接收请求 / 参数校验"], "blue")}
{box(390, 110, 220, 60, "ApplicationService", ["事务边界 / 用例编排"], "green")}
{box(720, 110, 190, 60, "Factory", ["创建聚合"], "cyan")}
{box(720, 230, 190, 70, "AggregateRoot", ["业务规则", "不变量校验"], "purple")}
{box(390, 360, 220, 70, "Repository Port", ["领域仓储接口"], "purple")}
{box(720, 360, 190, 70, "RepositoryImpl", ["Domain <-> DO"], "yellow")}
{box(960, 360, 160, 70, "Mapper / DB", ["insert / update"], "yellow")}
{box(390, 530, 220, 70, "DomainEvent", ["领域事件"], "red")}
{box(720, 530, 190, 70, "Event Adapter", ["Spring Event / MQ"], "red")}

{line(280,140,390,140)}
{line(610,140,720,140)}
{line(815,170,815,230)}
{line(720,265,610,395,label="save")}
{line(610,395,720,395)}
{line(910,395,960,395)}
{line(500,430,500,530,label="publish")}
{line(610,565,720,565)}
''')

svg("mq-job-flow.svg", 1200, 760, f'''
<text x="600" y="42" text-anchor="middle" class="title">消息与定时任务处理链路</text>
<text x="600" y="68" text-anchor="middle" class="subtitle">业务模块通过 MQ 解耦异步处理，通过 XXL-Job 执行定时同步、超时处理、统计刷新等任务</text>
<rect x="60" y="110" width="1080" height="260" class="section"/>
<text x="90" y="142" class="box-title">MQ 异步消息链路</text>
{box(110, 185, 180, 60, "业务 Service", ["提交本地数据"], "green")}
{box(370, 185, 180, 60, "MQ Producer", ["构造消息"], "blue")}
{box(630, 185, 200, 60, "MQ Broker", ["RedisMQ / RabbitMQ", "Kafka / RocketMQ"], "yellow")}
{box(920, 185, 160, 60, "MQ Consumer", ["异步消费"], "blue")}
{line(290,215,370,215)}
{line(550,215,630,215)}
{line(830,215,920,215)}
{box(920, 285, 160, 50, "下游业务", ["更新 / 记录 / 通知"], "green")}
{line(1000,245,1000,285)}

<rect x="60" y="430" width="1080" height="240" class="section"/>
<text x="90" y="462" class="box-title">XXL-Job 定时任务链路</text>
{box(110, 510, 180, 60, "XXL-Job Admin", ["调度触发"], "yellow")}
{box(370, 510, 180, 60, "Executor", ["业务应用执行器"], "blue")}
{box(630, 510, 180, 60, "JobHandler", ["模块 job 包"], "green")}
{box(890, 510, 190, 60, "Service / DB / MQ", ["业务处理", "日志回写"], "purple")}
{line(290,540,370,540)}
{line(550,540,630,540)}
{line(810,540,890,540)}
<path d="M980,570 C900,645 260,645 200,570" class="dash"/>
<text x="600" y="650" text-anchor="middle" class="small">执行结果与日志回传 XXL-Job Admin</text>
''')

svg("ai-knowledge-flow.svg", 1200, 780, f'''
<text x="600" y="42" text-anchor="middle" class="title">AI 知识库处理流程</text>
<text x="600" y="68" text-anchor="middle" class="subtitle">ai-server 依赖 infra 文件能力、Tika 文档解析、Embedding 模型与向量存储完成知识库检索增强</text>
{box(80, 120, 180, 60, "上传文档", ["前端 / 管理端"], "cyan")}
{box(330, 120, 180, 60, "infra 文件服务", ["保存原始文件"], "blue")}
{box(580, 120, 180, 60, "ai-server", ["读取文件"], "green")}
{box(830, 120, 220, 60, "Tika Reader", ["解析文本内容"], "yellow")}

{line(260,150,330,150)}
{line(510,150,580,150)}
{line(760,150,830,150)}

{box(80, 310, 180, 60, "文本切分", ["Segment"], "green")}
{box(330, 310, 180, 60, "Embedding", ["生成向量"], "purple")}
{box(580, 310, 220, 60, "向量存储", ["Qdrant / Redis Vector", "Milvus"], "yellow")}
{box(870, 310, 180, 60, "知识库索引", ["可检索片段"], "green")}

<path d="M940,180 C940,245 170,245 170,310" class="line"/>
{line(260,340,330,340)}
{line(510,340,580,340)}
{line(800,340,870,340)}

{box(170, 530, 200, 60, "用户提问", ["Chat Request"], "cyan")}
{box(460, 530, 210, 60, "向量召回", ["检索相关片段"], "yellow")}
{box(760, 530, 230, 60, "大模型生成", ["OpenAI / Anthropic", "DeepSeek / Ollama 等"], "purple")}
{line(370,560,460,560)}
{line(670,560,760,560)}
<path d="M690,370 C690,450 565,450 565,530" class="dash"/>
''')

svg("mall-payment-flow.svg", 1200, 760, f'''
<text x="600" y="42" text-anchor="middle" class="title">商城交易与支付协作链路</text>
<text x="600" y="68" text-anchor="middle" class="subtitle">trade 作为交易主流程，协同 member、product、promotion、pay 完成下单、支付和状态回写</text>
{box(70, 160, 160, 60, "member-api", ["会员信息"], "cyan")}
{box(70, 280, 160, 60, "product-api", ["商品 / 库存"], "cyan")}
{box(70, 400, 160, 60, "promotion-api", ["优惠 / 活动"], "cyan")}
{box(380, 270, 210, 90, "trade-server", ["购物车 / 结算", "订单 / 售后", "配送 / 分销"], "green")}
{box(740, 270, 190, 90, "pay-server", ["支付应用", "支付订单 / 退款"], "purple")}
{box(1000, 270, 150, 90, "支付渠道", ["支付宝 / 微信"], "yellow")}
{box(740, 500, 190, 70, "MQ", ["支付成功消息"], "red")}

{line(230,190,380,300)}
{line(230,310,380,315)}
{line(230,430,380,330)}
{line(590,315,740,315,label="创建支付单")}
{line(930,315,1000,315,label="渠道请求")}
<path d="M1075,360 C1075,470 840,470 840,500" class="line"/>
<path d="M740,535 C570,610 470,470 470,360" class="line"/>
<text x="590" y="610" text-anchor="middle" class="small">支付成功后更新交易订单状态</text>
''')

print(f"Generated SVG files in {OUT}")
