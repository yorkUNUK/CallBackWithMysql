package com.example.callbackWithMysql.common.save;

import com.example.callbackWithMysql.common.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * User: York
 * Date: 2021/12/28
 * Time: 11:50
 */

@Repository
public class saveToMysql {
    //logback
    Logger logger =  LoggerFactory.getLogger(saveToMysql.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String save_CONTRACT_SEND_RESULT(Message msg) {
        try{
            jdbcTemplate.update("INSERT INTO CONTRACT_SEND_RESULT VALUES(?,?,?,?,?,?,?,?,?,?)", new Object[]{
                    0,
                    msg.getTimestamp(),
                    msg.getClientId(),
                    msg.getBizNo(),
                    msg.getBatchOperationId().toString(),
                    msg.getContractId().toString(),
                    msg.getSenderUserAccount(),
                    msg.getSenderBusinessLine(),
                    msg.getSenderEnterpriseName(),
                    msg.getResult()
            });
            logger.info("Save to CONTRACT_SEND_RESULT Successfully!");
        } catch (DataAccessException e) {
            logger.error("存入数据库异常:{}", e.getLocalizedMessage() , e);
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    public String save_OPERATION_COMPLETE(Message msg) {
        try{
            jdbcTemplate.update("INSERT INTO OPERATION_COMPLETE VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{
                    0,
                    msg.getTimestamp(),
                    msg.getClientId(),
                    msg.getBizNo(),
                    msg.getContractId().toString(),
                    msg.getReceiverId().toString(),
                    msg.getDevAccountId(),
                    msg.getUserAccount(),
                    msg.getEnterpriseName(),
                    msg.getUserType(),
                    msg.getOperationStatus(),
                    msg.getMessage(),
                    msg.getSenderUserAccount(),
                    msg.getSenderBusinessLine(),
                    msg.getSenderEnterpriseName()
            });
            logger.info("Save to OPERATION_COMPLETE Successfully!");
        } catch (DataAccessException e) {
            logger.error("存入数据库异常:{}", e.getLocalizedMessage() , e);
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    public String save_CONTRACT_COMPLETE(Message msg) {
        try{
            jdbcTemplate.update("INSERT INTO CONTRACT_COMPLETE VALUES(?,?,?,?,?,?,?,?)", new Object[]{
                    0,
                    msg.getTimestamp(),
                    msg.getClientId(),
                    msg.getBizNo(),
                    msg.getContractIds().get(0).toString(),
                    msg.getSenderUserAccount(),
                    msg.getSenderBusinessLine(),
                    msg.getSenderEnterpriseName()
            });
            logger.info("Save to CONTRACT_COMPLETE Successfully!");
        } catch (DataAccessException e) {
            logger.error("存入数据库异常:{}", e.getLocalizedMessage() , e);
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    public String save_CONTRACT_OVERDUE(Message msg) {
        try{
            jdbcTemplate.update("INSERT INTO CONTRACT_OVERDUE VALUES(?,?,?,?,?,?,?,?)", new Object[]{
                    0,
                    msg.getTimestamp(),
                    msg.getClientId(),
                    msg.getBizNo(),
                    msg.getContractId().toString(),
                    msg.getSenderUserAccount(),
                    msg.getSenderBusinessLine(),
                    msg.getSenderEnterpriseName()
            });
            logger.info("Save to CONTRACT_OVERDUE Successfully!");
        } catch (DataAccessException e) {
            logger.error("存入数据库异常:{}", e.getLocalizedMessage() , e);
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    public String save_CONTRACT_REVOKE(Message msg) {
        try{
            jdbcTemplate.update("INSERT INTO CONTRACT_REVOKE VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{
                    0,
                    msg.getTimestamp(),
                    msg.getClientId(),
                    msg.getContractId().toString(),
                    msg.getReceiverId().toString(),
                    msg.getUserAccount(),
                    msg.getUserName(),
                    msg.getEnterpriseName(),
                    msg.getUserType(),
                    msg.getSenderUserAccount(),
                    msg.getSenderBusinessLine(),
                    msg.getSenderEnterpriseName(),
                    msg.getRevokeReason()
            });
            logger.info("Save to CONTRACT_REVOKE Successfully!");
        } catch (DataAccessException e) {
            logger.error("存入数据库异常:{}", e.getLocalizedMessage() , e);
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    public String save_BINDING_ACCOUNT(Message msg) {
        try{
            jdbcTemplate.update("INSERT INTO BINDING_ACCOUNT VALUES(?,?,?,?,?,?,?,?)", new Object[]{
                    0,
                    msg.getTimestamp(),
                    msg.getClientId(),
                    msg.getBizNo(),
                    msg.getDevAccountId(),
                    msg.getAccount(),
                    msg.getEnterpriseName(),
                    msg.getUserType()
            });
            logger.info("Save to BINDING_ACCOUNT Successfully!");
        } catch (DataAccessException e) {
            logger.error("存入数据库异常:{}", e.getLocalizedMessage() , e);
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    public String save_BOX_ENT_AUTH_SUCCESS(Message msg) {
        try{
            jdbcTemplate.update("INSERT INTO BOX_ENT_AUTH_SUCCESS VALUES(?,?,?,?,?,?,?)", new Object[]{
                    0,
                    msg.getTimestamp(),
                    msg.getClientId(),
                    msg.getEntName(),
                    msg.getArchiveName(),
                    msg.getArchiveId().toString(),
                    msg.getAuthInfo()
            });
            logger.info("Save to BOX_ENT_AUTH_SUCCESS Successfully!");
        } catch (DataAccessException e) {
            logger.error("存入数据库异常:{}", e.getLocalizedMessage() , e);
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    public String save_BOX_CUSTOMER_AUTH_SUCCESS(Message msg) {
        try{
            jdbcTemplate.update("INSERT INTO BOX_CUSTOMER_AUTH_SUCCESS VALUES(?,?,?,?,?,?,?)", new Object[]{
                    0,
                    msg.getTimestamp(),
                    msg.getClientId(),
                    msg.getArchiveName(),
                    msg.getArchiveId().toString(),
                    msg.getAuthInfo(),
                    msg.getPersonInfo()
            });
            logger.info("Save to BOX_CUSTOMER_AUTH_SUCCESS Successfully!");
        } catch (DataAccessException e) {
            logger.error("存入数据库异常:{}", e.getLocalizedMessage() , e);
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    public String save_BOX_PASS_ENT_CUSTOMER_ALL_COLLECTIONS(Message msg) {
        try{
            jdbcTemplate.update("INSERT INTO BOX_PASS_ENT_CUSTOMER_ALL_COLLECTIONS VALUES(?,?,?,?,?,?,?)", new Object[]{
                    0,
                    msg.getTimestamp(),
                    msg.getClientId(),
                    msg.getEntName(),
                    msg.getArchiveName(),
                    msg.getArchiveId().toString(),
                    msg.getVerifyInfo()
            });
            logger.info("Save to BOX_PASS_ENT_CUSTOMER_ALL_COLLECTIONS Successfully!");
        } catch (DataAccessException e) {
            logger.error("存入数据库异常:{}", e.getLocalizedMessage() , e);
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    public String save_BOX_PASS_CUSTOMER_ALL_COLLECTIONS(Message msg) {
        try{
            jdbcTemplate.update("INSERT INTO BOX_PASS_CUSTOMER_ALL_COLLECTIONS VALUES(?,?,?,?,?,?,?)", new Object[]{
                    0,
                    msg.getTimestamp(),
                    msg.getClientId(),
                    msg.getArchiveName(),
                    msg.getArchiveId().toString(),
                    msg.getVerifyInfo(),
                    msg.getPersonInfo()
            });
            logger.info("Save to BOX_PASS_CUSTOMER_ALL_COLLECTIONS Successfully!");
        } catch (DataAccessException e) {
            logger.error("存入数据库异常:{}", e.getLocalizedMessage() , e);
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }
}
