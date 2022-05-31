package com.example.callbackWithMysql.common.controller;

import com.example.callbackWithMysql.common.model.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

/**
 * User: York
 * Date: 2021/12/28
 * Time: 11:50
 */

@RestController
public class BaseController {
    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
    * 返回成功Response
    *
    * @return
    */
    public APIResponse success() {
        logger.info("请求结束 : success");
        APIResponse res = new APIResponse();
        res.success();
        return res;
    }

    /**
     * 返回错误Response
     *
     * @param errmsg
     * @return
     */
    public APIResponse fail(String errmsg) {
        logger.error("请求结束:  fail: " + errmsg);
        APIResponse res = new APIResponse();
        res.fail(errmsg);
        return res;
    }

    /**
     * 接收成功，存入数据库错误Response
     *
     * @param errmsg
     * @return
     */
    public APIResponse success_failToSave(String errmsg) {
        logger.error("请求结束:  fail: " + errmsg);
        APIResponse res = new APIResponse();
        res.success_failToSave(errmsg);
        return res;
    }
}
