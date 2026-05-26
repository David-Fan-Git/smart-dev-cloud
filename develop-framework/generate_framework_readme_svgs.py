from pathlib import Path
from html import escape

OUT = Path(__file__).resolve().parent / "docs" / "images"
OUT.mkdir(parents=True, exist_ok=True)

STYLE = """
<style>
  .bg { fill:#f8fafc; }
  .panel { fill:#ffffff; stroke:#dbe3ef; stroke-width:1.2; rx:18; filter:url(#shadow); }
  .soft { fill:#f1f5f9; stroke:#cbd5e1; stroke-width:1.1; rx:14; }
  .title { font:700 28px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill:#0f172a; }
  .subtitle { font:500 14px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill:#475569; }
  .section-title { font:700 16px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill:#1e293b; }
  .box-title { font:700 15px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill:#0f172a; }
  .box-text { font:12px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill:#334155; }
  .small { font:11px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill:#64748b; }
  .tiny { font:10px Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; fill:#64748b; }
  .line { stroke:#64748b; stroke-width:1.8; fill:none; marker-end:url(#arrow); }
  .dash { stroke:#94a3b8; stroke-width:1.6; stroke-dasharray:6 5; fill:none; marker-end:url(#arrow); }
  .blue { fill:#dbeafe; stroke:#2563eb; }
  .green { fill:#dcfce7; stroke:#16a34a; }
  .yellow { fill:#fef3c7; stroke:#d97706; }
  .purple { fill:#ede9fe; stroke:#7c3aed; }
  .red { fill:#fee2e2; stroke:#dc2626; }
  .cyan { fill:#cffafe; stroke:#0891b2; }
  .orange { fill:#ffedd5; stroke:#ea580c; }
  .slate { fill:#e2e8f0; stroke:#64748b; }
  .white { fill:#ffffff; stroke:#cbd5e1; }
  .navy { fill:#e0f2fe; stroke:#0284c7; }
</style>
<defs>
  <filter id="shadow" x="-10%" y="-10%" width="120%" height="130%">
    <feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#0f172a" flood-opacity="0.10"/>
  </filter>
  <marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
    <path d="M0,0 L0,6 L9,3 z" fill="#64748b" />
  </marker>
</defs>
"""


def text(lines, x, y, cls="box-text", anchor="middle", lh=17):
    if isinstance(lines, str):
        lines = [lines]
    return "\n".join(
        f'<text x="{x}" y="{y + i * lh}" text-anchor="{anchor}" class="{cls}">{escape(line)}</text>'
        for i, line in enumerate(lines)
    )


def box(x, y, w, h, title, lines=(), cls="blue", r=14):
    body = [f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" class="{cls}"/>']
    body.append(text(title, x + w / 2, y + 25, "box-title"))
    if lines:
        body.append(text(lines, x + w / 2, y + 49, "box-text"))
    return "\n".join(body)


def panel(x, y, w, h, title=None):
    body = [f'<rect x="{x}" y="{y}" width="{w}" height="{h}" class="panel"/>']
    if title:
        body.append(text(title, x + 24, y + 34, "section-title", anchor="start"))
    return "\n".join(body)


def line(x1, y1, x2, y2, cls="line", label=None, lx=None, ly=None):
    body = f'<path d="M{x1},{y1} L{x2},{y2}" class="{cls}"/>'
    if label:
        body += "\n" + text(label, lx if lx is not None else (x1 + x2) / 2, ly if ly is not None else (y1 + y2) / 2 - 6, "small")
    return body


def curve(path, cls="line", label=None, lx=None, ly=None):
    body = f'<path d="{path}" class="{cls}"/>'
    if label:
        body += "\n" + text(label, lx, ly, "small")
    return body


def svg(name, w, h, body):
    content = f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}">\n{STYLE}\n<rect width="100%" height="100%" class="bg"/>\n{body}\n</svg>\n'
    (OUT / name).write_text(content, encoding="utf-8")


