package com.longxian.watermark.controller;

import com.longxian.watermark.model.ImageInfo;
import com.longxian.watermark.service.ImageUploadService;
import com.longxian.watermark.service.ImageWatermarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
public class WatermarkController {

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private ImageWatermarkService watermarkService;

    @RequestMapping(value = "/watermarktest", method = RequestMethod.POST)
    public ImageInfo watermarkTest(@RequestParam("file") MultipartFile image ) {

        ImageInfo imgInfo = new ImageInfo();

        String uploadPath = "static/images";  // 服务器上上传文件的相对路径
        String physicalUploadPath = this.getClass().getClassLoader().getResource("").getPath();  // 服务器上上传文件的物理路径

        String imageURL = imageUploadService.uploadImage( image, uploadPath, physicalUploadPath+uploadPath );
        File imageFile = new File(physicalUploadPath +uploadPath+"/"+ image.getOriginalFilename() );

        String watermarkAddImageURL = watermarkService.watermarkAdd(imageFile, image.getOriginalFilename(), uploadPath, physicalUploadPath);

        imgInfo.setImageUrl(imageURL);
        imgInfo.setLogoImageUrl(watermarkAddImageURL);
        return imgInfo;
    }
}