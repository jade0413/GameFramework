package com.yp.gameframwrok.exception;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author yyp
 */
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 所属模块
     */
    @Setter
    private String module;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 错误消息
     */
    private String defaultMessage;

    @Getter
    @Setter
    private String message;

    public BaseException(String module, String code, Object[] args, String defaultMessage) {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    public BaseException(String module, String code, Object[] args) {
        this(module, code, args, null);
    }

    public BaseException(String module, String defaultMessage) {
        this(module, null, null, defaultMessage);
    }

    public BaseException(String code, Object[] args) {
        this(null, code, args, null);
    }

    public BaseException(String defaultMessage) {
        this(null, null, null, defaultMessage);
    }

    public String getModule() {
        return module;
    }

    public String getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }


    @Override
    public String getMessage() {
        String resultMessage = this.message;
        if (resultMessage == null) {
//            if (!StringUtils.isEmpty(code)) {
//                resultMessage = MessageUtils.message(code, args);
//            }
            if (resultMessage == null) {
                resultMessage = defaultMessage;
            }
        }
        return resultMessage;
    }

}
