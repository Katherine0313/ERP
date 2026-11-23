package com.erp.service.impl;

import com.erp.common.exception.BusinessException;
import com.erp.dto.AdminLoginDTO;
import com.erp.entity.Admin;
import com.erp.mapper.AdminMapper;
import com.erp.service.AdminLoginService;
import com.erp.utils.JwtUtil;
import com.erp.utils.PasswordUtil;
import com.erp.vo.AdminLoginVO;
import com.erp.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员登录。
 *
 * <p>流程：参数校验 → 查账号 → 校验密码 → 拦截停用账号 → 签发 JWT。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLoginServiceImpl implements AdminLoginService {

    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;

    @Override
    public AdminLoginVO login(AdminLoginDTO dto, String clientIp, String userAgent) {
        // 1. 参数校验：用户名/密码都不能为空
        if (dto == null || dto.getUsername() == null || dto.getUsername().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名和密码不能为空");
        }
        String username = dto.getUsername().trim();

        // 2. 按用户名查管理员
        Admin admin = adminMapper.findByUsername(username);
        // BCrypt 密文走 matches，历史明文账号首次登录成功后自动升级为 BCrypt。
        // 查不到和密码错统一回同一句话，不暴露账号是否存在。
        if (admin == null || !PasswordUtil.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        // 3. 账号停用拦截（status=0；已软删的账号 findByUsername 查不到，走不到这里）
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "账号已停用");
        }

        // 4. 历史明文密码升级为 BCrypt（失败不影响本次登录）
        if (!PasswordUtil.isEncoded(admin.getPassword())) {
            try {
                adminMapper.updatePassword(admin.getId(), PasswordUtil.encode(dto.getPassword()),
                        LocalDateTime.now());
            } catch (Exception e) {
                log.warn("密码升级为 BCrypt 失败，不影响本次登录: {}", e.getMessage());
            }
        }

        // 5. 签发令牌并组装登录响应
        // roles / permissions 暂空（RBAC 未接入），sid 暂空（未接在线会话）
        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername(),
                List.of(), List.of(), null, admin.getAdminAvatar());
        return AdminLoginVO.builder()
                .token(token)
                .user(AdminUserVO.builder()
                        .id(admin.getId())
                        .username(admin.getUsername())
                        .name(admin.getName())
                        .phone(admin.getPhone())
                        .adminAvatar(admin.getAdminAvatar())
                        .createTime(admin.getCreateTime())
                        .build())
                .build();
    }
}
