package com.service.zgbj.controller;

import com.service.zgbj.mysqlTab.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName FriendCronller.java
 * @Description TODO
 * @createTime 2020年11月20日 11:35:00
 */
@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private UserServiceImpl service;

    @RequestMapping("/addFriendMsg")
    public String addFriendMsg(HttpServletRequest req){
        String from_id = req.getParameter("from_id");
        String to_id = req.getParameter("to_id");
        String pid = req.getParameter("pid");
        int friend_type = Integer.parseInt(req.getParameter("friend_type"));
        int source = Integer.parseInt(req.getParameter("source"));
        String content = req.getParameter("content");
        String friendMsg = service.addFriendMsg(to_id, from_id, pid, friend_type, source, content);
        return friendMsg;
    }

    @RequestMapping("/addFriend")
    public String addFriend(HttpServletRequest req){
        String from_id = req.getParameter("from_id");
        String to_id = req.getParameter("to_id");
        String pid = req.getParameter("pid");
        int friend_type = Integer.parseInt(req.getParameter("friend_type"));
        int source = Integer.parseInt(req.getParameter("source"));
        String addFriend = service.addFriend(to_id, from_id, pid, friend_type, source);
        return addFriend;
    }

    @RequestMapping("/allFriendMsg")
    public String allFriendMsg(HttpServletRequest req){
        String allFriendMsg = service.getAllFriendMsg(req.getParameter("uid"));
        return allFriendMsg;
    }

    @RequestMapping("/allFriend")
    public String allFriend(HttpServletRequest req){
        String allFriend = service.getAllFriend(req.getParameter("uid"));
        return allFriend;
    }

    @RequestMapping("/findFriend")
    public String findFriend(HttpServletRequest req){
        String phone = req.getParameter("phone");
        String uid = req.getParameter("uid");
        String userInfoPhone = service.getUserInfoPhone(uid, phone);
        return userInfoPhone;
    }

    @RequestMapping("/info")
    public String info(HttpServletRequest req){
        String id = req.getParameter("id");
        String uid = req.getParameter("uid");
        String friendUserInfo = service.getFriendUserInfo(id, uid);
        return friendUserInfo;
    }

    @RequestMapping("/allAddFriendCount")
    public String allAddFriendCount(HttpServletRequest req){
        String allAddFriendMsg = service.getAllAddFriendMsg(req.getParameter("uid"));
        return allAddFriendMsg;
    }



}
