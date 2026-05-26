package com.develop.mvp.pk.module.system.application.dept.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.datapermission.core.annotation.DataPermission;
import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.dept.DeptSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.post.PostPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.post.PostSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.PostDO;
import com.develop.mvp.pk.module.system.dal.mysql.dept.DeptMapper;
import com.develop.mvp.pk.module.system.dal.mysql.dept.PostMapper;
import com.develop.mvp.pk.module.system.dal.redis.RedisKeyConstants;
import com.develop.mvp.pk.module.system.domain.dept.Dept;
import com.develop.mvp.pk.module.system.domain.dept.DeptFactory;
import com.develop.mvp.pk.module.system.domain.dept.event.DeptDomainEvent;
import com.develop.mvp.pk.module.system.domain.dept.repository.DeptRepository;
import com.develop.mvp.pk.module.system.domain.dept.valueobject.DeptId;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertMap;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

/**
 * Dept Application Service 应用服务。
 */
@Validated
public class DeptApplicationService implements DeptUseCase {

	private final DeptRepository deptRepository;

	private final ApplicationEventPublisher eventPublisher;

	private final DeptMapper deptMapper;

	private final PostMapper postMapper;

	/**
	 * 创建 DeptApplicationService 实例。
	 *
	 * @param deptRepository deptRepository 参数
	 * @param eventPublisher eventPublisher 参数
	 * @param deptMapper     deptMapper 参数
	 * @param postMapper     postMapper 参数
	 */
	public DeptApplicationService(@Autowired(required = false) DeptRepository deptRepository, @Autowired(required = false) ApplicationEventPublisher eventPublisher, DeptMapper deptMapper, PostMapper postMapper) {

		this.deptRepository = deptRepository;
		this.eventPublisher = eventPublisher;
		this.deptMapper = deptMapper;
		this.postMapper = postMapper;
	}

