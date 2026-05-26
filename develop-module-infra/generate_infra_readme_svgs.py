from pathlib import Path
from html import escape

OUT = Path(__file__).resolve().parent / "docs" / "images"
OUT.mkdir(parents=True, exist_ok=True)

STYLE = """
<style>
  .bg { fill:#f8fafc; }
  .title { font:700 26px Arial, 'Microsoft YaHei', sans-serif; fill:#0f172a; }
  .subtitle { font:500 14px Arial, 'Microsoft YaHei', sans-serif; fill:#475569; }
  .box-title { font:700 15px Arial, 'Microsoft YaHei', sans-serif; fill:#0f172a; }
  .box-text { font:12px Arial, 'Microsoft YaHei', sans-serif; fill:#334155; }
  .small { font:11px Arial, 'Microsoft YaHei', sans-serif; fill:#475569; }
  .line { stroke:#64748b; stroke-width:1.8; fill:none; marker-end:url(#arrow); }
  .dash { stroke:#94a3b8; stroke-width:1.5; stroke-dasharray:6 5; fill:none; marker-end:url(#arrow); }
  .blue { fill:#dbeafe; stroke:#2563eb; }
  .green { fill:#dcfce7; stroke:#16a34a; }
  .yellow { fill:#fef9c3; stroke:#ca8a04; }
  .purple { fill:#ede9fe; stroke:#7c3aed; }
  .red { fill:#fee2e2; stroke:#dc2626; }
  .cyan { fill:#cffafe; stroke:#0891b2; }
  .orange { fill:#ffedd5; stroke:#ea580c; }
  .gray { fill:#e2e8f0; stroke:#64748b; }
  .white { fill:#ffffff; stroke:#cbd5e1; }
</style>
<defs>
  <marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
    <path d="M0,0 L0,6 L9,3 z" fill="#64748b" />
  </marker>
</defs>
"""

def t(lines, x, y, cls="box-text", anchor="middle", lh=17):
    if isinstance(lines, str):
        lines = [lines]
    return "\n".join(
        f'<text x="{x}" y="{y + i * lh}" text-anchor="{anchor}" class="{cls}">{escape(line)}</text>'
        for i, line in enumerate(lines)
    )

def box(x, y, w, h, title, lines=(), cls="blue", r=12):
    body = [f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" class="{cls}"/>']
    body.append(t(title, x + w / 2, y + 24, "box-title"))
    if lines:
        body.append(t(lines, x + w / 2, y + 47, "box-text"))
    return "\n".join(body)

def line(x1, y1, x2, y2, cls="line", label=None, lx=None, ly=None):
    body = f'<path d="M{x1},{y1} L{x2},{y2}" class="{cls}"/>'
    if label:
        body += "\n" + t(label, lx if lx is not None else (x1+x2)/2, ly if ly is not None else (y1+y2)/2 - 6, "small")
    return body

def svg(name, w, h, body):
    content = f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}">\n{STYLE}\n<rect width="100%" height="100%" class="bg"/>\n{body}\n</svg>\n'
    (OUT / name).write_text(content, encoding="utf-8")

