package com.develop.mvp.pk.module.system.convert.tenant;

import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.tenant.TenantSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 租户 Convert
 *
 * @author David
 */
@Mapper
public interface TenantConvert {

    TenantConvert INSTANCE = Mappers.getMapper(TenantConvert.class);

    /**
     * 转换 convert02 对应的数据对象。
     *
     * @param bean bean 参数
     * @return 处理结果
     */
    default UserSaveReqVO convert02(TenantSaveReqVO bean) {
        UserSaveReqVO reqVO = new UserSaveReqVO();
        reqVO.setUsername(bean.getUsername());
        reqVO.setPassword(bean.getPassword());
        reqVO.setNickname(bean.getContactName()).setMobile(bean.getContactMobile());
        return reqVO;
    }

}
