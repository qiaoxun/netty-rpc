package com.qiao.service;

import com.qiao.bean.InfoUser;
import com.qiao.server.RpcService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RpcService
public class InfoUserServiceImpl implements InfoUserService {
    private Map<String, InfoUser> map = new HashMap<>();

    @Override
    public List<InfoUser> insertInfoUser(InfoUser infoUser) {
        map.put(infoUser.getId(), infoUser);
        return map.values().stream().collect(Collectors.toList());
    }

    @Override
    public InfoUser getInfoUser(String id) {
        return map.get(id);
    }

    @Override
    public void deleteInfoUser(String id) {
        map.remove(id);
    }

    @Override
    public Map<String, InfoUser> listAllInfoUser() {
        return map;
    }
}
