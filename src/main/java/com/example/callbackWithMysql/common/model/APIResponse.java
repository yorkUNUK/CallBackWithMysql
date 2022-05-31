package com.example.callbackWithMysql.common.model;

/**
 * User: York
 * Date: 2021/12/28
 * Time: 11:50
 */

public class APIResponse {
    // 信息代码
    private String errno;

    // 返回信息
    private String errmsg;

    // 返回的code
    private String code;

    // 返回的message
    private String message;

    /**
     * 返回错误信息
     */

    public void fail(String errmsg) {
        this.errno = "777";
        this.errmsg = errmsg;
    }

    /**
     * 接收成功，存入数据库错误
     */

    public void success_failToSave(String errmsg) {
        this.code = "200";
        this.message = "success";
        this.errno = "777";
        this.errmsg = errmsg;
    }

    /**
     * 返回成功信息
     */
    public void success() {
        this.code = "200";
        this.message = "success";
    }

    public String getErrno() {return errno;}

    public void setErrno(String errno) {this.errno = errno;}

    public String getErrmsg() {return errmsg;}

    public void setErrmsg(String errmsg) {this.errmsg = errmsg;}

    public String getCode() {return code;}

    public void setCode(String code) {this.code = code;}

    public String getMessage() {return message;}

    public void setMessage(String message) {this.message = message;}
}
