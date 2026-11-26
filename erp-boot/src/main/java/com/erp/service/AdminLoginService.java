package com.erp.service;

import com.erp.dto.AdminLoginDTO;
import com.erp.vo.AdminLoginVO;

public interface AdminLoginService {

    /**
     * 管理员登录。
     *
     * @param dto       登录请求（账号、密码）
     * @param clientIp  客户端 IP
     * @param userAgent 客户端 UA
     * @return 令牌与管理员信息
     */
    AdminLoginVO login(AdminLoginDTO dto, String clientIp, String userAgent);
}
