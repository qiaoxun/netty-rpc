package com.qiao.controller;

import com.qiao.bean.InfoUser;
import com.qiao.factory.RpcServiceFactory;
import com.qiao.service.InfoUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Proxy;

@Controller
public class RpcClientController {

    @Autowired
    private RpcServiceFactory rpcServiceFactory;

    @ResponseBody
    @RequestMapping("/addData")
    public String addData() {
        InfoUserService infoUserService = (InfoUserService)Proxy.newProxyInstance(InfoUserService.class.getClassLoader(), new Class[]{InfoUserService.class}, rpcServiceFactory);
        InfoUser user = new InfoUser();
        user.setEmail("woqiaoxun@gmail.com");
        user.setId("111");
        user.setName("Joeu");
        infoUserService.insertInfoUser(user);

        return "add data";
    }

}
