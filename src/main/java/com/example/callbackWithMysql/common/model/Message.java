package com.example.callbackWithMysql.common.model;

import java.util.List;

/**
 * User: York
 * Date: 2021/12/28
 * Time: 11:50
 */

public class Message {
    private String timestamp;
    private String clientId;
    private String type;
    private String bizNo;
    private Number batchOperationId;
    private Number contractId;
    private List<Number> contractIds;
    private Number receiverId;
    private String senderUserAccount;
    private String senderBusinessLine;
    private String senderEnterpriseName;
    private String result;
    private String devAccountId;
    private String userAccount;
    private String enterpriseName;
    private String userType;
    private String operationStatus;
    private String message;
    private String userName;
    private String revokeReason;
    private String account;
    private String entName;
    private String archiveName;
    private Number archiveId;
    private String authInfo;
    private String personInfo;
    private String verifyInfo;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public Number getBatchOperationId() {
        return batchOperationId;
    }

    public void setBatchOperationId(Number batchOperationId) {
        this.batchOperationId = batchOperationId;
    }

    public Number getContractId() {
        return contractId;
    }

    public void setContractId(Number contractId) {
        this.contractId = contractId;
    }

    public List<Number> getContractIds() {
        return contractIds;
    }

    public void setContractIds(List<Number> contractIds) {
        this.contractIds = contractIds;
    }

    public Number getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Number receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderUserAccount() {
        return senderUserAccount;
    }

    public void setSenderUserAccount(String senderUserAccount) {
        this.senderUserAccount = senderUserAccount;
    }

    public String getSenderBusinessLine() {
        return senderBusinessLine;
    }

    public void setSenderBusinessLine(String senderBusinessLine) {
        this.senderBusinessLine = senderBusinessLine;
    }

    public String getSenderEnterpriseName() {
        return senderEnterpriseName;
    }

    public void setSenderEnterpriseName(String senderEnterpriseName) {
        this.senderEnterpriseName = senderEnterpriseName;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDevAccountId() {
        return devAccountId;
    }

    public void setDevAccountId(String devAccountId) {
        this.devAccountId = devAccountId;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRevokeReason() {
        return revokeReason;
    }

    public void setRevokeReason(String revokeReason) {
        this.revokeReason = revokeReason;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getEntName() {
        return entName;
    }

    public void setEntName(String entName) {
        this.entName = entName;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public Number getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(Number archiveId) {
        this.archiveId = archiveId;
    }

    public String getAuthInfo() {
        return authInfo;
    }

    public void setAuthInfo(String authInfo) {
        this.authInfo = authInfo;
    }

    public String getPersonInfo() {
        return personInfo;
    }

    public void setPersonInfo(String personInfo) {
        this.personInfo = personInfo;
    }

    public String getVerifyInfo() {
        return verifyInfo;
    }

    public void setVerifyInfo(String verifyInfo) {
        this.verifyInfo = verifyInfo;
    }
}
