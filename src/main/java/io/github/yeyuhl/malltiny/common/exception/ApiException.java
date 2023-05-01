package io.github.yeyuhl.malltiny.common.exception;

import io.github.yeyuhl.malltiny.common.response.IErrorCode;

/**
 * 自定义API异常
 *
 * @author yeyuhl
 * @date 2023/4/22
 */
public class ApiException extends RuntimeException {
    private IErrorCode errorCode;

    public ApiException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public IErrorCode getErrorCode() {
        return errorCode;
    }
}