svg("infra-module-architecture.svg", 1180, 760, f"""
{t('develop-module-infra 模块运行架构', 590, 42, 'title')}
{t('基础设施运维管理：配置、文件、数据源、API 日志、Redis、WebSocket 与任务清理', 590, 68, 'subtitle')}
{box(45, 115, 210, 115, '调用方', ['管理后台 / App', '其它业务模块', 'develop-server 容器'], 'white')}
{box(315, 105, 240, 135, 'develop-module-infra-api', ['ConfigApi / FileApi', 'WebSocketSenderApi', 'DTO / Enum 稳定契约'], 'cyan')}
{box(635, 95, 265, 155, 'develop-module-infra-server', ['Controller / ApplicationService', 'Domain / Infrastructure', 'DAL / Framework', 'Job / WebSocket'], 'blue')}
{box(960, 105, 175, 135, '运行时支撑', ['Nacos / Redis', 'MySQL / MQ / XXL-Job', '文件存储后端'], 'purple')}
{line(255,172,315,172,label='依赖 API', ly=156)}
{line(555,172,635,172,label='本地实现', ly=156)}
{line(900,172,960,172,label='基础设施', ly=156)}
{box(70, 330, 180, 105, '配置中心', ['ConfigController', 'ConfigApplicationService', 'ConfigApi'], 'green')}
{box(290, 330, 180, 105, '文件能力', ['FileController / AppFile', 'FileClientFactory', 'DB/Local/FTP/SFTP/S3'], 'green')}
{box(510, 330, 180, 105, '数据源管理', ['DataSourceConfig', '数据源配置', '连接校验'], 'green')}
{box(730, 330, 180, 105, '日志审计', ['ApiAccessLog', 'ApiErrorLog', '清理 Job'], 'green')}
{box(950, 330, 180, 105, '运维入口', ['RedisController', 'WebSocket', 'Monitor / Security'], 'green')}
{line(750,250,160,330,'dash')}
{line(760,250,380,330,'dash')}
{line(775,250,600,330,'dash')}
{line(800,250,820,330,'dash')}
{line(830,250,1040,330,'dash')}
{box(70, 555, 240, 100, '核心上下文', ['config / db / file / logger', 'event 领域事件目录'], 'yellow')}
{box(360, 555, 210, 100, '规模指标', ['271 个 Java 文件', '13 个 Controller', '6 个 ApplicationService'], 'yellow')}
{box(625, 555, 210, 100, '持久化', ['6 个领域仓储接口', '6 个仓储实现', '18 个 MyBatis Mapper'], 'yellow')}
{box(890, 555, 210, 100, '技术扩展', ['文件客户端 5 类存储', '2 个日志清理 Job', 'WebSocket 发送 API'], 'yellow')}
{line(310,605,360,605)}
{line(570,605,625,605)}
{line(835,605,890,605)}
""")

svg("infra-layered-architecture.svg", 1180, 820, f"""
{t('infra server 分层与六边形结构', 590, 42, 'title')}
{t('基础设施模块已出现 application/domain/infrastructure 分层，同时保留 service 旧目录作为迁移来源', 590, 68, 'subtitle')}
{box(80, 105, 1020, 78, '入口层 controller / websocket / job', ['管理端：配置、数据源、文件、API 日志、Redis；App：文件上传；Job：日志清理；WebSocket：消息推送'], 'blue')}
{box(125, 245, 930, 90, '应用层 application', ['Config / DataSourceConfig / File / FileConfig / ApiAccessLog / ApiErrorLog ApplicationService', '负责用例编排、事务边界、查询命令对象和端口调用'], 'green')}
{box(165, 400, 850, 105, '领域层 domain', ['config / db / file / logger / event', 'repository / valueobject / event 等领域构件目录'], 'yellow')}
{box(90, 575, 310, 92, '基础设施层 infrastructure', ['cache / external / messaging / persistence / rpc', '实现仓储和外部适配'], 'purple')}
{box(435, 575, 310, 92, '数据访问层 dal', ['dataobject / mysql', '配置、文件、数据源、日志、示例表 Mapper'], 'orange')}
{box(780, 575, 310, 92, 'framework 技术能力', ['文件客户端工厂', 'DB / Local / FTP / SFTP / S3', 'Monitor / RPC / Security 配置'], 'cyan')}
{box(105, 720, 200, 55, 'api', ['契约实现适配'], 'white')}
{box(345, 720, 200, 55, 'convert', ['VO/DTO/DO 转换'], 'white')}
{box(585, 720, 200, 55, 'service', ['旧服务目录 / 迁移来源'], 'red')}
{box(825, 720, 250, 55, 'mq / job', ['消息目录占位与日志清理任务'], 'white')}
{line(590,183,590,245,label='请求 / 调度进入用例', lx=685, ly=220)}
{line(590,335,590,400,label='调用领域规则与仓储接口', lx=700, ly=372)}
{line(455,505,245,575,label='仓储实现', lx=300, ly=548)}
{line(590,505,590,575,label='Mapper / DO', lx=665, ly=548)}
{line(730,505,935,575,label='文件客户端等技术适配', lx=890, ly=548)}
{line(245,667,205,720,'dash')}
{line(590,667,445,720,'dash')}
{line(590,667,685,720,'dash')}
{line(935,667,950,720,'dash')}
""")

