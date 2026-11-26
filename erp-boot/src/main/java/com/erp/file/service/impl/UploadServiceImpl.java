package com.erp.file.service.impl;

import com.erp.common.exception.BusinessException;
import com.erp.file.service.UploadService;
import com.erp.file.vo.UploadVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传服务：校验通过后按业务类型分目录存到项目根目录的 upload/ 下，
 * 返回前端可直接访问的相对路径（/image/子目录/文件名）。
 */
@Service
public class UploadServiceImpl implements UploadService {

    /** 大小上限：图片 5MB、视频 50MB（与 application.yml 的 multipart 限制保持一致） */
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 50L * 1024 * 1024;

    /** 允许的媒体类型 -> 文件后缀，只放行白名单内的类型 */
    private static final Map<String, String> ALLOWED_MEDIA_TYPES = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/gif", "gif",
            "image/webp", "webp",
            "video/mp4", "mp4"
    );

    /** 业务类型 -> 存储子目录；未登记的类型统一放 upload 目录，避免 type 直接参与路径拼接 */
    private static final Map<String, String> TYPE_SUBDIR = Map.of(
            "admin-login", "admin-login"
    );

    /** 存储根目录（相对程序运行目录） */
    private static final String UPLOAD_ROOT = "upload";
    /** 对外 URL 前缀：需要在 WebMvc 配置中把 /image/** 映射到 upload/ 目录 */
    private static final String URL_PREFIX = "/image/";

    @Override
    public UploadVO upload(MultipartFile file, String type) {
        // 1. 空文件校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请选择要上传的文件");
        }

        // 2. 类型校验：Content-Type 归一化后按白名单取后缀
        String contentType = normalizeContentType(file.getContentType());
        String suffix = contentType == null ? null : ALLOWED_MEDIA_TYPES.get(contentType);
        if (suffix == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "只支持 png/jpg/gif/webp 图片或 mp4 视频");
        }

        // 3. 大小校验：按图片 / Video 分别限流
        boolean isVideo = "mp4".equals(suffix);
        long maxBytes = isVideo ? MAX_VIDEO_BYTES : MAX_IMAGE_BYTES;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(HttpStatus.PAYLOAD_TOO_LARGE,
                    isVideo ? "视频不能超过50MB" : "图片不能超过5MB");
        }

        // 4. 文件头校验：防止伪造 Content-Type 上传非图片 / 视频内容
        if (!hasValidHeader(file, suffix)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "文件内容与声明的类型不符");
        }

        // 5. 确定子目录并确保目录存在（并发下 mkdirs 可能失败，再判断一次 exists）
        String sub = (type == null || type.isBlank()) ? "upload" : TYPE_SUBDIR.getOrDefault(type, "upload");
        File dir = new File(UPLOAD_ROOT, sub).getAbsoluteFile();
        if (!dir.exists() && !dir.mkdirs() && !dir.exists()) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "存储目录不可用，请联系管理员");
        }

        // 6. UUID 文件名，避免特殊字符和同名覆盖；绝对路径确保写到 upload/ 而非容器临时目录
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + suffix;
        File target = new File(dir, filename).getAbsoluteFile();
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "文件保存失败");
        }
        if (!target.isFile() || target.length() == 0) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "文件保存失败");
        }

        // 7. 返回相对路径，前端按 /image/** 访问
        return UploadVO.builder().url(URL_PREFIX + sub + "/" + filename).build();
    }

    /** 归一化 Content-Type：转小写并去掉「;charset」等参数，便于和白名单比对；空值原样返回 */
    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        int semicolon = normalized.indexOf(';');
        return semicolon > 0 ? normalized.substring(0, semicolon).trim() : normalized;
    }

    /** 读前 12 个字节核对文件头，确认内容与后缀一致 */
    private boolean hasValidHeader(MultipartFile file, String suffix) {
        try (InputStream in = file.getInputStream()) {
            byte[] head = new byte[12];
            int n = in.read(head);
            if (n < 3) {
                return false;
            }
            return switch (suffix) {
                // PNG: 89 50 4E 47 0D 0A 1A 0A
                case "png" -> n >= 8 && head[0] == (byte) 0x89 && head[1] == 'P'
                        && head[2] == 'N' && head[3] == 'G' && head[4] == 0x0D
                        && head[5] == 0x0A && head[6] == 0x1A && head[7] == 0x0A;
                // JPEG: FF D8 FF
                case "jpg" -> (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8
                        && (head[2] & 0xFF) == 0xFF;
                // GIF: GIF87a / GIF89a
                case "gif" -> n >= 6 && head[0] == 'G' && head[1] == 'I' && head[2] == 'F';
                // WebP: 前 4 字节 RIFF，第 8~11 字节 WEBP
                case "webp" -> n >= 12 && ascii(head, 0, 4).equals("RIFF") && ascii(head, 8, 4).equals("WEBP");
                // MP4: 第 4~7 字节 ftyp
                case "mp4" -> n >= 8 && ascii(head, 4, 4).equals("ftyp");
                default -> false;
            };
        } catch (IOException e) {
            return false;
        }
    }

    /** 按 ASCII 取出文件头中指定区间的内容 */
    private String ascii(byte[] bytes, int from, int len) {
        return new String(bytes, from, len, StandardCharsets.US_ASCII);
    }
}
