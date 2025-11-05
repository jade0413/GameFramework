package com.yp.gameframwrok.web.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @author Lion Li
 */
@Data
@NoArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    private static final HttpStatus SUCCESS = HttpStatus.OK;

    /**
     * 失败
     */
    private static final HttpStatus FAIL = HttpStatus.INTERNAL_SERVER_ERROR;

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 数据对象
     */
    private T data;

    public static <T> Result<T> success() {
        return restResult(null, SUCCESS.value(), SUCCESS.getReasonPhrase());
    }

    public static <T> Result<T> success(T data) {
        return restResult(data, SUCCESS.value(), "success");
    }

    public static <T> Result<T> success(String msg) {
        return restResult(null, SUCCESS.value(), msg);
    }

    public static <T> Result<T> success(String msg, T data) {
        return restResult(data, SUCCESS.value(), msg);
    }

    public static <T> Result<T> fail() {
        return restResult(null, FAIL.value(), "fail");
    }

    public static <T> Result<T> fail(String msg) {
        return restResult(null, FAIL.value(), msg);
    }

    public static <T> Result<T> fail(T data) {
        return restResult(data, FAIL.value(), "fail");
    }

    public static <T> Result<T> fail(String msg, T data) {
        return restResult(data, FAIL.value(), msg);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }


    private static <T> Result<T> restResult(T data, int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }

    public static <T> Boolean isError(Result<T> ret) {
        return !checkIsSuccess(ret);
    }

    public static <T> Boolean checkIsSuccess(Result<T> ret) {
        return Result.SUCCESS.value() == ret.getCode();
    }

    public Boolean checkIsSuccess() {
        return this.code == Result.SUCCESS.value();
    }

    public Boolean checkIsFail() {
        return !checkIsSuccess();
    }
}