svg("framework-overall-architecture.svg", 1280, 820, f"""
{text('develop-framework 总体架构', 640, 42, 'title')}
{text('位于依赖治理、运行单元与业务模块之间，沉淀可复用的横向框架能力', 640, 70, 'subtitle')}

{panel(50, 105, 1180, 105, '依赖治理')}
{box(465, 135, 350, 52, 'develop-dependencies', ['统一治理 Spring / Cloud / MyBatis / Redis 等版本'], 'navy')}

{panel(50, 260, 1180, 150, '运行单元')}
{box(170, 310, 250, 70, 'develop-server', ['模块化单体启动容器', '按 Maven 依赖装配业务模块'], 'green')}
{box(860, 310, 250, 70, 'develop-gateway', ['API 网关入口', '路由与文档聚合'], 'green')}

{panel(50, 460, 1180, 150, '业务模块')}
{box(110, 510, 180, 64, 'system / infra', ['默认核心模块'], 'purple')}
{box(340, 510, 180, 64, 'member / bpm', ['会员与工作流'], 'slate')}
{box(570, 510, 180, 64, 'pay / mall', ['支付与商城'], 'slate')}
{box(800, 510, 180, 64, 'crm / erp', ['客户与资源'], 'slate')}
{box(1030, 510, 150, 64, 'iot / ai 等', ['可选模块'], 'slate')}

{panel(50, 660, 1180, 110, '通用框架层')}
{box(115, 700, 190, 52, 'develop-common', ['基础公共能力'], 'cyan')}
{box(360, 700, 220, 52, '技术 Starter', ['Web / Security / Data / MQ'], 'blue')}
{box(635, 700, 220, 52, '治理 Starter', ['Monitor / Protection / Job'], 'blue')}
{box(910, 700, 220, 52, 'biz-* / 工具', ['租户 / 权限 / Excel / Test'], 'blue')}

{line(640, 210, 640, 260, label='版本约束', lx=705, ly=242)}
{line(295, 410, 295, 460, label='装配业务能力', lx=390, ly=442)}
{line(985, 410, 985, 460, label='网关依赖框架能力', lx=1085, ly=442)}
{line(640, 610, 640, 660, label='复用 Starter', lx=720, ly=642)}
{curve('M210,700 C210,630 200,610 200,575', 'dash')}
{curve('M470,700 C470,635 520,610 620,575', 'dash')}
{curve('M745,700 C745,635 780,610 890,575', 'dash')}
{curve('M1020,700 C1020,635 1070,610 1105,575', 'dash')}
""")

svg("framework-starter-layering.svg", 1320, 920, f"""
{text('Starter 能力分层', 660, 42, 'title')}
{text('图内只展示层级与模块归属，具体职责以 README 职责矩阵为准', 660, 70, 'subtitle')}

{box(525, 115, 270, 64, '基础公共层', ['develop-common'], 'cyan')}

{panel(55, 235, 570, 170, '接入与接口层')}
{box(90, 285, 115, 58, 'web', ['REST 接入'], 'blue')}
{box(225, 285, 115, 58, 'security', ['安全接入'], 'blue')}
{box(360, 285, 115, 58, 'websocket', ['实时通信'], 'blue')}
{box(495, 285, 95, 58, 'rpc', ['服务调用'], 'blue')}

{panel(695, 235, 570, 170, '数据与状态层')}
{box(745, 285, 135, 58, 'mybatis', ['数据访问'], 'green')}
{box(920, 285, 120, 58, 'redis', ['缓存状态'], 'green')}
{box(1080, 285, 120, 58, 'mq', ['消息解耦'], 'green')}

{panel(55, 470, 570, 170, '治理与可靠性层')}
{box(90, 520, 115, 58, 'env', ['环境扩展'], 'yellow')}
{box(225, 520, 115, 58, 'monitor', ['可观测'], 'yellow')}
{box(360, 520, 115, 58, 'protection', ['可靠性'], 'yellow')}
{box(495, 520, 95, 58, 'job', ['任务调度'], 'yellow')}

{panel(695, 470, 570, 170, '共享业务技术能力层')}
{box(745, 520, 145, 58, 'biz-tenant', ['租户支撑'], 'purple')}
{box(925, 520, 185, 58, 'biz-data-permission', ['数据权限'], 'purple')}
{box(1145, 520, 80, 58, 'biz-ip', ['地域识别'], 'purple')}

{panel(375, 710, 570, 130, '工具与测试层')}
{box(505, 755, 130, 58, 'excel', ['数据文件'], 'orange')}
{box(685, 755, 130, 58, 'test', ['测试支撑'], 'orange')}

{line(660, 179, 340, 235, 'dash')}
{line(660, 179, 980, 235, 'dash')}
{line(660, 179, 340, 470, 'dash')}
{line(660, 179, 980, 470, 'dash')}
{line(660, 179, 660, 710, 'dash')}
""")

svg("framework-capability-map.svg", 1240, 860, f"""
{text('develop-framework 能力地图', 620, 42, 'title')}
{text('以中心辐射方式展示能力分组，便于在文档中快速定位 Starter', 620, 70, 'subtitle')}

{box(490, 365, 260, 105, 'develop-framework', ['通用框架能力', 'Spring Boot Starter 聚合'], 'blue')}

{box(95, 120, 235, 105, '基础公共能力', ['develop-common'], 'cyan')}
{box(500, 105, 240, 125, '接入与接口', ['web / security', 'websocket / rpc'], 'green')}
{box(910, 120, 235, 105, '数据与状态', ['mybatis / redis / mq'], 'yellow')}
{box(95, 630, 235, 105, '治理与可靠性', ['env / monitor', 'protection / job'], 'purple')}
{box(500, 645, 240, 105, '共享业务技术', ['biz-tenant', 'biz-data-permission / biz-ip'], 'orange')}
{box(910, 630, 235, 105, '工具与测试', ['excel / test'], 'red')}

{line(490, 385, 330, 172)}
{line(620, 365, 620, 230)}
{line(750, 385, 910, 172)}
{line(490, 450, 330, 682)}
{line(620, 470, 620, 645)}
{line(750, 450, 910, 682)}
""")

