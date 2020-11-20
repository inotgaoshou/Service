package com.service.zgbj.controller;

import com.service.zgbj.mysqlTab.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName UserController.java
 * @Description TODO
 * @createTime 2020年11月20日 12:34:00
 */
@RestController
@RequestMapping("/info")
public class UserController {

    @Autowired
    private UserServiceImpl service;

    @RequestMapping("/user")
    public String user(HttpServletRequest req){
        String uid = req.getParameter("uid");
        String userInfo = service.getUserInfo(uid);
        return userInfo;
    }
}
