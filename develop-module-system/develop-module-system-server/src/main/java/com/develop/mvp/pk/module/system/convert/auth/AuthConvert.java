package com.develop.mvp.pk.module.system.convert.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import com.develop.mvp.pk.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserBindReqDTO;
import com.develop.mvp.pk.module.system.controller.admin.auth.vo.AuthPermissionInfoRespVO;
import com.develop.mvp.pk.module.system.controller.admin.auth.vo.AuthSmsLoginReqVO;
import com.develop.mvp.pk.module.system.controller.admin.auth.vo.AuthSmsSendReqVO;
import com.develop.mvp.pk.module.system.controller.admin.auth.vo.AuthSocialLoginReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.enums.permission.MenuTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.filterList;
import static com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO.ID_ROOT;

/**
 * Auth Convert 对象转换器。
 */
@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    /**
     * 转换 convert 对应的数据对象。
     *
     * @param user user 参数
     * @param roleList roleList 参数
     * @param menuList menuList 参数
     * @return 处理结果
     */
    default AuthPermissionInfoRespVO convert(AdminUserDO user, List<RoleDO> roleList, List<MenuDO> menuList) {
        return AuthPermissionInfoRespVO.builder()
                .user(BeanUtils.toBean(user, AuthPermissionInfoRespVO.UserVO.class))
                .roles(convertSet(roleList, RoleDO::getCode))
                // 权限标识信息
                .permissions(convertSet(menuList, MenuDO::getPermission))
                // 菜单树
                .menus(buildMenuTree(menuList))
                .build();
    }

    /**
     * 将菜单列表，构建成菜单树
     *
     * @param menuList 菜单列表
     * @return 菜单树
     */
    default List<AuthPermissionInfoRespVO.MenuVO> buildMenuTree(List<MenuDO> menuList) {
        if (CollUtil.isEmpty(menuList)) {
            return Collections.emptyList();
        }
        // 移除按钮
        menuList.removeIf(menu -> menu.getType().equals(MenuTypeEnum.BUTTON.getType()));
        // 排序，保证菜单的有序性
        menuList.sort(Comparator.comparing(MenuDO::getSort));

        // 构建菜单树
        // 使用 LinkedHashMap 的原因，是为了排序 。实际也可以用 Stream API ，就是太丑了。
        Map<Long, AuthPermissionInfoRespVO.MenuVO> treeNodeMap = new LinkedHashMap<>();
        menuList.forEach(menu -> treeNodeMap.put(menu.getId(),
                BeanUtils.toBean(menu, AuthPermissionInfoRespVO.MenuVO.class)));
        // 处理父子关系
        treeNodeMap.values().stream().filter(node -> ObjUtil.notEqual(node.getParentId(), ID_ROOT)).forEach(childNode -> {
            // 获得父节点
            AuthPermissionInfoRespVO.MenuVO parentNode = treeNodeMap.get(childNode.getParentId());
            if (parentNode == null) {
                LoggerFactory.getLogger(getClass()).error("[buildRouterTree][resource({}) 找不到父资源({})]",
                        childNode.getId(), childNode.getParentId());
                return;
            }
            // 将自己添加到父节点中
            if (parentNode.getChildren() == null) {
                parentNode.setChildren(new ArrayList<>());
            }
            parentNode.getChildren().add(childNode);
        });
        // 获得到所有的根节点
        return filterList(treeNodeMap.values(), node -> ID_ROOT.equals(node.getParentId()));
    }

    /**
     * 转换 convert 对应的数据对象。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    SocialUserBindReqDTO convert(Long userId, Integer userType, AuthSocialLoginReqVO reqVO);

    /**
     * 转换 convert 对应的数据对象。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    SmsCodeSendReqDTO convert(AuthSmsSendReqVO reqVO);

    /**
     * 转换 convert 对应的数据对象。
     *
     * @param reqVO reqVO 参数
     * @param scene scene 参数
     * @param usedIp usedIp 参数
     * @return 处理结果
     */
    SmsCodeUseReqDTO convert(AuthSmsLoginReqVO reqVO, Integer scene, String usedIp);

}
