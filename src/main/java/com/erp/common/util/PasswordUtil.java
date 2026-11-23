package com.erp.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具。基于 BCrypt。
 *
 * <p>兼容历史明文账号：库里若存的是明文，{@link #matches} 做明文比对，
 * 登录成功后由调用方用 {@link #encode} 覆盖成 BCrypt 密文。
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** 生成 BCrypt 密文 */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码。
     *
     * @param rawPassword    用户提交的明文
     * @param storedPassword 库里存的密码（BCrypt 密文或历史明文）
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isEncoded(storedPassword)) {
            return ENCODER.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    /** 判断库里存的是否已经是 BCrypt 密文（BCrypt 密文以 $2 开头） */
    public static boolean isEncoded(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith("$2");
    }
}
