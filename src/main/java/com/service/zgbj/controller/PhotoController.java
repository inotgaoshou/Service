package com.service.zgbj.controller;

import com.service.zgbj.mysqlTab.impl.FileImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName PhotoController.java
 * @Description TODO
 * @createTime 2020年11月20日 12:23:00
 */
@RestController
@RequestMapping("/photo")
public class PhotoController {

    @Autowired
    private FileImpl fileService;

    @RequestMapping("/likePhotoList")
    public String likePhotoList(HttpServletRequest req){
        String uid = req.getParameter("uid");
        String likePhoto = fileService.getLikePhoto(uid);
        return likePhoto;
    }



    @RequestMapping("/addLike")
    public String addLike(HttpServletRequest req){
        String uid = req.getParameter("uid");
        String url = req.getParameter("url");
        String pid = req.getParameter("pid");
        String type = req.getParameter("type");
        String addLikePhoto = fileService.addLikePhoto(uid, url, pid, Integer.valueOf(type));
        return addLikePhoto;
    }
}
