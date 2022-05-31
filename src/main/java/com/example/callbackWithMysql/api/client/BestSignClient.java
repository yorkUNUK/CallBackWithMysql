package com.example.callbackWithMysql.api.client;

import com.alibaba.fastjson.JSONObject;
import com.example.callbackWithMysql.api.decrypt.MD5Utils;
import com.example.callbackWithMysql.api.decrypt.RSAUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author whthomas
 * @date 2018/6/17
 *
 * <h3>BestSignClient should be shared</h3>
 *
 * <p>BestSignClient performs best when you create a single {@code BestSignClient} instance and reuse it for
 * all of your API calls.
 *
 * <p>Use {@code new BestSignClient()} to create a shared instance with custom settings:
 * <pre>   {@code
 *
 *   // The singleton BestSignClient client.
 *   public final BestSignClient client = new BestSignClient(
 *                                          "https://api.bestsign.info",
 *                                          "l2ZxwSA6OBs6IgsQ6og9f7ixuPMR7hMQ",
 *                                          "3seTeUqUyhGlYZfD3CbGVXPDrcu1ieG4",
 *                                          privateKey
 * );
 *
 * }</pre>
 *
 * <p>Or use {@code spring} to create a singleton instance with custom settings:
 */
public class BestSignClient {
    private final Logger logger =  LoggerFactory.getLogger(this.getClass());
    public static final Marker bestSign = MarkerFactory.getMarker("BESTSIGN");
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * 系统的地址
     */
    private String host;

    /**
     * BestSign分配的客户端编号
     */
    private String clientId;

    /**
     * 客户端凭证
     */
    private String clientSecret;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * Token的缓存
     */
    private String tokenCache;

    public BestSignClient(String host, String clientId, String clientSecret, String privateKey) {
        this.host = host;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.privateKey = privateKey;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    //shared perform best
    private final OkHttpClient okHttpClient = new OkHttpClient
            .Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .build();

    /**
     * @param uriWithParam 请求的URL和参数
     * @param method       HTTP请求动词
     * @param requestData  请求的内容
     *
     * @return JSON字符串
     */
    public String executeRequest(String uriWithParam, String method, Object requestData) {
        return executeRequest(uriWithParam, method, requestData, 3);
    }

    private String executeRequest(String uriWithParam, String method, Object requestData, int retryTime) {
        int totalTry = retryTime;
        while (retryTime > 0) {
            try {
                logger.info(bestSign, "这是第" + (totalTry - retryTime + 1) + "次尝试");
                String token = queryToken();
                Long timestamp = System.currentTimeMillis();
                String urlWithQueryParam = String.format("%s%s", host, uriWithParam);
                final String signRSA = signRequest(uriWithParam, timestamp, requestData, method.toUpperCase());
                Headers headers = new Headers
                        .Builder()
                        .add("Authorization", "bearer " + token)
                        .add("bestsign-sign-timestamp", timestamp.toString())
                        .add("bestsign-client-id", clientId)
                        .add("bestsign-signature-type", "RSA256")
                        .add("bestsign-signature", signRSA)
                        .build();
                final Request.Builder requestBuilder = new Request
                        .Builder()
                        .url(urlWithQueryParam)
                        .headers(headers);

                if (Objects.equals(method.toUpperCase(), "GET")) {
                    requestBuilder.get();
                } else {
                    logger.info(bestSign, "入参: " + objectMapper.writeValueAsString(requestData));
                    final RequestBody requestBody = RequestBody.create(JSON_TYPE, requestData == null ? "" : objectMapper.writeValueAsString(requestData));
                    requestBuilder.method(method.toUpperCase(), requestBody);
                }
                final Response response = okHttpClient.newCall(requestBuilder.build()).execute();
                logger.info(bestSign, "http code:" + response.code() + ", Content-Type:" + response.headers().get("Content-Type"));
                if (response.code() == 401) {
                    // token失效, 重新获取token
                    invalidToken(token);
                } else if (response.code() == 200) {
                    if(response.headers().get("Content-Type").startsWith("application/zip") || response.headers().get("Content-Type").startsWith("application/pdf")) {
                        return Base64.getEncoder().encodeToString(response.body().bytes());
                    }
                    final String result = response.body().string();
                    logger.info(bestSign, "返回: " + result);
                    return response.body() != null ? result : null;
                }
                retryTime--;
            } catch (Exception ex) {
                logger.error(bestSign, "http请求异常:{}", ex.getLocalizedMessage() , ex);
                retryTime--;
            }
        }
        return null;
    }

    private String signRequest(String uriWithParam, Long timestamp, Object requestData, String method) throws Exception {
        String md5;
        if(Objects.equals(method.toUpperCase(), "GET")){
            md5 = MD5Utils.stringMD5("");
        }else{
            md5 = MD5Utils.stringMD5(requestData == null ? "" : objectMapper.writeValueAsString(requestData));
        }
        String content = String.format(
                "bestsign-client-id=%sbestsign-sign-timestamp=%sbestsign-signature-type=%srequest-body=%suri=%s",
                clientId,
                timestamp.toString(),
                "RSA256",
                md5,
                uriWithParam);
        return RSAUtils.signRSA(content, privateKey);
    }

    private synchronized String queryToken() throws IOException {
        if (null == tokenCache) {
            Map<String, String> requestData = new HashMap<>(2);
            requestData.put("clientId", clientId);
            requestData.put("clientSecret", clientSecret);
            final RequestBody requestBody = RequestBody
                    .create(JSON_TYPE, objectMapper.writeValueAsString(requestData));
            Request request = new Request
                    .Builder()
                    .url(String.format("%s/api/oa2/client-credentials/token", host))
                    .post(requestBody)
                    .build();
            try{
                final Response response = okHttpClient.newCall(request).execute();
                if (response.body() != null) {
                    tokenCache = objectMapper.readTree(response.body().string()).get("data").get("accessToken").asText();
                    return tokenCache;
                }else{
                    new Exception("Unexpected code" + response);
                }
            }catch (Exception e){
                logger.error("生成token异常:{}", e.getLocalizedMessage() , e);
                e.printStackTrace();
            }
        } else {
            return tokenCache;
        }
        return null;
    }

    private synchronized void invalidToken(String oldToken) {
        if (oldToken.equals(tokenCache)) {
            tokenCache = null;
        }
    }

}