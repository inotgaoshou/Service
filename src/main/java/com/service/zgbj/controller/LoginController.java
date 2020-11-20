package com.service.zgbj.controller;

import com.service.zgbj.mysqlTab.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName LoginController.java
 * @Description TODO
 * @createTime 2020年11月20日 11:35:00
 */
@RestController
@RequestMapping("/Lg")
public class LoginController {

    @Autowired
    private UserServiceImpl service;

    @RequestMapping("/user")
    public String login(HttpServletRequest req){
        String mobile = req.getParameter("mobile");
        String pwd = req.getParameter("password");
        String login = service.Login(mobile, pwd);
        return login;
    }
}
