/**
 * RPC Starter 的基础包，封装基于 OpenFeign 的远程 HTTP 调用能力。
 *
 * <p>本包的职责边界是提供远程调用所需的基础设施。它只解决“如何调用其他服务”的技术问题，
 * 不承载订单、用户、权限等任何业务规则，也不替业务模块决定调用编排。</p>
 *
 * <p>业务模块需要跨模块或跨服务访问时，应在各自 API 契约中定义稳定接口，再由 RPC Starter 的基础能力完成远程适配；
 * 本包不应该反向依赖具体业务模块。</p>
 *
 * @author David
 */
package com.develop.mvp.pk.framework.rpc;
