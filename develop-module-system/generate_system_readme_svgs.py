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

svg("system-module-architecture.svg", 1180, 760, f"""
{t('develop-module-system 模块运行架构', 590, 42, 'title')}
{t('系统管理基础域：认证授权、用户组织、角色菜单、租户、字典、日志、通知、短信、OAuth2 与社交登录', 590, 68, 'subtitle')}
{box(40, 110, 210, 120, '调用方', ['管理后台 / 移动端', 'Gateway 路由', '其它业务模块'], 'white')}
{box(310, 100, 240, 140, 'develop-module-system-api', ['DTO / Enum', 'CommonApi / Feign RemoteClient', '跨模块稳定契约'], 'cyan')}
{box(630, 90, 270, 160, 'develop-module-system-server', ['Controller / ApplicationService', 'Domain / Infrastructure', 'DAL / MQ / Job'], 'blue')}
{box(960, 100, 180, 140, 'develop-server', ['主启动容器', '按依赖装配 server 模块', '统一暴露 REST API'], 'purple')}
{line(250, 170, 310, 170, label='依赖 API 契约', ly=155)}
{line(550, 170, 630, 170, label='本地实现 / 远程适配', ly=155)}
{line(900, 170, 960, 170, label='Maven 装配', ly=155)}
{box(60, 330, 180, 105, '安全与权限', ['Security Starter', 'Token / Permission', 'DataPermission'], 'green')}
{box(280, 330, 180, 105, '数据与缓存', ['MyBatis Plus', 'MySQL Mapper / DO', 'Redis / OAuth2 Token'], 'green')}
{box(500, 330, 180, 105, '多租户与组织', ['Tenant Starter', 'Dept / Post', 'Role / Menu'], 'green')}
{box(720, 330, 180, 105, '消息与任务', ['MQ Starter', 'Mail / SMS Message', 'XXL-Job Token Clean'], 'green')}
{box(940, 330, 180, 105, '外部能力', ['Infra Config API', 'JustAuth / WeChat', 'Captcha / Mail'], 'green')}
{line(720, 250, 150, 330, 'dash')}
{line(740, 250, 370, 330, 'dash')}
{line(765, 250, 590, 330, 'dash')}
{line(790, 250, 810, 330, 'dash')}
{line(820, 250, 1030, 330, 'dash')}
{box(60, 540, 250, 120, '核心业务域', ['auth / user / permission / tenant', 'dept / dict / logger', 'mail / notice / notify / sms', 'oauth2 / social / member'], 'yellow')}
{box(365, 540, 210, 120, '入站接口', ['35 个 Controller', 'admin 与 app 双端入口', '18 个 UseCase / ApplicationService'], 'yellow')}
{box(630, 540, 210, 120, '领域与基础设施', ['14 个领域上下文', '19 个领域仓储接口', '17 个仓储实现'], 'yellow')}
{box(895, 540, 210, 120, '持久化与集成', ['32 个 MyBatis Mapper', '2 个 Redis 组件', '6 个 MQ 类 / 3 个 Job 类'], 'yellow')}
{line(310, 600, 365, 600)}
{line(575, 600, 630, 600)}
{line(840, 600, 895, 600)}
""")

svg("system-layered-architecture.svg", 1180, 820, f"""
{t('system server 分层与六边形结构', 590, 42, 'title')}
{t('Controller 只做入口适配；Application 编排用例；Domain 表达领域上下文；Infrastructure 适配数据库、缓存、RPC、MQ 与外部服务', 590, 68, 'subtitle')}
{box(80, 110, 1020, 75, '入口层 controller', ['admin：认证、用户、部门、角色、菜单、租户、字典、日志、邮件、通知、OAuth2、短信、社交；app：字典、地区、租户'], 'blue')}
{box(120, 245, 940, 90, '应用层 application', ['AuthUseCase / AdminUserUseCase / RoleUseCase / PermissionUseCase / TenantUseCase / SmsUseCase ...', 'ApplicationService 负责编排事务、权限上下文、跨用例协作和外部 API 调用'], 'green')}
{box(160, 400, 860, 105, '领域层 domain', ['auth / user / permission / tenant / dept / dict / logger / mail / notice / notify / oauth2 / sms / social / member', 'repository / service / event / valueobject / specification 等领域构件目录'], 'yellow')}
{box(120, 575, 440, 95, '基础设施层 infrastructure', ['cache / external / messaging / persistence / rpc', '实现领域仓储与出站适配'], 'purple')}
{box(620, 575, 440, 95, '数据访问层 dal', ['dataobject / mysql / redis', 'MyBatis Mapper、DO、Redis Key 与 OAuth2 缓存组件'], 'orange')}
{box(120, 720, 220, 55, 'convert', ['VO / DTO / DO 转换'], 'white')}
{box(380, 720, 220, 55, 'api', ['模块内 API 实现适配'], 'white')}
{box(640, 720, 220, 55, 'mq / job', ['消息消费者与定时任务入口'], 'white')}
{box(900, 720, 160, 55, 'framework', ['模块配置扩展'], 'white')}
{line(590,185,590,245,label='请求进入 UseCase', lx=660, ly=220)}
{line(590,335,590,400,label='调用领域规则 / 仓储接口', lx=690, ly=372)}
{line(430,505,340,575,label='仓储实现', lx=335, ly=548)}
{line(750,505,820,575,label='DO / Mapper', lx=830, ly=548)}
{line(340,670,250,720,'dash')}
{line(440,670,490,720,'dash')}
{line(760,670,750,720,'dash')}
{line(910,670,980,720,'dash')}
""")

