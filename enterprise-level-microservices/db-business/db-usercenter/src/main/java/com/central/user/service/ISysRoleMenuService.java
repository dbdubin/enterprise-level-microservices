package com.central.user.service;

import com.central.common.entity.SysMenu;
import com.central.common.service.ISuperService;
import com.central.user.entity.SysRoleMenu;

import java.util.List;
import java.util.Set;

public interface ISysRoleMenuService extends ISuperService<SysRoleMenu> {
	int save(Long roleId, Long menuId);

	int delete(Long roleId, Long menuId);

	List<SysMenu> findMenusByRoleIds(Set<Long> roleIds, Integer type);

	List<SysMenu> findMenusByRoleCodes(Set<String> roleCodes, Integer type);
}
