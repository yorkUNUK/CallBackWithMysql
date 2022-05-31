package com.example.callbackWithMysql.api.controller;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.callbackWithMysql.api.decrypt.*;
import com.example.callbackWithMysql.common.controller.BaseController;
import com.example.callbackWithMysql.common.model.APIResponse;
import com.example.callbackWithMysql.common.model.Message;
import com.example.callbackWithMysql.common.save.saveToMysql;
import com.example.callbackWithMysql.api.client.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User: York
 * Date: 2021/12/28
 * Time: 11:50
 */

@RestController
public class callback extends BaseController{

    public static final String CONTRACT_SEND_RESULT = "CONTRACT_SEND_RESULT"; //合同发送成功
    public static final String OPERATION_COMPLETE = "OPERATION_COMPLETE"; //合同被收件人签署
    public static final String CONTRACT_COMPLETE = "CONTRACT_COMPLETE"; //合同签署完成
    public static final String CONTRACT_OVERDUE = "CONTRACT_OVERDUE"; //逾期未签
    public static final String CONTRACT_REVOKE = "CONTRACT_REVOKE"; //撤销合同
    public static final String BINDING_ACCOUNT = "BINDING_ACCOUNT"; //账号绑定
    public static final String BOX_ENT_AUTH_SUCCESS = "BOX_ENT_AUTH_SUCCESS"; //档案+完成获取企业资料的授权
    public static final String BOX_CUSTOMER_AUTH_SUCCESS = "BOX_CUSTOMER_AUTH_SUCCESS"; //档案+获取到个人资料的授权
    public static final String BOX_PASS_ENT_CUSTOMER_ALL_COLLECTIONS = "BOX_PASS_ENT_CUSTOMER_ALL_COLLECTIONS"; //档案+完成自定义档案柜企业资料的全部验真
    public static final String BOX_PASS_CUSTOMER_ALL_COLLECTIONS = "BOX_PASS_CUSTOMER_ALL_COLLECTIONS"; //档案+完成自定义档案柜个人资料的全部验真

    //logback
    private final Logger logger =  LoggerFactory.getLogger(this.getClass());
    public static final Marker ultimate = MarkerFactory.getMarker("ULTIMATE");
    public static final Marker openapi = MarkerFactory.getMarker("OPENAPI");
    public static final Marker bestSign = MarkerFactory.getMarker("BESTSIGN");
    @Value("${message.token}")
    private String token;
    @Value("${message.aesKey}")
    private String aesKey;
    @Value("${message.encrypt}")
    private int message_encrypt;
    @Value("${saveToMysql}")
    private int saveToMysql;

    @Value("${sign.auto}")
    private int signAuto;
    @Value("${sign.clientId}")
    private String signClientId;
    @Value("${sign.host}")
    private String signHost;
    @Value("${sign.clientSecret}")
    private String signClientSecret;
    @Value("${sign.privateKey}")
    private String signPrivateKey;
    @Value("${sign.enterpriseName}")
    private String signEnterpriseName;
    @Value("${sign.account}")
    private String signAccount;
    @Value("${sign.bizName}")
    private String signBizName;
    @Value("${sign.sealName}")
    private String signSealName;
    @Value("${sign.pushUrl}")
    private String signPushUrl;