svg("framework-access-flow.svg", 1320, 620, f"""
{text('业务模块接入 framework 能力流程', 660, 42, 'title')}
{text('从识别横向能力到声明依赖、保持业务边界、完成运行验证的标准路径', 660, 70, 'subtitle')}

{box(60, 210, 155, 82, '1. 识别能力', ['Web / 数据 / MQ', '租户 / 权限等'], 'cyan')}
{box(265, 210, 155, 82, '2. 选择 Starter', ['按职责矩阵选择'], 'blue')}
{box(470, 210, 155, 82, '3. 声明依赖', ['业务模块 pom.xml'], 'green')}
{box(675, 210, 155, 82, '4. 保持边界', ['业务规则仍在模块内'], 'yellow')}
{box(880, 210, 155, 82, '5. 运行聚合', ['server / gateway'], 'purple')}
{box(1085, 210, 155, 82, '6. Maven 验证', ['compile / package'], 'orange')}

{line(215, 251, 265, 251)}
{line(420, 251, 470, 251)}
{line(625, 251, 675, 251)}
{line(830, 251, 880, 251)}
{line(1035, 251, 1085, 251)}

{panel(165, 410, 990, 92, '边界提示')}
{text('Starter 提供横向技术能力；领域规则、应用编排、业务流程仍留在业务模块 domain / application 等边界内。', 660, 466, 'box-text')}
{curve('M752,292 C752,350 660,365 660,410', 'dash')}
""")

svg("framework-autoconfig-sequence.svg", 1320, 700, f"""
{text('Spring Boot 自动配置时序', 660, 42, 'title')}
{text('Starter 进入 Classpath 后，按自动配置条件注册框架组件，业务模块再按需使用', 660, 70, 'subtitle')}

{text('Spring Boot 应用', 140, 130, 'box-title')}
{text('Maven 依赖解析', 360, 130, 'box-title')}
{text('Starter', 580, 130, 'box-title')}
{text('自动配置', 800, 130, 'box-title')}
{text('BeanFactory', 1020, 130, 'box-title')}
{text('业务模块', 1200, 130, 'box-title')}

{line(140, 155, 140, 610, 'dash')}
{line(360, 155, 360, 610, 'dash')}
{line(580, 155, 580, 610, 'dash')}
{line(800, 155, 800, 610, 'dash')}
{line(1020, 155, 1020, 610, 'dash')}
{line(1200, 155, 1200, 610, 'dash')}

{line(140, 205, 360, 205, label='1. 声明 Starter 依赖', ly=190)}
{line(360, 275, 140, 275, label='2. 加入 Classpath', ly=260)}
{line(140, 345, 580, 345, label='3. 启动发现能力', ly=330)}
{line(580, 415, 800, 415, label='4. 按条件装配', ly=400)}
{line(800, 485, 1020, 485, label='5. 注册框架组件', ly=470)}
{line(1200, 555, 1020, 555, label='6. 注入并使用', ly=540)}
""")

svg("framework-web-request-sequence.svg", 1320, 720, f"""
{text('典型 Web 请求链路', 660, 42, 'title')}
{text('展示运行单元、Web/Security Starter、业务模块与数据状态 Starter 的协作边界', 660, 70, 'subtitle')}

{text('客户端', 120, 130, 'box-title')}
{text('运行单元', 330, 130, 'box-title')}
{text('Web Starter', 535, 130, 'box-title')}
{text('Security Starter', 755, 130, 'box-title')}
{text('业务模块', 980, 130, 'box-title')}
{text('数据与状态 Starter', 1190, 130, 'box-title')}

{line(120, 155, 120, 630, 'dash')}
{line(330, 155, 330, 630, 'dash')}
{line(535, 155, 535, 630, 'dash')}
{line(755, 155, 755, 630, 'dash')}
{line(980, 155, 980, 630, 'dash')}
{line(1190, 155, 1190, 630, 'dash')}

{line(120, 205, 330, 205, label='1. 发起请求', ly=190)}
{line(330, 275, 535, 275, label='2. Web 处理链路', ly=260)}
{line(535, 345, 755, 345, label='3. 安全处理', ly=330)}
{line(755, 415, 980, 415, label='4. 转交业务处理', ly=400)}
{line(980, 485, 1190, 485, label='5. 使用基础能力', ly=470)}
{line(1190, 555, 980, 555, label='6. 返回处理结果', ly=540)}
{curve('M980,610 L535,610 L120,610', 'line', label='7. 统一响应', lx=560, ly=596)}
""")

print(f"Generated framework README SVG files in {OUT}")
