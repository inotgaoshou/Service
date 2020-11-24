package com.service.zgbj.controller;

import com.service.zgbj.im.BaseResponse;
import com.service.zgbj.im.ChatMessage;
import com.service.zgbj.im.SocketManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName RoomController.java
 * @Description TODO
 * @createTime 2020年11月24日 22:40:00
 */
@Slf4j
@RestController
@RequestMapping("/api/room/")
public class RoomController {


    @RequestMapping("test")
    public String test(){
        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setFromId("12");
        chatMessage.setToId("45");
        SocketManager.publishImMessage(chatMessage);
        return "success";
    }
}
