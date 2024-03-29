package com.central.oauth.service;

import com.central.common.entity.PageResult;
import com.central.common.entity.Result;
import com.central.common.service.ISuperService;
import com.central.oauth.model.Client;

import java.util.Map;

public interface IClientService extends ISuperService<Client> {
    Result saveClient(Client clientDto);

    /**
     * 查询应用列表
     * @param params
     * @param isPage 是否分页
     */
    PageResult<Client> listClent(Map<String, Object> params, boolean isPage);

    void delClient(long id);
}
