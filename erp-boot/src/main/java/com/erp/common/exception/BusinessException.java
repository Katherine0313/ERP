package com.erp.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常。用于表达「可预期的失败」——账号不存在、密码错误、账号停用等。
 *
 * <p>由 {@link GlobalExceptionHandler} 统一翻译成 Result 返回，HTTP 状态码
 * 直接取这里携带的 HttpStatus。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 响应的 HTTP 状态码 */
    private final HttpStatus status;

    /** 业务状态码，与 HTTP 状态码一致 */
    private final Integer code;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.code = status.value();
    }
}
