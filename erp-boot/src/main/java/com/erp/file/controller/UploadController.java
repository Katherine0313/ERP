package com.erp.file.controller;

import com.erp.common.result.Result;
import com.erp.file.service.UploadService;
import com.erp.file.vo.UploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 后台文件上传接口；返回相对路径，前端按 /image/** 访问。 */
@Tag(name = "后台文件上传")
@RestController
@RequestMapping("/api/admin/upload")
public class UploadController {
    /** 按实现约定用 @Resource 字段注入，不用构造器注入 */
    @Resource
    private UploadService uploadService;

    /** 上传图片（png/jpg/gif/webp，≤5MB）或视频（mp4，≤50MB）；type 决定存储子目录，可省略 */
    @Operation(summary = "上传图片/视频")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UploadVO> upload(@RequestParam(value = "file", required = false) MultipartFile file,
                                   @RequestParam(required = false) String type) {
        return Result.success(uploadService.upload(file, type));
    }
}
