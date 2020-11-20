package com.service.zgbj.controller;

import com.service.zgbj.mysqlTab.impl.HistoryServiceImpl;
import com.service.zgbj.mysqlTab.impl.UserServiceImpl;
import com.service.zgbj.utils.OfTenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName RegisterController.java
 * @Description TODO
 * @createTime 2020年11月20日 12:27:00
 */
@RestController
@RequestMapping("/rgs")
public class RegisterController {

    @Autowired
    private UserServiceImpl service;
    @Autowired
    private HistoryServiceImpl historyService;

    @RequestMapping("/user")
    public String register(HttpServletRequest req){
        String email = req.getParameter("email");
        String imageUrl = req.getParameter("imageUrl");
        String location = req.getParameter("location");
        String mobile = req.getParameter("mobile");
        String pwd = req.getParameter("password");
        String sex = req.getParameter("sex");
        String name = req.getParameter("name");
        String randomUid = OfTenUtils.getRandomUid();
        historyService.createTable(randomUid);
        service.createTableFriend(randomUid);
        service.createTableFriendMsg(randomUid);
        String register = service.register(email, imageUrl, location, mobile, pwd, sex, name, randomUid);
        return register;
    }
}
