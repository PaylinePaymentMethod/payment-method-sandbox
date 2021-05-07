package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.exception.PluginException;
import com.payline.payment.sandbox.utils.Logger;
import com.payline.payment.sandbox.utils.PaymentResponseUtil;
import com.payline.payment.sandbox.utils.service.AbstractService;
import com.payline.pmapi.bean.common.*;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.TransactionStateChangedResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.NotificationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class NotificationServiceImpl extends AbstractService<NotificationResponse> implements NotificationService {
    private static final String NOTIFICATION_REQUEST = "notificationRequest";
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(NotificationServiceImpl.class);

    @Override
    public NotificationResponse parse(NotificationRequest notificationRequest) {

        if(notificationRequest.getContractConfiguration() != null){
            PaymentResponseUtil.apiResponseDelay(notificationRequest.getContractConfiguration());
        }

        this.verifyRequest( notificationRequest );

        // retrieve amount from the NotificationRequest content
        try(BufferedReader br = new BufferedReader(new InputStreamReader(notificationRequest.getContent(), StandardCharsets.UTF_8))) {
            String amount = br.lines().collect(Collectors.joining(System.lineSeparator()));

            TransactionCorrelationId transactionCorrelationId = TransactionCorrelationId.TransactionCorrelationIdBuilder.aCorrelationIdBuilder()
                    .withType(TransactionCorrelationId.CorrelationIdType.PARTNER_TRANSACTION_ID)
                    .withValue(PaymentResponseUtil.PARTNER_TRANSACTION_ID)
                    .build();

            switch (amount) {
                /* IgnoreNotificationResponse */
                case "40000":
                    Logger.log(this.getClass().getSimpleName(), NOTIFICATION_REQUEST, amount, "IgnoreNotificationResponse");
                    return new IgnoreNotificationResponse();
                case "40001":
                    Logger.log(this.getClass().getSimpleName(), NOTIFICATION_REQUEST, amount, "IgnoreNotificationResponse avec httpStatus");
                    return IgnoreNotificationResponse.IgnoreNotificationResponseBuilder.aIgnoreNotificationResponseBuilder()
                            .withHttpStatus(204)
                            .build();

                /* PaymentResponseByNotificationResponse */
                case "40100":
                    Logger.log(this.getClass().getSimpleName(), NOTIFICATION_REQUEST, amount, "PaymentResponseByNotificationResponse avec PaymentResponseSuccess");
                    return PaymentResponseByNotificationResponse.PaymentResponseByNotificationResponseBuilder.aPaymentResponseByNotificationResponseBuilder()
                            .withPaymentResponse(PaymentResponseUtil.successMinimal())
                            .withTransactionCorrelationId(transactionCorrelationId)
                            .build();
                case "40101":
                    Logger.log(this.getClass().getSimpleName(), NOTIFICATION_REQUEST, amount, "PaymentResponseByNotificationResponse avec PAymentResponseFailure");
                    return PaymentResponseByNotificationResponse.PaymentResponseByNotificationResponseBuilder.aPaymentResponseByNotificationResponseBuilder()
                            .withPaymentResponse(PaymentResponseUtil.failureClassic())
                            .withTransactionCorrelationId(transactionCorrelationId)
                            .build();
                case "40102":
                    Logger.log(this.getClass().getSimpleName(), NOTIFICATION_REQUEST, amount, "PaymentResponseByNotificationResponse avec PaymentReponseOnHold");
                    return PaymentResponseByNotificationResponse.PaymentResponseByNotificationResponseBuilder.aPaymentResponseByNotificationResponseBuilder()
                            .withPaymentResponse(PaymentResponseUtil.onHoldMinimalScoringAsync())
                            .withTransactionCorrelationId(transactionCorrelationId)
                            .build();

                /* TransactionStateChangedResponse */
                case "40200":
                    Logger.log(this.getClass().getSimpleName(), NOTIFICATION_REQUEST, amount, "TransactionStateChangedResponse");
                    return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder.aTransactionStateChangedResponse()
                            .build();
                case "40201":
                    Logger.log(this.getClass().getSimpleName(), NOTIFICATION_REQUEST, amount, "TransactionStateChangedResponse avec SuccessTransactionStatus");
                    return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder.aTransactionStateChangedResponse()
                            .withTransactionStatus(SuccessTransactionStatus.builder().build())
                            .build();
                case "40202":
                    Logger.log(this.getClass().getSimpleName(), NOTIFICATION_REQUEST, amount, "TransactionStateChangedResponse avec FailureTransactionStatus");
                    return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder.aTransactionStateChangedResponse()
                            .withTransactionStatus(
                                    FailureTransactionStatus.builder()
                                            .failureCause(FailureCause.INVALID_DATA)
                                            .build()
                            )
                            .build();
                case "40203":
                    Logger.log(this.getClass().getSimpleName(), NOTIFICATION_REQUEST, amount, "TransactionStateChangedResponse avec OnHoldTransactionStatus");
                    return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder.aTransactionStateChangedResponse()
                            .withTransactionStatus(
                                    OnHoldTransactionStatus.builder()
                                            .onHoldCause(OnHoldCause.SCORING_ASYNC)
                                            .build()
                            )
                            .build();

                /* Generic plugin errors */
                default:
                    return super.generic(this.getClass().getSimpleName(), NOTIFICATION_REQUEST, amount);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to load the BufferReader :", e);
            throw new PluginException("Plugin error: unable to load the BufferReader", e);
        }
    }

    @Override
    public void notifyTransactionStatus(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        // RAS
    }

    /**
     * Performs standard verification of the request content.
     * Checks that every field required in the request is filled
     * @param notificationRequest the request
     */
    private void verifyRequest(NotificationRequest notificationRequest) {
        if( notificationRequest.getContent() == null ){
            throw new IllegalArgumentException("The NotificationRequest is missing a content");
        }
    }

}