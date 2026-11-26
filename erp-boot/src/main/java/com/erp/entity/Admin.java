package com.erp.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员账号，对应 admin 表。
 */
@Data
public class Admin {

    /** 主键 */
    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码，BCrypt 密文；历史明文账号首次登录后自动升级 */
    private String password;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 头像 URL */
    private String adminAvatar;

    /** 状态：1=正常 0=停用 */
    private Integer status;

    /** 软删除：0=正常 1=已删除 */
    private Integer delFlag;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
