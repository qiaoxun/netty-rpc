package com.qiao.service;

import com.qiao.bean.InfoUser;

import java.util.List;
import java.util.Map;

public interface InfoUserService {
    List<InfoUser> insertInfoUser(InfoUser infoUser);
    InfoUser getInfoUser(String id);
    void deleteInfoUser(String id);
    Map<String, InfoUser> listAllInfoUser();
}
