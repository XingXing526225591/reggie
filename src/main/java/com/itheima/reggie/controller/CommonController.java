package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String path;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是一个零时文件，需要转存，否则本次请求完成后临时文件会消失
        log.info(file.toString());
        //获取文件名
        String originalFilename = file.getOriginalFilename();
        //生成随机文件名
        String uuid = UUID.randomUUID().toString();

        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileName =  uuid + substring;
        //创建一个目录对象
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdir();
        }
        try {
            //将临时文件转存到特定地址
            file.transferTo(new File(path + File.separator + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        //输入流，通过输入流读取文件内容
        try {
            FileInputStream fileInputStream = new FileInputStream( path + File.separator + name);
            //输出流，通过输出流将文件回写回浏览器

            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

           int len = 0;
           byte[] bytes = new byte[1021];
           while ((len = fileInputStream.read(bytes)) != -1){
               outputStream.write(bytes,0,len);
               outputStream.flush();
           }
           //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
