package com.develop.mvp.pk.module.system.application.member.port.outbound;

/**
 * Member User Gateway 接口。
 */
public interface MemberUserGateway {

    /**
     * 查询 get Member User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Object getMemberUser(Long id);

}
