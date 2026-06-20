package com.tzy.dto;

/**
 * 通用响应结果类
 */
public class ResponseResult {

    /**
     * 成功状态码
     */
    private static final int SUCCESS_CODE = 200;
    /**
     * 错误状态码
     */
    private static final int ERROR_CODE = 500;
    /**
     * 状态码
     */
    private int code;
    /**
     * 消息
     */
    private String msg;
    /**
     * 数据
     */
    private Object data;

    public ResponseResult() {
    }

    public ResponseResult(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应
     *
     * @return 响应结果
     */
    public static ResponseResult success() {
        return new ResponseResult(SUCCESS_CODE, "操作成功", null);
    }

    /**
     * 成功响应
     *
     * @param msg 消息
     * @return 响应结果
     */
    public static ResponseResult success(String msg) {
        return new ResponseResult(SUCCESS_CODE, msg, null);
    }

    /**
     * 成功响应
     *
     * @param data 数据
     * @return 响应结果
     */
    public static ResponseResult success(Object data) {
        return new ResponseResult(SUCCESS_CODE, "操作成功", data);
    }

    /**
     * 成功响应
     *
     * @param msg 消息
     * @param data 数据
     * @return 响应结果
     */
    public static ResponseResult success(String msg, Object data) {
        return new ResponseResult(SUCCESS_CODE, msg, data);
    }

    /**
     * 错误响应
     *
     * @return 响应结果
     */
    public static ResponseResult error() {
        return new ResponseResult(ERROR_CODE, "操作失败", null);
    }

    /**
     * 错误响应
     *
     * @param msg 消息
     * @return 响应结果
     */
    public static ResponseResult error(String msg) {
        return new ResponseResult(ERROR_CODE, msg, null);
    }

    /**
     * 错误响应
     *
     * @param code 状态码
     * @param msg 消息
     * @return 响应结果
     */
    public static ResponseResult error(int code, String msg) {
        return new ResponseResult(code, msg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