svg("infra-component-matrix.svg", 1180, 760, f"""
{t('infra 模块组件调用关系', 590, 42, 'title')}
{t('基于 develop-module-infra-server POM 与源码目录归纳的组件依赖视图', 590, 68, 'subtitle')}
{box(60, 120, 210, 90, 'Web 与安全', ['Security Starter', 'WebSocket Starter', 'Tenant Starter'], 'blue')}
{box(335, 120, 210, 90, '数据与缓存', ['MyBatis Starter', 'Redis Starter', '多数据源配置'], 'green')}
{box(610, 120, 210, 90, '服务治理', ['RPC Starter', 'Nacos Discovery', 'Nacos Config'], 'purple')}
{box(885, 120, 210, 90, '任务与消息', ['Job Starter', 'MQ Starter', '日志清理 Job'], 'cyan')}
{box(60, 310, 210, 90, '文件客户端', ['commons-net FTP', 'JSch SFTP', 'AWS S3 SDK', 'Tika 文件识别'], 'orange')}
{box(335, 310, 210, 90, '数据源管理', ['数据源配置', '连接参数维护', '基础设施运维'], 'yellow')}
{box(610, 310, 210, 90, '监控与日志', ['Monitor Starter', 'ApiAccessLog', 'ApiErrorLog'], 'yellow')}
{box(885, 310, 210, 90, '跨模块 API', ['ConfigApi', 'FileApi', 'WebSocketSenderApi'], 'cyan')}
{box(230, 540, 720, 105, 'develop-module-infra-server', ['配置管理、文件管理、数据源管理、API 日志、Redis 管理、WebSocket', '为 system 和其它业务模块提供配置、文件、WebSocket 等基础能力'], 'white')}
{line(165,210,470,540)}
{line(440,210,530,540)}
{line(715,210,600,540)}
{line(990,210,675,540)}
{line(165,400,520,540)}
{line(440,400,575,540)}
{line(715,400,645,540)}
{line(990,400,735,540)}
""")

svg("infra-file-flow.svg", 1180, 820, f"""
{t('文件上传与存储选择流程', 590, 42, 'title')}
{t('依据 FileController / AppFileController、FileApplicationService 与 framework/file 客户端体系整理', 590, 68, 'subtitle')}
{box(80, 120, 220, 75, '1. 调用方上传文件', ['管理端 FileController', 'AppFileController', '或 FileApi'], 'blue')}
{box(380, 120, 220, 75, '2. FileApplicationService', ['校验与编排', '创建文件记录'], 'green')}
{box(680, 120, 260, 75, '3. FileConfigApplicationService', ['读取主配置', '确定存储渠道'], 'green')}
{line(300,158,380,158)}
{line(600,158,680,158)}
{box(80, 315, 200, 82, 'DB 存储', ['DBFileClient', '内容写入数据库'], 'yellow')}
{box(315, 315, 200, 82, '本地存储', ['LocalFileClient', '本地磁盘路径'], 'yellow')}
{box(550, 315, 200, 82, 'FTP / SFTP', ['FtpFileClient', 'SftpFileClient'], 'yellow')}
{box(785, 315, 200, 82, 'S3 兼容存储', ['S3FileClient', 'MinIO / 云厂商 S3'], 'yellow')}
{line(810,195,180,315,'dash')}
{line(810,195,415,315,'dash')}
{line(810,195,650,315,'dash')}
{line(810,195,885,315,'dash')}
{box(245, 535, 270, 85, '4. FileClientFactory', ['根据配置创建 / 缓存客户端', '屏蔽具体存储差异'], 'purple')}
{box(650, 535, 270, 85, '5. 持久化文件元数据', ['FileMapper / FileDO', '记录 URL、类型、大小等信息'], 'orange')}
{line(415,397,380,535)}
{line(650,397,380,535)}
{line(885,397,380,535)}
{line(380,620,785,620)}
{box(350, 700, 480, 55, '6. 返回文件 URL / 文件标识', ['上层模块通过 FileApi 复用统一文件上传能力'], 'white')}
{line(785,620,590,700)}
""")