svg("system-component-matrix.svg", 1180, 760, f"""
{t('system 模块组件调用关系', 590, 42, 'title')}
{t('基于 develop-module-system-server POM 与源码目录归纳的组件依赖视图', 590, 68, 'subtitle')}
{box(60, 120, 210, 90, '认证授权', ['Security Starter', 'PasswordEncoder', 'Token / Permission'], 'blue')}
{box(335, 120, 210, 90, '租户与数据权限', ['biz-tenant', 'biz-data-permission', 'TenantUseCase'], 'green')}
{box(610, 120, 210, 90, '数据存储', ['MyBatis Starter', 'MySQL Mapper', 'Redis Starter'], 'orange')}
{box(885, 120, 210, 90, '服务治理', ['Nacos Discovery', 'Nacos Config', 'RPC Starter'], 'purple')}
{box(60, 300, 210, 90, '异步消息', ['MQ Starter', 'MailProducer / Consumer', 'SmsProducer / Consumer'], 'cyan')}
{box(335, 300, 210, 90, '任务调度', ['Job Starter', 'TokenCleanJob', 'DemoJob'], 'cyan')}
{box(610, 300, 210, 90, '外部登录', ['JustAuth', 'WeChat MP / MiniApp', 'SocialUseCase'], 'yellow')}
{box(885, 300, 210, 90, '通知能力', ['Mail Starter / Hutool Extra', 'SMS 模板与日志', 'Notify 消息'], 'yellow')}
{box(230, 520, 720, 110, 'develop-module-system-server', ['认证、用户、部门、岗位、角色、菜单、租户、字典、日志、站内信、邮件、短信、OAuth2、社交登录', '通过 API 契约支撑 infra、member、mall、bpm、pay 等上层业务模块'], 'white')}
{line(165,210,450,520)}
{line(440,210,520,520)}
{line(715,210,600,520)}
{line(990,210,680,520)}
{line(165,390,520,520)}
{line(440,390,570,520)}
{line(715,390,640,520)}
{line(990,390,730,520)}
""")

svg("system-auth-sequence.svg", 1180, 760, f"""
{t('后台账号密码登录认证时序', 590, 42, 'title')}
{t('依据 AuthController 与 AuthApplicationService 的实际调用链整理', 590, 68, 'subtitle')}
{box(60, 115, 150, 55, 'Admin Client', ['提交账号密码'], 'white')}
{box(260, 115, 160, 55, 'AuthController', ['/system/auth/login'], 'blue')}
{box(470, 115, 190, 55, 'AuthApplicationService', ['login / authenticate'], 'green')}
{box(710, 115, 150, 55, 'UserUseCase', ['查询用户 / 校验密码'], 'yellow')}
{box(910, 115, 160, 55, 'OAuth2UseCase', ['创建访问令牌'], 'purple')}
{line(135,220,340,220,label='1. POST /login', ly=205)}
{line(340,270,565,270,label='2. 调用 login(reqVO)', ly=255)}
{line(565,320,785,320,label='3. 查询用户并匹配密码', ly=305)}
{line(785,370,565,370,label='4. 返回用户状态', ly=355)}
{line(565,420,990,420,label='5. 登录成功后创建 Token', ly=405)}
{line(990,470,565,470,label='6. 返回 accessToken / refreshToken', ly=455)}
{line(565,520,340,520,label='7. 组装 AuthLoginRespVO', ly=505)}
{line(340,570,135,570,label='8. CommonResult 返回', ly=555)}
{box(150, 650, 880, 55, '关键规则', ['验证码校验失败、用户不存在、密码不匹配、用户禁用都会记录登录日志并抛出业务异常'], 'red')}
""")

