package com.safecharge.biz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.safecharge.request.AddUPOCreditCardRequest;
import com.safecharge.request.Authorization3DRequest;
import com.safecharge.request.GetOrderDetailsRequest;
import com.safecharge.request.GetSessionTokenRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.safecharge.request.AddUPOAPMRequest;
import com.safecharge.request.AddUPOCreditCardByTempTokenRequest;
import com.safecharge.request.CardTokenizationRequest;
import com.safecharge.request.GetMerchantPaymentMethodsRequest;
import com.safecharge.request.OpenOrderRequest;
import com.safecharge.request.Payment3DRequest;
import com.safecharge.request.PaymentAPMRequest;
import com.safecharge.request.PaymentCCRequest;
import com.safecharge.request.RefundTransactionRequest;
import com.safecharge.request.SafechargeRequest;
import com.safecharge.request.SettleTransactionRequest;
import com.safecharge.request.UpdateOrderRequest;
import com.safecharge.request.VoidTransactionRequest;
import com.safecharge.response.AddUPOAPMResponse;
import com.safecharge.response.AddUPOCreditCardByTempTokenResponse;
import com.safecharge.response.AddUPOCreditCardResponse;
import com.safecharge.response.Authorization3DResponse;
import com.safecharge.response.CardTokenizationResponse;
import com.safecharge.response.GetMerchantPaymentMethodsResponse;
import com.safecharge.response.GetOrderDetailsResponse;
import com.safecharge.response.OpenOrderResponse;
import com.safecharge.response.Payment3DResponse;
import com.safecharge.response.PaymentAPMResponse;
import com.safecharge.response.PaymentCCResponse;
import com.safecharge.response.RefundTransactionResponse;
import com.safecharge.response.SafechargeResponse;
import com.safecharge.response.SessionTokenResponse;
import com.safecharge.response.SettleTransactionResponse;
import com.safecharge.response.UpdateOrderResponse;
import com.safecharge.response.VoidTransactionResponse;
import com.safecharge.util.APIConstants;

/**
 * Copyright (C) 2007-2017 SafeCharge International Group Limited.
 * <p>
 * This class provides functionality to execute {@link SafechargeRequest}s directly to the SafeCharge's REST API
 *
 * @author <a mailto:nikolad@safecharge.com>Nikola Dichev</a>
 * @since 2/16/2017
 */
public class SafechargeRequestExecutor {

    private static final Log logger = LogFactory.getLog(SafechargeRequestExecutor.class);
    private static final Map<Class<? extends SafechargeRequest>, Class<? extends SafechargeResponse>> RESPONSE_TYPE_BY_REQUEST_TYPE =
            new HashMap<Class<? extends SafechargeRequest>, Class<? extends SafechargeResponse>>() {
                private static final long serialVersionUID = -5429154998138428047L;

                {
                    put(GetSessionTokenRequest.class, SessionTokenResponse.class);
                    put(OpenOrderRequest.class, OpenOrderResponse.class);
                    put(UpdateOrderRequest.class, UpdateOrderResponse.class);
                    put(GetOrderDetailsRequest.class, GetOrderDetailsResponse.class);
                    put(PaymentCCRequest.class, PaymentCCResponse.class);
                    put(PaymentAPMRequest.class, PaymentAPMResponse.class);
                    put(Authorization3DRequest.class, Authorization3DResponse.class);
                    put(CardTokenizationRequest.class, CardTokenizationResponse.class);
                    put(Payment3DRequest.class, Payment3DResponse.class);
                    put(AddUPOCreditCardByTempTokenRequest.class, AddUPOCreditCardByTempTokenResponse.class);
                    put(SettleTransactionRequest.class, SettleTransactionResponse.class);
                    put(VoidTransactionRequest.class, VoidTransactionResponse.class);
                    put(RefundTransactionRequest.class, RefundTransactionResponse.class);
                    put(AddUPOCreditCardRequest.class, AddUPOCreditCardResponse.class);
                    put(AddUPOAPMRequest.class, AddUPOAPMResponse.class);
                    put(GetMerchantPaymentMethodsRequest.class, GetMerchantPaymentMethodsResponse.class);
                }
            };
    private static final Map<Class<? extends SafechargeRequest>, String> REQUEST_URL_BY_REQUEST_TYPE =
            new HashMap<Class<? extends SafechargeRequest>, String>() {
                private static final long serialVersionUID = -6533247180543051173L;

                {
                    put(GetSessionTokenRequest.class, APIConstants.GET_SESSION_TOKEN_URL);
                    put(OpenOrderRequest.class, APIConstants.OPEN_ORDER_URL);
                    put(UpdateOrderRequest.class, APIConstants.UPDATE_ORDER_URL);
                    put(GetOrderDetailsRequest.class, APIConstants.GET_ORDER_DETAILS_URL);
                    put(PaymentCCRequest.class, APIConstants.PAYMENT_CC_URL);
                    put(PaymentAPMRequest.class, APIConstants.PAYMENT_APM_URL);
                    put(Authorization3DRequest.class, APIConstants.AUTHORIZATION_3D_URL);
                    put(CardTokenizationRequest.class, APIConstants.CARD_TOKENIZATION_URL);
                    put(Payment3DRequest.class, APIConstants.PAYMENT_3D_URL);
                    put(AddUPOCreditCardByTempTokenRequest.class, APIConstants.ADD_UPO_CREDIT_CARD_BY_TEMP_TOKEN_URL);
                    put(SettleTransactionRequest.class, APIConstants.SETTLE_TRANSACTION_URL);
                    put(VoidTransactionRequest.class, APIConstants.VOID_TRANSACTION_URL);
                    put(RefundTransactionRequest.class, APIConstants.REFUND_TRANSACTION_URL);
                    put(AddUPOCreditCardRequest.class, APIConstants.ADD_UPO_CREDIT_CARD_URL);
                    put(AddUPOAPMRequest.class, APIConstants.ADD_UPO_APM_URL);
                    put(GetMerchantPaymentMethodsRequest.class, APIConstants.GET_MERCHANT_PAYMENT_METHODS_REQUEST_URL);
                }
            };
    private static SafechargeRequestExecutor instance = null;
    private static HttpClient httpClient;
    private static boolean isInitialized = false;

