package io.github.yeyuhl.malltiny.common.exception;

import io.github.yeyuhl.malltiny.common.response.IErrorCode;

/**
 * 断言处理类，用于抛出各种API异常
 *
 * @author yeyuhl
 * @date 2023/4/22
 */

public class Asserts {
    public static void fail(String message) {
        throw new ApiException(message);
    }

    public static void fail(IErrorCode errorCode) {
        throw new ApiException(errorCode);
    }
}
