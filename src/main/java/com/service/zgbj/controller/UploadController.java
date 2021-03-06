package com.service.zgbj.controller;

import com.service.zgbj.mysqlTab.impl.FileImpl;
import com.service.zgbj.utils.GsonUtil;
import com.service.zgbj.utils.OSSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName UploadController.java
 * @Description TODO
 * @createTime 2020年11月20日 12:30:00
 */
@RestController
@RequestMapping("/uploadFile")
public class UploadController {

    @Autowired
    private FileImpl fileService;

    public void upload(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
        HashMap<String, Object> statusMap = new HashMap<>();


        //将当前上下文初始化给  CommonsMutipartResolver （多部分解析器）
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(req.getSession().getServletContext());


        // 判断是否是多数据段提交格式
        if (multipartResolver.isMultipart(req)) {
            MultipartHttpServletRequest multiRequest = multipartResolver.resolveMultipart(req);
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                MultipartFile mf = multiRequest.getFile(iter.next());
                String fileName = mf.getOriginalFilename();
                System.out.print("filename---:" + fileName);
                if (fileName == null || fileName.trim().equals("")) {
                    continue;
                }
                String fileType = fileName.substring(fileName.lastIndexOf('.'));
                int type = OSSUtil.getFileType(fileType);
                System.out.print("---文件后缀名----" + fileType + "\n");
                File newFile = new File(fileName);
                FileOutputStream os = new FileOutputStream(newFile);
                os.write(mf.getBytes());
                os.close();
                mf.transferTo(newFile);
                //上传到OSS
                String uploadUrl = OSSUtil.upload(newFile, type);
                if (null != uploadUrl && !uploadUrl.isEmpty()) {
                    statusMap.put("code", 1);
                    statusMap.put("msg", "成功");
                    statusMap.put("url", uploadUrl);
                    if (type == OSSUtil.TYPE_PIC) {
                        fileService.addOrUpdateFileUrl(fileName, uploadUrl);
                    }
                } else {
                    statusMap.put("code", 0);
                    statusMap.put("msg", "失败");
                    statusMap.put("url", null);
                }
                resp.getWriter().write(GsonUtil.BeanToJson(statusMap));
            }
        }
    }
}
