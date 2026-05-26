package com.develop.mvp.pk.module.system.dal.mysql.dict;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.type.DictTypePageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictTypeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * Dict Type Mapper 持久化 Mapper。
 */
@Mapper
public interface DictTypeMapper extends BaseMapperX<DictTypeDO> {

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default PageResult<DictTypeDO> selectPage(DictTypePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DictTypeDO>()
                .likeIfPresent(DictTypeDO::getName, reqVO.getName())
                .likeIfPresent(DictTypeDO::getType, reqVO.getType())
                .eqIfPresent(DictTypeDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(DictTypeDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DictTypeDO::getId));
    }

    /**
     * 查询 select By Type 对应的数据。
     *
     * @param type type 参数
     * @return 处理结果
     */
    default DictTypeDO selectByType(String type) {
        return selectOne(DictTypeDO::getType, type);
    }

    /**
     * 查询 select By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    default DictTypeDO selectByName(String name) {
        return selectOne(DictTypeDO::getName, name);
    }

    /**
     * 更新 update To Delete 对应的数据。
     *
     * @param id id 参数
     * @param deletedTime deletedTime 参数
     */
    @Update("UPDATE system_dict_type SET deleted = 1, deleted_time = #{deletedTime} WHERE id = #{id}")
    void updateToDelete(@Param("id") Long id, @Param("deletedTime") LocalDateTime deletedTime);

}
