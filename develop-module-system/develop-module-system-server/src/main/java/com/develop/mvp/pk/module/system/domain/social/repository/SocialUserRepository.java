package com.develop.mvp.pk.module.system.domain.social.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.domain.social.SocialUser;
import java.util.List;
import java.util.Optional;

/**
 * Social User Repository 领域仓储接口。
 */
public interface SocialUserRepository {
    /**
     * 创建 save 对应的数据。
     *
     * @param u u 参数
     * @return 处理结果
     */
    SocialUser save(SocialUser u);
    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    void delete(Long id);
    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    SocialUser findById(Long id);
    /**
     * 查询 find By User Type And Openid 对应的数据。
     *
     * @param userType userType 参数
     * @param type type 参数
     * @param openid openid 参数
     * @return 处理结果
     */
    Optional<SocialUser> findByUserTypeAndOpenid(Integer userType, Integer type, String openid);
    /**
     * 查询 find By User Id And User Type 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    List<SocialUser> findByUserIdAndUserType(Long userId, Integer userType);
    /**
     * 查询 find Page 对应的数据。
     *
     * @param nickname nickname 参数
     * @param userType userType 参数
     * @param type type 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    PageResult<SocialUser> findPage(String nickname, Integer userType, Integer type, Integer pageNo, Integer pageSize);
}