svg("system-user-flow.svg", 1180, 820, f"""
{t('后台用户创建 / 更新流程', 590, 42, 'title')}
{t('依据 UserController 与 AdminUserApplicationService 的用户维护逻辑整理', 590, 68, 'subtitle')}
{box(80, 120, 220, 75, '1. 管理端提交用户信息', ['用户名、手机号、邮箱', '部门、岗位、状态'], 'blue')}
{box(380, 120, 220, 75, '2. Controller 入站适配', ['参数校验', '权限 / 操作日志注解'], 'blue')}
{box(680, 120, 260, 75, '3. AdminUserUseCase', ['createUser / updateUser', '事务边界'], 'green')}
{line(300,158,380,158)}
{line(600,158,680,158)}
{box(120, 300, 230, 85, '4. 租户账号额度校验', ['tenantService.handleTenantInfo', '账号数不能超过套餐限制'], 'yellow')}
{box(470, 300, 230, 85, '5. 唯一性与组织校验', ['用户名 / 手机 / 邮箱', '部门 / 岗位有效性'], 'yellow')}
{box(820, 300, 230, 85, '6. 密码与用户 DO', ['PasswordEncoder 加密', 'BeanUtils 转换 DO'], 'yellow')}
{line(810,195,235,300)}
{line(810,195,585,300)}
{line(810,195,935,300)}
{box(180, 505, 240, 85, '7. 写入用户表', ['AdminUserMapper', 'insert / updateById'], 'orange')}
{box(470, 505, 240, 85, '8. 写入用户岗位关系', ['UserPostMapper', 'insertBatch / delete'], 'orange')}
{box(760, 505, 240, 85, '9. 记录操作日志上下文', ['LogRecordContext', '创建 / 更新日志'], 'purple')}
{line(935,385,300,505)}
{line(935,385,590,505)}
{line(935,385,880,505)}
{box(300, 690, 580, 65, '10. 返回用户 ID 或完成状态', ['上层业务可通过 system-api 的 AdminUserApi / RemoteClient 复用用户能力'], 'white')}
{line(300,590,590,690)}
{line(590,590,590,690)}
{line(880,590,590,690)}
""")

svg("system-mq-job-flow.svg", 1180, 760, f"""
{t('system 消息与定时任务链路', 590, 42, 'title')}
{t('源码中包含邮件 / 短信 MQ 生产消费，以及 TokenCleanJob 等 XXL-Job 任务入口', 590, 68, 'subtitle')}
{box(80, 130, 210, 85, '业务用例', ['MailApplicationService', 'SmsApplicationService', 'OAuth2ApplicationService'], 'green')}
{box(390, 120, 210, 105, '消息生产者', ['MailProducer', 'SmsProducer', '发送 MailSendMessage', '发送 SmsSendMessage'], 'blue')}
{box(700, 130, 180, 85, 'MQ Starter', ['Redis/RabbitMQ', 'RocketMQ/Kafka 抽象'], 'cyan')}
{box(960, 120, 160, 105, '消费者', ['MailSendConsumer', 'SmsSendConsumer', '执行发送动作'], 'yellow')}
{line(290,172,390,172)}
{line(600,172,700,172)}
{line(880,172,960,172)}
{box(80, 390, 210, 85, 'XXL-Job 调度', ['Job Starter', '外部调度中心触发'], 'purple')}
{box(390, 390, 210, 85, 'TokenCleanJob', ['清理 OAuth2 Token', '维护认证数据'], 'orange')}
{box(700, 390, 180, 85, 'DAL / Redis', ['OAuth2 Token 表', 'Redis 缓存组件'], 'orange')}
{box(960, 390, 160, 85, '系统状态', ['令牌数据收敛', '降低历史数据堆积'], 'white')}
{line(290,432,390,432)}
{line(600,432,700,432)}
{line(880,432,960,432)}
{box(190, 620, 800, 55, '设计边界', ['消息消费者与 Job 是技术入口，核心规则仍应进入 Application / Domain，不应在入口层堆叠业务规则'], 'red')}
""")

svg("system-mind-map.svg", 1180, 820, f"""
{t('develop-module-system 代码思维导图', 590, 42, 'title')}
{t('从 Maven 子模块、业务能力、分层结构、外部组件和维护边界五个维度理解 system 模块', 590, 68, 'subtitle')}
{box(480, 350, 220, 90, 'develop-module-system', ['系统管理基础域', '支撑上层核心业务'], 'blue')}
{box(90, 120, 220, 95, 'Maven 子模块', ['develop-module-system-api', 'develop-module-system-server', '聚合 POM 不放业务代码'], 'cyan')}
{box(470, 105, 240, 125, '业务能力', ['认证 / OAuth2 / 社交登录', '用户 / 部门 / 岗位', '角色 / 菜单 / 权限', '租户 / 字典 / 日志', '邮件 / 短信 / 通知'], 'green')}
{box(860, 120, 220, 95, '分层结构', ['controller / application', 'domain / infrastructure', 'convert / dal / mq / job'], 'yellow')}
{box(95, 585, 230, 105, '外部组件', ['Security / Tenant / DataPermission', 'MyBatis / Redis / RPC', 'Nacos / MQ / XXL-Job', 'JustAuth / WeChat / Captcha'], 'purple')}
{box(480, 590, 220, 95, '跨模块契约', ['DTO / Enum', 'CommonApi', 'RemoteClient', 'local / remote 适配'], 'orange')}
{box(855, 585, 230, 105, '维护边界', ['API 稳定优先', 'Server 承载实现', '核心规则进入 DDD 分层', '入口层不堆叠业务规则'], 'red')}
{line(480,365,310,168)}
{line(590,350,590,230)}
{line(700,365,860,168)}
{line(480,430,325,637)}
{line(590,440,590,590)}
{line(700,430,855,637)}
""")

print(f"Generated system README SVG files in {OUT}")