svg("infra-config-flow.svg", 1180, 760, f"""
{t('配置读取与跨模块调用流程', 590, 42, 'title')}
{t('system 等模块通过 infra-api 读取运行配置，infra server 负责配置查询与持久化', 590, 68, 'subtitle')}
{box(70, 130, 190, 70, '业务模块', ['例如 system', '调用 ConfigApi'], 'white')}
{box(330, 125, 210, 80, 'develop-module-infra-api', ['ConfigApi', '稳定 DTO / 契约'], 'cyan')}
{box(610, 125, 220, 80, 'ConfigApplicationService', ['按 key 查询配置', '校验可见性和状态'], 'green')}
{box(900, 125, 190, 80, 'ConfigMapper', ['读取 infra_config', '返回 ConfigDO'], 'orange')}
{line(260,165,330,165,label='1. getConfigValueByKey', ly=148)}
{line(540,165,610,165,label='2. 本地/远程适配', ly=148)}
{line(830,165,900,165,label='3. 查询数据库', ly=148)}
{line(900,255,610,255,label='4. 返回配置值', ly=238)}
{line(610,335,330,335,label='5. CommonResult/DTO', ly=318)}
{line(330,415,165,415,label='6. 调用方执行业务规则', ly=398)}
{box(120, 555, 940, 70, '典型使用场景', ['用户注册开关、用户初始化密码、文件主配置、运行时业务参数等应通过配置能力读取，不应在业务模块硬编码。'], 'yellow')}
""")

svg("infra-log-clean-flow.svg", 1180, 760, f"""
{t('API 日志记录与清理流程', 590, 42, 'title')}
{t('infra 管理 API 访问日志和错误日志，并通过 XXL-Job 定期清理历史数据', 590, 68, 'subtitle')}
{box(70, 130, 190, 80, 'Web 请求', ['业务接口访问', '异常请求'], 'white')}
{box(330, 130, 210, 80, '日志采集 / API', ['ApiAccessLog', 'ApiErrorLog'], 'blue')}
{box(610, 130, 220, 80, 'ApplicationService', ['分页查询', '处理错误日志', '写入/更新状态'], 'green')}
{box(900, 130, 190, 80, 'MyBatis Mapper', ['访问日志表', '错误日志表'], 'orange')}
{line(260,170,330,170)}
{line(540,170,610,170)}
{line(830,170,900,170)}
{box(110, 405, 210, 80, 'XXL-Job', ['定时触发'], 'purple')}
{box(390, 405, 230, 80, 'AccessLogCleanJob', ['清理访问日志'], 'cyan')}
{box(690, 405, 230, 80, 'ErrorLogCleanJob', ['清理错误日志'], 'cyan')}
{line(320,445,390,445)}
{line(620,445,690,445)}
{line(505,485,900,210,'dash',label='删除历史访问日志', lx=675, ly=350)}
{line(805,485,970,210,'dash',label='删除历史错误日志', lx=925, ly=350)}
{box(170, 625, 840, 55, '维护边界', ['日志查询和处理属于 infra 运维能力；业务模块只产生日志语义，不应直接管理 infra 日志表生命周期。'], 'red')}
""")

svg("infra-mind-map.svg", 1180, 820, f"""
{t('develop-module-infra 代码思维导图', 590, 42, 'title')}
{t('从模块结构、业务能力、文件体系、运维能力和维护边界理解 infra 模块', 590, 68, 'subtitle')}
{box(480, 350, 220, 90, 'develop-module-infra', ['基础设施运维管理', '平台通用能力支撑'], 'blue')}
{box(90, 115, 220, 100, 'Maven 子模块', ['develop-module-infra-api', 'develop-module-infra-server', '聚合 POM 不放业务代码'], 'cyan')}
{box(465, 100, 250, 125, '业务能力', ['配置管理 / 数据源管理', '文件配置 / 文件上传', 'API 访问日志 / 错误日志', 'Redis 管理 / WebSocket'], 'green')}
{box(855, 115, 230, 100, '文件体系', ['FileClientFactory', 'DB / Local', 'FTP / SFTP / S3', 'Tika 文件类型识别'], 'yellow')}
{box(90, 585, 230, 105, '外部组件', ['Tenant / Security / WebSocket', 'MyBatis / Redis / RPC', 'Nacos / MQ / XXL-Job', '文件存储组件'], 'purple')}
{box(470, 590, 240, 95, '跨模块契约', ['ConfigApi', 'FileApi', 'WebSocketSenderApi', 'DTO / Enum'], 'orange')}
{box(855, 585, 230, 105, '维护边界', ['infra 提供平台能力', '业务规则不上移到 infra', '旧 service 目录逐步迁移', '入口层不堆叠核心逻辑'], 'red')}
{line(480,365,310,165)}
{line(590,350,590,225)}
{line(700,365,855,165)}
{line(480,430,320,637)}
{line(590,440,590,590)}
{line(700,430,855,637)}
""")

print(f"Generated infra README SVG files in {OUT}")
