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
@RequestMapping("/updateHistory")
public class UpdateHistoryController {

    @Autowired
    private HistoryServiceImpl historyService;

    @RequestMapping("/user")
    public String getHistoryUser(HttpServletRequest req){
        String pid = req.getParameter("pid");
        String fromId = req.getParameter("uid");
        String json = historyService.updateHistoryStatus(fromId, 2, pid);
        return json;
    }
}
