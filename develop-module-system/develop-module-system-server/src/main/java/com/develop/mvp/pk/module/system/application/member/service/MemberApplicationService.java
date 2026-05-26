package com.develop.mvp.pk.module.system.application.member.service;

import cn.hutool.core.util.ReflectUtil;
import com.develop.mvp.pk.module.system.application.member.port.inbound.MemberUseCase;
import com.develop.mvp.pk.module.system.application.member.port.outbound.MemberUserGateway;

/**
 * Member Application Service 应用服务。
 */
public class MemberApplicationService implements MemberUseCase {

    private final MemberUserGateway memberUserGateway;

    /**
     * 创建 MemberApplicationService 实例。
     *
     * @param memberUserGateway memberUserGateway 参数
     */
    public MemberApplicationService(MemberUserGateway memberUserGateway) {
        this.memberUserGateway = memberUserGateway;
    }

    /**
     * 查询 get Member User Mobile 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public String getMemberUserMobile(Long id) {
        Object user = getMemberUser(id);
        if (user == null) {
            return null;
        }
        return ReflectUtil.invoke(user, "getMobile");
    }

    /**
     * 查询 get Member User Email 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public String getMemberUserEmail(Long id) {
        Object user = getMemberUser(id);
        if (user == null) {
            return null;
        }
        return ReflectUtil.invoke(user, "getEmail");
    }

    /**
     * 查询 get Member User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    private Object getMemberUser(Long id) {
        if (id == null) {
            return null;
        }
        return memberUserGateway.getMemberUser(id);
    }
}