    private SafechargeRequestExecutor() {
    }

    public static SafechargeRequestExecutor getInstance() {
        if (instance == null) {
            instance = new SafechargeRequestExecutor();
        }
        return instance;
    }

    public void init() {
        init(SafechargeHttpClient.createDefault());
    }

    /**
     * This method initiates the {@link SafechargeRequestExecutor} with a configured {@link HttpClient} and server information.
     */
    public void init(HttpClient httpClient) {

        if (isInitialized) {
            // already initialized
            return;
        }

        SafechargeRequestExecutor.httpClient = httpClient;

        isInitialized = true;
    }

    /**
     * Sends a {@link SafechargeRequest} to SafeCharge's API via HTTP POST method.
     *
     * @param request
     * @return
     */
    public SafechargeResponse executeRequest(SafechargeRequest request) {

        if (!isInitialized) {
            init();
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        try {
            String requestJSON = gson.toJson(request);
            String serviceUrl = request.getServerHost() + REQUEST_URL_BY_REQUEST_TYPE.get(request.getClass());
            String responseJSON = executeJsonRequest(request, requestJSON, serviceUrl);

            return gson.fromJson(responseJSON, RESPONSE_TYPE_BY_REQUEST_TYPE.get(request.getClass()));

        } catch (IOException e) {

            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
        }

        return null;
    }

    public String executeJsonRequest(SafechargeRequest request, String requestJSON, String serviceUrl) throws IOException {
        HttpPost httpPost = new HttpPost(serviceUrl);
        httpPost.setHeaders(APIConstants.REQUEST_HEADERS);
        httpPost.setEntity(new StringEntity(requestJSON));

        if (logger.isDebugEnabled()) {
            logger.debug("Sent " + request.getClass()
                                          .getSimpleName() + ": " + requestJSON);
        }

        HttpResponse response = httpClient.execute(httpPost);

        String responseJSON = EntityUtils.toString(response.getEntity());
        if (logger.isDebugEnabled()) {
            logger.debug("Received " + request.getClass()
                                              .getSimpleName() + ": " + responseJSON);
        }
        return responseJSON;
    }

    public String executeRequest(String request, String serviceUrl, Header[] headers) throws IOException {
        HttpPost httpPost = new HttpPost(serviceUrl);
        httpPost.setHeaders(headers);
        httpPost.setEntity(new StringEntity(request));

        if (logger.isDebugEnabled()) {
            logger.debug("Sent " + request);
        }

        HttpResponse response = httpClient.execute(httpPost);

        String responseJSON = EntityUtils.toString(response.getEntity());
        if (logger.isDebugEnabled()) {
            logger.debug("Received " + response);
        }
        return responseJSON;
    }
}