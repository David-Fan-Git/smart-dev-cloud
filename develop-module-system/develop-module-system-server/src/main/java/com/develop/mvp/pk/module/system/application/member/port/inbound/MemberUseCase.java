package com.develop.mvp.pk.module.system.application.member.port.inbound;

// DDD 角色：入站端口 — 定义 Member 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

/**
 * Member 聚合的入站用例端口。
 */
public interface MemberUseCase {

    /**
     * 查询 get Member User Mobile 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    String getMemberUserMobile(Long id);

    /**
     * 查询 get Member User Email 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    String getMemberUserEmail(Long id);
}
