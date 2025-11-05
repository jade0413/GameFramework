package com.yp.gameframwrok.engine.core;


/**
 * 业务异常
 *
 * @author ruoyi
 */
public final class ServiceException extends BaseException {
    private static final long serialVersionUID = 1L;

//    /**
//     * 错误码
//     */
//    private Integer code;

//    /**
//     * 错误提示
//     */
//    private String message;

//    /**
//     * 错误明细，内部调试错误
//     * <p>
//     * 和 {@link CommonResult#getDetailMessage()} 一致的设计
//     */
//    private String detailMessage;

//    /**
//     * 错误消息
//     */
//    private String defaultMessage;

//    /**
//     * 错误码对应的参数
//     */
//    private Object[] args;

    public ServiceException(String module, String code, Object[] args, String defaultMessage) {
        super(module, code, args, defaultMessage);
    }

    public ServiceException(String module, String code, Object[] args) {
        super(module, code, args);
    }


    public ServiceException(String module, String defaultMessage) {
        super(module, defaultMessage);
    }

    public ServiceException(String code, Object[] args) {
        super(code, args);
    }

    public ServiceException(String defaultMessage) {
        super(defaultMessage);
    }

    public ServiceException(Integer code, Object... args) {
        super(null, code.toString(), args, null);
    }


    public ServiceException(String message, Integer code) {
        super(null, code.toString(), null, null);
        setMessage(message);
    }

}