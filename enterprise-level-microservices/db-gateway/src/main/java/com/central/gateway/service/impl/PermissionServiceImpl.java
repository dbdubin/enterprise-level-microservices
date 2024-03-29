package com.central.gateway.service.impl;

import com.central.common.entity.SysMenu;
import com.central.gateway.feign.MenuService;
import com.central.oauth2.common.service.impl.DefaultPermissionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * 请求权限判断service
 */
@Slf4j
@Service("permissionService")
public class PermissionServiceImpl extends DefaultPermissionServiceImpl {
    @Resource
    private MenuService menuService;

    @Override
    public List<SysMenu> findMenuByRoleCodes(String roleCodes) {
        return menuService.findByRoleCodes(roleCodes);
    }
}
