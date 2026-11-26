package com.erp.file.service;

import com.erp.file.vo.UploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    UploadVO upload(MultipartFile file, String Type);
}
