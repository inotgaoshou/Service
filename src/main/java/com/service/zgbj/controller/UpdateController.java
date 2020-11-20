package com.service.zgbj.controller;

import com.service.zgbj.mysqlTab.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName UpdateController.java
 * @Description TODO
 * @createTime 2020年11月20日 12:44:00
 */
@RestController
@RequestMapping("/update")
public class UpdateController {

    @Autowired
    private UserServiceImpl service;

    @RequestMapping("/user")
    public String updateUser(HttpServletRequest req){
        String updateUser = service.updateUser(req.getParameterMap());
        return updateUser;
    }
}