	/**
	 * 创建 create Dept 对应的数据。
	 *
	 * @param createReqVO createReqVO 参数
	 * @return 处理结果
	 */
	@CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true)
	public Long createDept(DeptSaveReqVO createReqVO) {

		if (createReqVO.getParentId() == null) {
			createReqVO.setParentId(DeptDO.PARENT_ID_ROOT);
		}
		validateParentDept(null, createReqVO.getParentId());
		validateDeptNameUnique(null, createReqVO.getParentId(), createReqVO.getName());
		DeptDO dept = BeanUtils.toBean(createReqVO, DeptDO.class);
		deptMapper.insert(dept);
		return dept.getId();
	}

	/**
	 * 更新 update Dept 对应的数据。
	 *
	 * @param updateReqVO updateReqVO 参数
	 */
	@CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true)
	public void updateDept(DeptSaveReqVO updateReqVO) {

		if (updateReqVO.getParentId() == null) {
			updateReqVO.setParentId(DeptDO.PARENT_ID_ROOT);
		}
		validateDeptExists(updateReqVO.getId());
		validateParentDept(updateReqVO.getId(), updateReqVO.getParentId());
		validateDeptNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());
		DeptDO updateObj = BeanUtils.toBean(updateReqVO, DeptDO.class);
		deptMapper.updateById(updateObj);
	}

	/**
	 * 删除 delete Dept 对应的数据。
	 *
	 * @param id id 参数
	 */
	@CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true)
	public void deleteDept(Long id) {

		validateDeptExists(id);
		if (deptMapper.selectCountByParentId(id) > 0) {
			throw exception(DEPT_EXITS_CHILDREN);
		}
		deptMapper.deleteById(id);
	}

	/**
	 * 删除 delete Dept List 对应的数据。
	 *
	 * @param ids ids 参数
	 */
	@CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true)
	public void deleteDeptList(List<Long> ids) {

		for (Long id : ids) {
			if (deptMapper.selectCountByParentId(id) > 0) {
				throw exception(DEPT_EXITS_CHILDREN);
			}
		}
		deptMapper.deleteByIds(ids);
	}

	/**
	 * 校验 validate Dept Exists 对应的业务规则。
	 *
	 * @param id id 参数
	 */
	@VisibleForTesting
	void validateDeptExists(Long id) {

		if (id == null) {
			return;
		}
		DeptDO dept = deptMapper.selectById(id);
		if (dept == null) {
			throw exception(DEPT_NOT_FOUND);
		}
	}

	/**
	 * 校验 validate Parent Dept 对应的业务规则。
	 *
	 * @param id       id 参数
	 * @param parentId parentId 参数
	 */
	@VisibleForTesting
	void validateParentDept(Long id, Long parentId) {

		if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
			return;
		}
		if (Objects.equals(id, parentId)) {
			throw exception(DEPT_PARENT_ERROR);
		}
		DeptDO parentDept = deptMapper.selectById(parentId);
		if (parentDept == null) {
			throw exception(DEPT_PARENT_NOT_EXITS);
		}
		if (id == null) {
			return;
		}
		for (int i = 0; i < Short.MAX_VALUE; i++) {
			parentId = parentDept.getParentId();
			if (Objects.equals(id, parentId)) {
				throw exception(DEPT_PARENT_IS_CHILD);
			}
			if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
				break;
			}
			parentDept = deptMapper.selectById(parentId);
			if (parentDept == null) {
				break;
			}
		}
	}

	/**
	 * 校验 validate Dept Name Unique 对应的业务规则。
	 *
	 * @param id       id 参数
	 * @param parentId parentId 参数
	 * @param name     name 参数
	 */
	@VisibleForTesting
	void validateDeptNameUnique(Long id, Long parentId, String name) {

		DeptDO dept = deptMapper.selectByParentIdAndName(parentId, name);
		if (dept == null) {
			return;
		}
		if (id == null || ObjectUtil.notEqual(dept.getId(), id)) {
			throw exception(DEPT_NAME_DUPLICATE);
		}
	}

	/**
	 * 查询 get Dept 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public DeptDO getDept(Long id) {

		return deptMapper.selectById(id);
	}

	/**
	 * 查询 get Dept List 对应的数据。
	 *
	 * @param ids ids 参数
	 * @return 处理结果
	 */
	public List<DeptDO> getDeptList(Collection<Long> ids) {

		if (CollUtil.isEmpty(ids)) {
			return Collections.emptyList();
		}
		return deptMapper.selectByIds(ids);
	}

	/**
	 * 查询 get Dept List 对应的数据。
	 *
	 * @param reqVO reqVO 参数
	 * @return 处理结果
	 */
	public List<DeptDO> getDeptList(DeptListReqVO reqVO) {

		List<DeptDO> list = deptMapper.selectList(reqVO);
		list.sort(Comparator.comparing(DeptDO::getSort));
		return list;
	}

	/**
	 * 查询 get Dept Map 对应的数据。
	 *
	 * @param ids ids 参数
	 * @return 处理结果
	 */
	public Map<Long, DeptDO> getDeptMap(Collection<Long> ids) {

		List<DeptDO> list = getDeptList(ids);
		return CollectionUtils.convertMap(list, DeptDO::getId);
	}

	/**
	 * 查询 get Child Dept List 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public List<DeptDO> getChildDeptList(Long id) {

		return getChildDeptList(Collections.singleton(id));
	}

	/**
	 * 查询 get Child Dept List 对应的数据。
	 *
	 * @param ids ids 参数
	 * @return 处理结果
	 */
	public List<DeptDO> getChildDeptList(Collection<Long> ids) {

		List<DeptDO> children = new LinkedList<>();
		Collection<Long> parentIds = ids;
		for (int i = 0; i < Short.MAX_VALUE; i++) {
			List<DeptDO> depts = deptMapper.selectListByParentId(parentIds);
			if (CollUtil.isEmpty(depts)) {
				break;
			}
			children.addAll(depts);
			parentIds = convertSet(depts, DeptDO::getId);
		}
		return children;
	}

	/**
	 * 查询 get Dept List By Leader User Id 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public List<DeptDO> getDeptListByLeaderUserId(Long id) {

		return deptMapper.selectListByLeaderUserId(id);
	}

	/**
	 * 查询 get Child Dept Id List From Cache 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	@DataPermission(enable = false)
	@Cacheable(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, key = "#id")
	public Set<Long> getChildDeptIdListFromCache(Long id) {

		List<DeptDO> children = getChildDeptList(id);
		return convertSet(children, DeptDO::getId);
	}

	/**
	 * 校验 validate Dept List 对应的业务规则。
	 *
	 * @param ids ids 参数
	 */
	public void validateDeptList(Collection<Long> ids) {

		if (CollUtil.isEmpty(ids)) {
			return;
		}
		Map<Long, DeptDO> deptMap = getDeptMap(ids);
		ids.forEach(id -> {
			DeptDO dept = deptMap.get(id);
			if (dept == null) {
				throw exception(DEPT_NOT_FOUND);
			}
			if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
				throw exception(DEPT_NOT_ENABLE, dept.getName());
			}
		});
	}

	/**
	 * 创建 create Post 对应的数据。
	 *
	 * @param createReqVO createReqVO 参数
	 * @return 处理结果
	 */
	public Long createPost(PostSaveReqVO createReqVO) {

		validatePostForCreateOrUpdate(null, createReqVO.getName(), createReqVO.getCode());
		PostDO post = BeanUtils.toBean(createReqVO, PostDO.class);
		postMapper.insert(post);
		return post.getId();
	}

	/**
	 * 更新 update Post 对应的数据。
	 *
	 * @param updateReqVO updateReqVO 参数
	 */
	public void updatePost(PostSaveReqVO updateReqVO) {

		validatePostForCreateOrUpdate(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getCode());
		PostDO updateObj = BeanUtils.toBean(updateReqVO, PostDO.class);
		postMapper.updateById(updateObj);
	}

	/**
	 * 删除 delete Post 对应的数据。
	 *
	 * @param id id 参数
	 */
	public void deletePost(Long id) {

		validatePostExists(id);
		postMapper.deleteById(id);
	}

	/**
	 * 删除 delete Post List 对应的数据。
	 *
	 * @param ids ids 参数
	 */
	public void deletePostList(List<Long> ids) {

		postMapper.deleteByIds(ids);
	}

	/**
	 * 查询 get Post List 对应的数据。
	 *
	 * @param ids ids 参数
	 * @return 处理结果
	 */
	public List<PostDO> getPostList(Collection<Long> ids) {

		if (CollUtil.isEmpty(ids)) {
			return Collections.emptyList();
		}
		return postMapper.selectByIds(ids);
	}

	/**
	 * 查询 get Post List 对应的数据。
	 *
	 * @param ids      ids 参数
	 * @param statuses statuses 参数
	 * @return 处理结果
	 */
	public List<PostDO> getPostList(Collection<Long> ids, Collection<Integer> statuses) {

		return postMapper.selectList(ids, statuses);
	}

	/**
	 * 查询 get Post Page 对应的数据。
	 *
	 * @param reqVO reqVO 参数
	 * @return 处理结果
	 */
	public PageResult<PostDO> getPostPage(PostPageReqVO reqVO) {

		return postMapper.selectPage(reqVO);
	}

	/**
	 * 查询 get Post 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public PostDO getPost(Long id) {

		return postMapper.selectById(id);
	}

	/**
	 * 校验 validate Post List 对应的业务规则。
	 *
	 * @param ids ids 参数
	 */
	public void validatePostList(Collection<Long> ids) {

		if (CollUtil.isEmpty(ids)) {
			return;
		}
		List<PostDO> posts = postMapper.selectByIds(ids);
		Map<Long, PostDO> postMap = convertMap(posts, PostDO::getId);
		ids.forEach(id -> {
			PostDO post = postMap.get(id);
			if (post == null) {
				throw exception(POST_NOT_FOUND);
			}
			if (!CommonStatusEnum.ENABLE.getStatus().equals(post.getStatus())) {
				throw exception(POST_NOT_ENABLE, post.getName());
			}
		});
	}

	/**
	 * 校验 validate Post For Create Or Update 对应的业务规则。
	 *
	 * @param id   id 参数
	 * @param name name 参数
	 * @param code code 参数
	 */
	private void validatePostForCreateOrUpdate(Long id, String name, String code) {

		validatePostExists(id);
		validatePostNameUnique(id, name);
		validatePostCodeUnique(id, code);
	}

	/**
	 * 校验 validate Post Name Unique 对应的业务规则。
	 *
	 * @param id   id 参数
	 * @param name name 参数
	 */
	private void validatePostNameUnique(Long id, String name) {

		PostDO post = postMapper.selectByName(name);
		if (post == null) {
			return;
		}
		if (id == null || !post.getId().equals(id)) {
			throw exception(POST_NAME_DUPLICATE);
		}
	}

	/**
	 * 校验 validate Post Code Unique 对应的业务规则。
	 *
	 * @param id   id 参数
	 * @param code code 参数
	 */
	private void validatePostCodeUnique(Long id, String code) {

		PostDO post = postMapper.selectByCode(code);
		if (post == null) {
			return;
		}
		if (id == null || !post.getId().equals(id)) {
			throw exception(POST_CODE_DUPLICATE);
		}
	}

	/**
	 * 校验 validate Post Exists 对应的业务规则。
	 *
	 * @param id id 参数
	 */
	private void validatePostExists(Long id) {

		if (id == null) {
			return;
		}
		if (postMapper.selectById(id) == null) {
			throw exception(POST_NOT_FOUND);
		}
	}

	/**
	 * 创建 create Dept Domain 对应的数据。
	 *
	 * @param id           id 参数
	 * @param name         name 参数
	 * @param parentId     parentId 参数
	 * @param sort         sort 参数
	 * @param leaderUserId leaderUserId 参数
	 * @param phone        phone 参数
	 * @param email        email 参数
	 * @return 处理结果
	 */
	@Transactional
	public Long createDeptDomain(Long id, String name, Long parentId, Integer sort, Long leaderUserId, String phone, String email) {

		if (deptRepository.existsByName(name, null)) {
			throw exception(DEPT_NAME_DUPLICATE);
		}
		if (parentId != null && parentId != 0L) {
			Dept parent = deptRepository.findById(DeptId.of(parentId));
			if (parent == null) {
				throw exception(DEPT_PARENT_NOT_EXITS);
			}
			if (!parent.isEnabled()) {
				throw exception(DEPT_NOT_ENABLE, parent.name().value());
			}
		}
		Dept dept = DeptFactory.create(id, name, parentId, sort, leaderUserId, phone, email);
		deptRepository.save(dept);
		publishEvents(dept);
		return id;
	}

	/**
	 * 更新 update Dept Domain 对应的数据。
	 *
	 * @param id           id 参数
	 * @param name         name 参数
	 * @param parentId     parentId 参数
	 * @param sort         sort 参数
	 * @param leaderUserId leaderUserId 参数
	 * @param phone        phone 参数
	 * @param email        email 参数
	 */
	@Transactional
	public void updateDeptDomain(Long id, String name, Long parentId, Integer sort, Long leaderUserId, String phone, String email) {

		findExistingDomain(id);
		if (parentId != null && parentId.equals(id)) {
			throw exception(DEPT_PARENT_ERROR);
		}
		Dept saved = DeptFactory.create(id, name, parentId, sort, leaderUserId, phone, email);
		deptRepository.save(saved);
	}

	/**
	 * 删除 delete Dept Domain 对应的数据。
	 *
	 * @param id id 参数
	 */
	@Transactional
	public void deleteDeptDomain(Long id) {

		Dept dept = findExistingDomain(id);
		if (!deptRepository.findByParentId(id).isEmpty()) {
			throw exception(DEPT_EXITS_CHILDREN);
		}
		dept.markDeleted();
		deptRepository.delete(dept.id());
		publishEvents(dept);
	}

	/**
	 * 查询 get Dept Domain 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public Dept getDeptDomain(Long id) {

		return deptRepository.findById(DeptId.of(id));
	}

	/**
	 * 查询 get Dept Domain List 对应的数据。
	 *
	 * @param ids ids 参数
	 * @return 处理结果
	 */
	public List<Dept> getDeptDomainList(Collection<Long> ids) {

		return deptRepository.findByIds(ids.stream().map(DeptId::of).collect(Collectors.toList()));
	}

	/**
	 * 查询 get All Dept Domains 对应的数据。
	 *
	 * @return 处理结果
	 */
	public List<Dept> getAllDeptDomains() {

		return deptRepository.findAll();
	}

	/**
	 * 查询 get Child Dept Domain List 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public List<Dept> getChildDeptDomainList(Long id) {

		return deptRepository.findByParentId(id);
	}

	/**
	 * 查询 get Child Dept Domain Ids From Cache 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public Set<Long> getChildDeptDomainIdsFromCache(Long id) {

		return deptRepository.findChildIdsFromCache(id);
	}

	/**
	 * 查询 find Existing Domain 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	private Dept findExistingDomain(Long id) {

		Dept dept = deptRepository.findById(DeptId.of(id));
		if (dept == null) {
			throw exception(DEPT_NOT_FOUND);
		}
		return dept;
	}

	/**
	 * 发送 publish Events 对应的消息。
	 *
	 * @param dept dept 参数
	 */
	private void publishEvents(Dept dept) {

		if (eventPublisher == null) {
			return;
		}
		for (DeptDomainEvent event : dept.pullEvents()) {
			eventPublisher.publishEvent(event);
		}
	}

}
