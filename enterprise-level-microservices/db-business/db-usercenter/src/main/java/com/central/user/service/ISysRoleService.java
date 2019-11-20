package com.central.user.service;

import java.util.Map;

import com.central.common.entity.PageResult;
import com.central.common.entity.Result;
import com.central.common.entity.SysRole;
import com.central.common.service.ISuperService;

public interface ISysRoleService extends ISuperService<SysRole> {
	void saveRole(SysRole sysRole);

	void deleteRole(Long id);

	/**
	 * 角色列表
	 * @param params
	 * @return
	 */
	PageResult<SysRole> findRoles(Map<String, Object> params);

	/**
	 * 新增或更新角色
	 * @param sysRole
	 * @return Result
	 */
	Result saveOrUpdateRole(SysRole sysRole);
}
