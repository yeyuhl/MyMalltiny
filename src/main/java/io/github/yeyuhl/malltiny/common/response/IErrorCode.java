package io.github.yeyuhl.malltiny.common.response;

/**
 * 错误码
 *
 * @author yeyuhl
 * @date 2023/4/22
 */
public interface IErrorCode {

    long getCode();

    String getMessage();
}
