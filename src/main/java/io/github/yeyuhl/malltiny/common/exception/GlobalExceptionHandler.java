package io.github.yeyuhl.malltiny.common.exception;

import io.github.yeyuhl.malltiny.common.response.CommonResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局的异常处理器
 * 不同的参数解析方式，Spring抛出的异常也不同，因此有些异常需要手动处理
 *
 * @author yeyuhl
 * @date 2023/4/22
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义的API异常
     */
    @ExceptionHandler(ApiException.class)
    public CommonResult handle(ApiException e) {
        if (e.getErrorCode() != null) {
            return CommonResult.failed(e.getErrorCode());
        }
        return CommonResult.failed(e.getMessage());
    }

    /**
     * 处理请求体绑定到bean上失败时抛出的异常（即带有@Valid注解的参数验证失败时抛出该异常）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        return CommonResult.validateFailed(message);
    }

    /**
     * 处理请求参数绑定到bean上失败时抛出的异常（参数校验不通过）
     */
    @ExceptionHandler(BindException.class)
    public CommonResult handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        return CommonResult.validateFailed(message);
    }
}
