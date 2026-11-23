package com.erp.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录成功返回的管理员信息。
 */
@Data
@Builder
public class AdminUserVO {
    private Long id;
    private String username;
    private String name;
    /** 手机号（前端展示时自行脱敏） */
    private String phone;
    /** 头像 URL，未设置时为 null，前端回退默认头像 */
    private String adminAvatar;
    /** 创建时间 */
    private LocalDateTime createTime;
}
