package com.qiao.controller;

import com.qiao.bean.InfoUser;
import com.qiao.service.InfoUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RpcClientController {

    @ResponseBody
    @RequestMapping("addData")
    public String addData() {
        InfoUserService infoUserService = null;
        InfoUser user = new InfoUser();
        user.setEmail("woqiaoxun@gmail.com");
        user.setId("111");
        user.setName("Joeu");
        infoUserService.insertInfoUser(user);
        return "add data";
    }

}
