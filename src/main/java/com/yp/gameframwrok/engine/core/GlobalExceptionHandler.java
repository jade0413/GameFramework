package com.yp.gameframwrok.engine.core;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

import com.yp.gameframwrok.web.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * 全局异常处理器
 *
 * @author ruoyi
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {



    /**
     * 权限码异常
     */
//    @ExceptionHandler(NotPermissionException.class)
//    public AjaxResult handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求地址'{}',权限码校验失败'{}'", requestURI, e.getMessage());
//        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有访问权限，请联系管理员授权");
//    }
//
//    /**
//     * 角色权限异常
//     */
//    @ExceptionHandler(NotRoleException.class)
//    public AjaxResult handleNotRoleException(NotRoleException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求地址'{}',角色权限校验失败'{}'", requestURI, e.getMessage());
//        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有访问权限，请联系管理员授权");
//    }
//
//    /**
//     * 请求方式不支持
//     */
//    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
//    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
//        return AjaxResult.error(e.getMessage());
//    }
//
//
//    /**
//     * 业务异常
//     */
//    @ExceptionHandler(IllegalStateException.class)
//    public AjaxResult handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
//        log.error(e.getMessage(), e);
//        return AjaxResult.error(e.getMessage());
//    }
//
    /**
     * 业务异常
     */
    @ExceptionHandler({ServiceException.class})
    public Result<Object> handleServiceException(BaseException e, HttpServletRequest request) {
        String code = e.getCode();
        log.error(e.getMessage(), e);
        return StringUtils.isNotEmpty(code) ? Result.fail(Integer.parseInt(code), e.getMessage()) : Result.fail(e.getMessage());
    }
//
//
//    /**
//     * 请求路径中缺少必需的路径变量
//     */
//    @ExceptionHandler(MissingPathVariableException.class)
//    public AjaxResult handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI, e);
//        return AjaxResult.error(String.format("请求路径中缺少必需的路径变量[%messages.properties]", e.getVariableName()));
//    }
//
//    /**
//     * 请求参数类型不匹配
//     */
//    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
//    public AjaxResult handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        String value = Convert.toStr(e.getValue());
//        if (StringUtils.isNotEmpty(value)) {
//            value = EscapeUtil.clean(value);
//        }
//        log.error("请求参数类型不匹配'{}',发生系统异常.", requestURI, e);
//        return AjaxResult.error(String.format("请求参数类型不匹配，参数[%messages.properties]要求类型为：'%messages.properties'，但输入值为：'%messages.properties'", e.getName(), e.getRequiredType().getName(), value));
//    }
//
//    /**
//     * 拦截未知的运行时异常
//     */
//    @ExceptionHandler(RuntimeException.class)
//    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求地址'{}',发生未知异常.", requestURI, e);
//        return AjaxResult.error(MessageUtils.message(CommonExceptionCode.COMMON_ERROR));
//    }
//
//    /**
//     * 系统异常
//     */
//    @ExceptionHandler(Exception.class)
//    public AjaxResult handleException(Exception e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求地址'{}',发生系统异常.", requestURI, e);
//        return AjaxResult.error(MessageUtils.message(CommonExceptionCode.COMMON_ERROR));
//    }
//
//    /**
//     * 自定义验证异常
//     */
//    @ExceptionHandler(BindException.class)
//    public AjaxResult handleBindException(BindException e) {
//        log.error(e.getMessage(), e);
//        String message = e.getAllErrors().get(0).getDefaultMessage();
//        return AjaxResult.error(message);
//    }
//
//    /**
//     * 自定义验证异常
//     */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
//        log.error(e.getMessage(), e);
//        String message = e.getBindingResult().getFieldError().getDefaultMessage();
//        return AjaxResult.error(message);
//    }
//
//    /**
//     * 内部认证异常
//     */
//    @ExceptionHandler(InnerAuthException.class)
//    public AjaxResult handleInnerAuthException(InnerAuthException e) {
//        return AjaxResult.error(e.getMessage());
//    }
//
//    /**
//     * 演示模式异常
//     */
//    @ExceptionHandler(DemoModeException.class)
//    public AjaxResult handleDemoModeException(DemoModeException e) {
//        return AjaxResult.error("演示模式，不允许操作");
//    }
//
//    @ExceptionHandler(MultipartException.class)
//    public R<Void> handleMaxUploadSizeExceededException(MultipartException ex) {
//        if (ex instanceof MaxUploadSizeExceededException) {
//            return R.fail("文件过大");
//            // 专门处理文件大小超限
//        }
//        return R.fail("文件处理失败");
//    }
}
