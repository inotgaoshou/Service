package com.service.zgbj.controller;

import com.service.zgbj.mysqlTab.impl.HistoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName HistoryController.java
 * @Description TODO
 * @createTime 2020年11月20日 12:20:00
 */
@RestController
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private HistoryServiceImpl service;

    @RequestMapping("/user")
    public String getHistoryUser(HttpServletRequest req){

        String uid = req.getParameter("uid");
        String conversation = req.getParameter("conversation");
        String no = req.getParameter("pageNo");
        String size = req.getParameter("pageSize");
        String chatMessage = service.getChatMessage(uid, conversation, Integer.valueOf(no), Integer.valueOf(size));
        return chatMessage;
    }
}
