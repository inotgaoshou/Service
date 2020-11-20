package com.service.zgbj.controller;

import com.service.zgbj.mysqlTab.impl.HistoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName ChatController.java
 * @Description TODO
 * @createTime 2020年11月20日 11:45:00
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private HistoryServiceImpl service;

    @RequestMapping("/getConversation")
    public String getConversation(HttpServletRequest req){
        String fromId = req.getParameter("fromId");
        String toId = req.getParameter("toId");
        String conversation = service.getConversation(fromId, toId);
        return conversation;
    }
}
