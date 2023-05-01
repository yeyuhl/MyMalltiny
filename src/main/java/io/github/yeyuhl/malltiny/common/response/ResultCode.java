package io.github.yeyuhl.malltiny.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 常用响应码，对错误码的包装
 *
 * @author yeyuhl
 * @date 2023/4/22
 */

@Getter
@AllArgsConstructor
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限");
    private final long code;
    private final String message;
}
