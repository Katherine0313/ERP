package com.erp.controller;

import com.erp.common.result.Result;
import com.erp.dto.AdminLoginDTO;
import com.erp.service.AdminLoginService;
import com.erp.vo.AdminLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台（管理系统）登录接口。
 *
 * <p>路径按端划分，管理端走 /api/admin/**，App 端后续走 /api/app/**，
 * 两个前端的接口不会互相撞车。
 */
@Tag(name = "后台登录", description = "管理员登录")
@RequestMapping("/api/admin")
@RestController
public class AdminLoginController {

    private final AdminLoginService adminLoginService;

    public AdminLoginController(AdminLoginService adminLoginService) {
        this.adminLoginService = adminLoginService;
    }

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO dto, HttpServletRequest request) {
        return Result.success(adminLoginService.login(dto, resolveClientIp(request),
                request.getHeader("User-Agent")));
    }

    /** 取客户端 IP：优先取代理透传的 X-Forwarded-For 第一段 */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