    @Autowired
    private saveToMysql stm;

    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    public APIResponse receiveMessageAndSign(@RequestBody JSONObject obj) {
        /**
         * 工具版异步通知
         */
        if(obj.containsKey("action")){
            logger.info(openapi, "接收到工具版异步通知：" + obj);
            return this.success();
        }
        /**
         * 旗舰版异步通知
         */
        else if(obj.containsKey("type")) {
            JSONObject responseData;
            String clientId;
            /**
             * 判断异步通知是否加密
             */
            if (obj.containsKey("encryptMsg") && (message_encrypt == 1)) {
                logger.info(ultimate, "接收到旗舰版加密的异步通知：" + obj);
                //加密的情况
                SSQAESEncrypt decrypt = SSQAESEncrypt.instanceOf(token, aesKey);
                SSQAESEncrypt.DecryptedContentAndClientId decryptedContentAndClientId = decrypt.decryptReturnValueContainsClientId(obj.getString("encryptMsg"));
                String decryptedContent = decryptedContentAndClientId.getPlanContent();
                clientId = decryptedContentAndClientId.getFromClientId();
                logger.info(ultimate, "解密后的responseData: " + decryptedContent);
                logger.info(ultimate, "解密后的开发者ID: " + clientId);

                responseData = JSONObject.parseObject(decryptedContent);
            } else if (obj.containsKey("responseData") && (message_encrypt == 0)) {
                logger.info(ultimate, "接收到旗舰版异步通知：" + obj);
                //不加密的情况
                responseData = obj.getJSONObject("responseData");
                clientId = obj.getString("clientId");
            } else {
                return this.fail("请确认异步通知的加密情况！");
            }
            /**
             * 判断是否企业自动签
             */
            if (obj.getString("type").equals(CONTRACT_SEND_RESULT)){
                logger.info(bestSign, "开始尝试自动签署");
                if (signAuto == 1 && signClientId != null) {
                    if(clientId.equals(signClientId)){
                        String contractId = String.valueOf(responseData.get("contractId"));
                        if(contractId != null) {
                            JSONObject requestBody = getRequestBody(contractId);
                            String result = sign(requestBody);
                            JSONObject resultObj = JSONObject.parseObject(result);
                            if(result != null) {
                                try {
                                    if (resultObj.getJSONObject("data").getIntValue("failureAmount") != 0) {
                                        logger.error(bestSign, resultObj.getJSONObject("data").get("errorInfos").toString());
                                    } else {
                                        logger.info(bestSign, "自动签署成功！");
                                    }
                                }catch(Exception e){
                                    logger.error(bestSign, "自动签署异常:{}", e.getLocalizedMessage() , e);
                                }
                            }else{
                                logger.error(bestSign, "自动签署异常，签署接口的response为null，请检查http code");
                            }
                        }else{
                            logger.error(bestSign, "自动签署异常，异步通知中未包含contractId信息");
                        }
                    }else{
                        logger.error(bestSign, "自动签署异常：请确认开发者ID是否正确");
                    }
                }else{
                    logger.warn(bestSign, "自动签署异常：未开启自动签署或者开发者ID未填入");
                }
            }
            /**
             * 判断是否存入数据库
             */
            if (saveToMysql == 1) {
                String save_result;
                Message msg = new Message();
                /**
                 * 合同发送成功
                 */
                if (obj.getString("type").equals(CONTRACT_SEND_RESULT)) {
                    msg.setTimestamp(obj.getString("timestamp"));
                    msg.setClientId(clientId);
                    msg.setType(obj.getString("type"));

                    msg.setBizNo(responseData.getString("bizNo"));
                    msg.setBatchOperationId((Number) responseData.get("batchOperationId"));
                    msg.setContractId((Number) responseData.get("contractId"));
                    msg.setSenderUserAccount(responseData.getString("senderUserAccount"));
                    if (responseData.containsKey("senderBusinessLine")) {
                        if(responseData.getString("senderBusinessLine") != null) {
                            msg.setSenderBusinessLine(responseData.getString("senderBusinessLine"));
                        }else{
                            msg.setSenderBusinessLine("");
                        }
                    }else{
                        msg.setSenderBusinessLine("");
                    }
                    msg.setSenderEnterpriseName(responseData.getString("senderEnterpriseName"));
                    msg.setResult(responseData.getString("result"));
                    save_result = stm.save_CONTRACT_SEND_RESULT(msg);
                    if (!save_result.equals("success")) {
                        return this.success_failToSave(save_result);
                    }
                }
                /**
                 * 合同被收件人签署
                 */
                else if (obj.getString("type").equals(OPERATION_COMPLETE)) {
                    msg.setTimestamp(obj.getString("timestamp"));
                    msg.setClientId(clientId);
                    msg.setType(obj.getString("type"));

                    msg.setBizNo(responseData.getString("bizNo"));
                    msg.setContractId((Number) responseData.get("contractId"));
                    msg.setReceiverId((Number) responseData.get("receiverId"));
                    logger.info("responseData: " + JSONObject.toJSONString(responseData, SerializerFeature.WriteMapNullValue));
                    if (responseData.containsKey("devAccountId") && responseData.get("devAccountId") != null) {
                        msg.setDevAccountId(responseData.getString("devAccountId"));
                    } else {
                        msg.setDevAccountId("");
                    }
                    msg.setUserAccount(responseData.getString("userAccount"));
                    msg.setEnterpriseName(responseData.getString("enterpriseName"));
                    msg.setUserType(responseData.getString("userType"));
                    msg.setOperationStatus(responseData.getString("operationStatus"));
                    msg.setMessage(responseData.getString("message"));
                    msg.setSenderUserAccount(responseData.getString("senderUserAccount"));
                    if (responseData.containsKey("senderBusinessLine")) {
                        if(responseData.getString("senderBusinessLine") != null) {
                            msg.setSenderBusinessLine(responseData.getString("senderBusinessLine"));
                        }else{
                            msg.setSenderBusinessLine("");
                        }
                    }else{
                        msg.setSenderBusinessLine("");
                    }
                    msg.setSenderEnterpriseName(responseData.getString("senderEnterpriseName"));
                    save_result = stm.save_OPERATION_COMPLETE(msg);
                    if (!save_result.equals("success")) {
                        return this.success_failToSave(save_result);
                    }
                }
                /**
                 * 合同签署完成
                 */
                else if (obj.getString("type").equals(CONTRACT_COMPLETE)) {
                    msg.setTimestamp(obj.getString("timestamp"));
                    msg.setClientId(clientId);
                    msg.setType(obj.getString("type"));

                    msg.setBizNo(responseData.getString("bizNo"));
                    JSONArray jsonArray = (JSONArray) responseData.get("contractIds");
                    msg.setContractIds(jsonArray.toJavaList(Number.class));
                    msg.setSenderUserAccount(responseData.getString("senderUserAccount"));
                    if (responseData.containsKey("senderBusinessLine")) {
                        if(responseData.getString("senderBusinessLine") != null) {
                            msg.setSenderBusinessLine(responseData.getString("senderBusinessLine"));
                        }else{
                            msg.setSenderBusinessLine("");
                        }
                    }else{
                        msg.setSenderBusinessLine("");
                    }
                    msg.setSenderEnterpriseName(responseData.getString("senderEnterpriseName"));
                    save_result = stm.save_CONTRACT_COMPLETE(msg);
                    if (!save_result.equals("success")) {
                        return this.success_failToSave(save_result);
                    }
                }
                /**
                 * 逾期未签
                 */
                else if (obj.getString("type").equals(CONTRACT_OVERDUE)) {
                    msg.setTimestamp(obj.getString("timestamp"));
                    msg.setClientId(clientId);
                    msg.setType(obj.getString("type"));

                    msg.setBizNo(responseData.getString("bizNo"));
                    msg.setContractId((Number) responseData.get("contractId"));
                    msg.setSenderUserAccount(responseData.getString("senderUserAccount"));
                    if (responseData.containsKey("senderBusinessLine")) {
                        if(responseData.getString("senderBusinessLine") != null) {
                            msg.setSenderBusinessLine(responseData.getString("senderBusinessLine"));
                        }else{
                            msg.setSenderBusinessLine("");
                        }
                    }else{
                        msg.setSenderBusinessLine("");
                    }
                    msg.setSenderEnterpriseName(responseData.getString("senderEnterpriseName"));
                    save_result = stm.save_CONTRACT_OVERDUE(msg);
                    if (!save_result.equals("success")) {
                        return this.success_failToSave(save_result);
                    }
                }
                /**
                 * 撤销合同
                 */
                else if (obj.getString("type").equals(CONTRACT_REVOKE)) {
                    msg.setTimestamp(obj.getString("timestamp"));
                    msg.setClientId(clientId);
                    msg.setType(obj.getString("type"));

                    msg.setContractId((Number) responseData.get("contractId"));
                    msg.setReceiverId((Number) responseData.get("receiverId"));
                    msg.setUserAccount(responseData.getString("userAccount"));
                    msg.setUserName(responseData.getString("userName"));
                    msg.setEnterpriseName(responseData.getString("enterpriseName"));
                    msg.setUserType(responseData.getString("userType"));
                    msg.setSenderUserAccount(responseData.getString("senderUserAccount"));
                    if (responseData.containsKey("senderBusinessLine")) {
                        if(responseData.getString("senderBusinessLine") != null) {
                            msg.setSenderBusinessLine(responseData.getString("senderBusinessLine"));
                        }else{
                            msg.setSenderBusinessLine("");
                        }
                    }else{
                        msg.setSenderBusinessLine("");
                    }
                    msg.setSenderEnterpriseName(responseData.getString("senderEnterpriseName"));
                    msg.setRevokeReason(responseData.getString("revokeReason"));
                    save_result = stm.save_CONTRACT_REVOKE(msg);
                    if (!save_result.equals("success")) {
                        return this.success_failToSave(save_result);
                    }
                }
                /**
                 * 账号绑定
                 */
                else if (obj.getString("type").equals(BINDING_ACCOUNT)) {
                    msg.setTimestamp(obj.getString("timestamp"));
                    msg.setClientId(clientId);
                    msg.setType(obj.getString("type"));

                    msg.setBizNo(responseData.getString("bizNo"));
                    msg.setEnterpriseName(responseData.getString("enterpriseName"));
                    msg.setAccount(responseData.getString("account"));
                    msg.setDevAccountId(responseData.getString("devAccountId"));
                    msg.setUserType(responseData.getString("userType"));
                    save_result = stm.save_BINDING_ACCOUNT(msg);
                    if (!save_result.equals("success")) {
                        return this.success_failToSave(save_result);
                    }
                }
                /**
                 * 档案+完成获取企业资料的授权
                 */
                else if (obj.getString("type").equals(BOX_ENT_AUTH_SUCCESS)) {
                    msg.setTimestamp(obj.getString("timestamp"));
                    msg.setClientId(clientId);
                    msg.setType(obj.getString("type"));

                    msg.setEntName(responseData.getString("entName"));
                    msg.setArchiveName(responseData.getString("archiveName"));
                    msg.setArchiveId((Number) responseData.get("archiveId"));
                    msg.setAuthInfo(responseData.getString("authInfo"));
                    save_result = stm.save_BOX_ENT_AUTH_SUCCESS(msg);
                    if (!save_result.equals("success")) {
                        return this.success_failToSave(save_result);
                    }
                }
                /**
                 * 档案+获取到个人资料的授权
                 */
                else if (obj.getString("type").equals(BOX_CUSTOMER_AUTH_SUCCESS)) {
                    msg.setTimestamp(obj.getString("timestamp"));
                    msg.setClientId(clientId);
                    msg.setType(obj.getString("type"));

                    msg.setArchiveName(responseData.getString("archiveName"));
                    msg.setArchiveId((Number) responseData.get("archiveId"));
                    msg.setAuthInfo(responseData.getString("authInfo"));
                    msg.setPersonInfo(responseData.getJSONObject("personInfo").toJSONString());
                    save_result = stm.save_BOX_CUSTOMER_AUTH_SUCCESS(msg);
                    if (!save_result.equals("success")) {
                        return this.success_failToSave(save_result);
                    }
                }
                /**
                 * 档案+完成获取企业资料的授权
                 */
                else if (obj.getString("type").equals(BOX_PASS_ENT_CUSTOMER_ALL_COLLECTIONS)) {
                    msg.setTimestamp(obj.getString("timestamp"));
                    msg.setClientId(clientId);
                    msg.setType(obj.getString("type"));

                    msg.setEntName(responseData.getString("entName"));
                    msg.setArchiveName(responseData.getString("archiveName"));
                    msg.setArchiveId((Number) responseData.get("archiveId"));
                    msg.setVerifyInfo(responseData.getString("verifyInfo"));
                    save_result = stm.save_BOX_PASS_ENT_CUSTOMER_ALL_COLLECTIONS(msg);
                    if (!save_result.equals("success")) {
                        return this.success_failToSave(save_result);
                    }
                }
                /**
                 * 档案+完成获取企业资料的授权
                 */
                else if (obj.getString("type").equals(BOX_PASS_CUSTOMER_ALL_COLLECTIONS)) {
                    msg.setTimestamp(obj.getString("timestamp"));
                    msg.setClientId(clientId);
                    msg.setType(obj.getString("type"));

                    msg.setArchiveName(responseData.getString("archiveName"));
                    msg.setArchiveId((Number) responseData.get("archiveId"));
                    msg.setVerifyInfo(responseData.getString("verifyInfo"));
                    msg.setPersonInfo(responseData.getJSONObject("personInfo").toJSONString());
                    save_result = stm.save_BOX_PASS_CUSTOMER_ALL_COLLECTIONS(msg);
                    if (!save_result.equals("success")) {
                        return this.success_failToSave(save_result);
                    }
                }
                /**
                 * 不做处理的异步通知
                 */
                else {
                    logger.info("此类型异步通知暂不处理。");
                    return this.success();
                }
            } else {
                logger.info("已选择不存入数据库，处理完毕。");
                return this.success();
            }
            return this.success();
        }
        /**
         * 啥也不是
         */
        else{
            return this.fail("啥也不是");
        }
    }

    public JSONObject getRequestBody(String contractId) {
        List<String> contractIds = new ArrayList<>();
        JSONObject requestBody = new JSONObject();
        JSONObject signer = new JSONObject();
        contractIds.add(contractId);
        signer.put("account", signAccount);
        signer.put("bizName", signBizName);
        signer.put("enterpriseName", signEnterpriseName);
        requestBody.put("signer", signer);
        requestBody.put("sealName", signSealName);
        requestBody.put("contractIds", contractIds);
        requestBody.put("pushUrl", signPushUrl);
        return requestBody;
    }
    public String sign(JSONObject request){
        final BestSignClient bestSignClient = new BestSignClient(
                signHost,
                signClientId,
                signClientSecret,
                signPrivateKey
        );
        final String api = "/api/contracts/sign";
        final String method = "POST";
        String result = bestSignClient.executeRequest(api, method, request);
        return result;
    }
}
