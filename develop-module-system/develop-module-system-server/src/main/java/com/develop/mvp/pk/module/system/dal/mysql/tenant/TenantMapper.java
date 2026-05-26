package com.develop.mvp.pk.module.system.dal.mysql.tenant;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.framework.mybatis.core.util.MyBatisUtils;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.tenant.TenantPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Tenant Mapper 持久化 Mapper。
 */
@Mapper
public interface TenantMapper extends BaseMapperX<TenantDO> {

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default PageResult<TenantDO> selectPage(TenantPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TenantDO>()
                .likeIfPresent(TenantDO::getName, reqVO.getName())
                .likeIfPresent(TenantDO::getContactName, reqVO.getContactName())
                .likeIfPresent(TenantDO::getContactMobile, reqVO.getContactMobile())
                .eqIfPresent(TenantDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(TenantDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(TenantDO::getId));
    }

    /**
     * 查询 select By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    default TenantDO selectByName(String name) {
        return selectOne(TenantDO::getName, name);
    }

    /**
     * 查询 select List By Website 对应的数据。
     *
     * @param website website 参数
     * @return 处理结果
     */
    default List<TenantDO> selectListByWebsite(String website) {
        return selectList(new LambdaQueryWrapperX<TenantDO>()
                .apply(MyBatisUtils.findInSet("websites", website)));
    }

    /**
     * 查询 select Count By Package Id 对应的数据。
     *
     * @param packageId packageId 参数
     * @return 处理结果
     */
    default Long selectCountByPackageId(Long packageId) {
        return selectCount(TenantDO::getPackageId, packageId);
    }

    /**
     * 查询 select List By Package Id 对应的数据。
     *
     * @param packageId packageId 参数
     * @return 处理结果
     */
    default List<TenantDO> selectListByPackageId(Long packageId) {
        return selectList(TenantDO::getPackageId, packageId);
    }

    /**
     * 查询 select List By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    default List<TenantDO> selectListByStatus(Integer status) {
        return selectList(TenantDO::getStatus, status);
    }

}
