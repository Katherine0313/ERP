package com.erp.common.result;

import lombok.Data;

/**
 * 统一返回体。所有接口都返回这个结构，前端按 code 判断成败。
 *
 * <p>约定：code 与 HTTP 状态码保持一致（200 成功、400 参数错误、401 未认证、
 * 403 无权限、500 服务端错误），前端既可以看 HTTP 状态码，也可以只看 code。
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

    /** 业务状态码，与 HTTP 状态码一致 */
    private Integer code;

    /** 提示信息，成功时为 success */
    private String msg;

    /** 业务数据，失败时为 null */
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
