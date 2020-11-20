package com.service.zgbj.controller;

import com.service.zgbj.mysqlTab.impl.FileImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName FileController.java
 * @Description TODO
 * @createTime 2020年11月20日 11:50:00
 */

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileImpl fileService;

    @RequestMapping("/existFile")
    public String existFile(HttpServletRequest req){
        String fileName = req.getParameter("fileName");
        String fileUrl = fileService.getFileUrl(fileName);
        return fileUrl;
    }
}
