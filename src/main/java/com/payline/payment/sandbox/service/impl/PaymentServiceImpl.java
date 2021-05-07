package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.exception.PluginException;
import com.payline.payment.sandbox.utils.Logger;
import com.payline.payment.sandbox.utils.PaymentResponseUtil;
import com.payline.payment.sandbox.utils.service.AbstractService;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseActiveWaiting;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PaymentServiceImpl extends AbstractService<PaymentResponse> implements PaymentService {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);
    private static final String PAYMENT_REQUEST = "paymentRequest";

    /**------------------------------------------------------------------------------------------------------------------*/
    @Override
    public PaymentResponse paymentRequest(PaymentRequest paymentRequest) {

        if(paymentRequest.getContractConfiguration() != null){
                PaymentResponseUtil.apiResponseDelay(paymentRequest.getContractConfiguration());
        }

        this.verifyRequest(paymentRequest);

        String amount = paymentRequest.getAmount().getAmountInSmallestUnit().toString();

        Boolean isPaymentFormUpdatedFilled = false;

            if(!paymentRequest.getRequestContext().getRequestData().isEmpty() && paymentRequest.getRequestContext().getRequestData().get("step").equals("2")){
                isPaymentFormUpdatedFilled = true;
            }

        /* PaymentResponseSuccess */
        if ("10000".equals(amount) || amount.matches("^[3-9][0-9]*$") || (("10300".equals(amount) ||"10301".equals(amount)) && isPaymentFormUpdatedFilled)) {
            String transactionAdditionalData = "";

            Logger.log(this.getClass().getSimpleName(),PAYMENT_REQUEST, amount, "PaymentResponseSuccess minimale");

            PaymentResponseSuccess.PaymentResponseSuccessBuilder builder = PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                    .withPartnerTransactionId(PaymentResponseUtil.PARTNER_TRANSACTION_ID)
                    .withTransactionDetails(new EmptyTransactionDetails());

            // If the payment form contains data, put them into transaction additional data
            if (paymentRequest.getPaymentFormContext() != null
                    && paymentRequest.getPaymentFormContext().getPaymentFormParameter() != null
                    && !paymentRequest.getPaymentFormContext().getPaymentFormParameter().isEmpty()) {

                transactionAdditionalData = paymentRequest.getPaymentFormContext().getPaymentFormParameter()
                        .entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining("&"));

                transactionAdditionalData = transactionAdditionalData + "&" + paymentRequest.getPaymentFormContext().getSensitivePaymentFormParameter()
                        .entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining("&"));
            }

            builder.withTransactionAdditionalData(transactionAdditionalData);

            return builder.build();
        }

        /* PaymentResponseFailure */
        if( "10100".equals( amount ) ){
            Logger.log(this.getClass().getSimpleName(),PAYMENT_REQUEST, amount, "PaymentResponseFailure avec failureCause(INVALID_DATA) & errorCode (<= 50 caractères)");
            return PaymentResponseUtil.failureClassic();
        }
        if( "10101".equals( amount ) ){
            Logger.log(this.getClass().getSimpleName(),PAYMENT_REQUEST, amount, "PaymentResponseFailure avec failureCause(INVALID_DATA)");
            return PaymentResponseUtil.failureMinimal();
        }
        if( "10102".equals( amount ) ){
            Logger.log(this.getClass().getSimpleName(),PAYMENT_REQUEST, amount, "PaymentResponseFailure avec failureCause(INVALID_DATA) & errorCode (> 50 caractères)");
            return PaymentResponseUtil.failureLongErrorCode();
        }
        if( "10103".equals( amount ) ){
            Logger.log(this.getClass().getSimpleName(),PAYMENT_REQUEST, amount, "PaymentResponseFailure avec failureCause(INVALID_DATA) & errorCode & partnerTransactionId");
            return PaymentResponseUtil.failureWithPartnerTransactionId();
        }

        /* PaymentResponseRedirect */
        PaymentResponseRedirect.RedirectionRequest redirectionRequest = null;
        try {
            redirectionRequest = PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder.aRedirectionRequest()
                    .withRequestType(PaymentResponseRedirect.RedirectionRequest.RequestType.GET)
                    .withUrl(new URL("https", "www.google.com", "/fr"))
                    .build();
        } catch (MalformedURLException e) {
            LOGGER.error("RedirectionRequest unable to create the URL: ", e);
            throw new PluginException("Plugin error, RedirectionRequest unable to create the URL: ", e);
        }
        if( "10200".equals( amount ) || amount.startsWith("2") ){
            Logger.log(this.getClass().getSimpleName(),PAYMENT_REQUEST, amount, "PaymentResponseRedirect avec redirectionRequest & partnerTransactionId");
            return PaymentResponseRedirect.PaymentResponseRedirectBuilder.aPaymentResponseRedirect()
                    .withRedirectionRequest(redirectionRequest)
                    .withPartnerTransactionId(PaymentResponseUtil.PARTNER_TRANSACTION_ID)
                    .build();
        }
        if( "10201".equals( amount ) ){
            Logger.log(this.getClass().getSimpleName(),PAYMENT_REQUEST, amount, "PaymentResponseRedirect avec redirectionRequest & partnerTransactionId & statusCode & requestContext");
            Map<String, String> context = new HashMap<>();
            context.put("key", "value");
            return PaymentResponseRedirect.PaymentResponseRedirectBuilder.aPaymentResponseRedirect()
                    .withRedirectionRequest(redirectionRequest)
                    .withPartnerTransactionId(PaymentResponseUtil.PARTNER_TRANSACTION_ID)
                    .withStatusCode("PARTNER_STATUS")
                    .withRequestContext(
                            RequestContext.RequestContextBuilder.aRequestContext()
                                    .withRequestData(context)
                                    .build()
                    )
                    .build();
        }

        /* PaymentResponseFormUpdated */
        if ("10300".equals(amount)) {
            Logger.log(this.getClass().getSimpleName(), PAYMENT_REQUEST, amount, "PaymentResponseFormUpdated avec un formulaire complet");
            // Create the PaymentFormConfigurationResponse
            PaymentFormConfigurationResponse paymentFormConfigurationResponse = PaymentFormConfigurationResponseSpecific
                    .PaymentFormConfigurationResponseSpecificBuilder
                    .aPaymentFormConfigurationResponseSpecific()
                    .withPaymentForm(PaymentResponseUtil.aCustomForm())

                    .build();

            Map<String, String> context = new HashMap<>();
            context.put("step", "2");

            // Return the PaymentResponseFormUpdated
            return PaymentResponseFormUpdated.PaymentResponseFormUpdatedBuilder
                    .aPaymentResponseFormUpdated()
                    .withPaymentFormConfigurationResponse(paymentFormConfigurationResponse)
                    .withRequestContext(RequestContext.RequestContextBuilder.aRequestContext()
                            .withRequestData(context)
                            .build())
                    .build();

        }
        /* PaymentResponseFormUpdated */
        if ("10301".equals(amount)) {
            Logger.log(this.getClass().getSimpleName(), PAYMENT_REQUEST, amount, "PaymentResponseFormUpdated avec un PartnerWidgetForm");
            // Create the PaymentFormConfigurationResponse
            PaymentFormConfigurationResponse paymentFormConfigurationResponse = null;

            paymentFormConfigurationResponse = PaymentFormConfigurationResponseSpecific
                    .PaymentFormConfigurationResponseSpecificBuilder
                    .aPaymentFormConfigurationResponseSpecific()
                    .withPaymentForm(PaymentResponseUtil.aPartnerWidgetForm())
                    .build();


            Map<String, String> context = new HashMap<>();
            context.put("step", "2");

            // Return the PaymentResponseFormUpdated
            return PaymentResponseFormUpdated.PaymentResponseFormUpdatedBuilder
                    .aPaymentResponseFormUpdated()
                    .withPaymentFormConfigurationResponse(paymentFormConfigurationResponse)
                    .withRequestContext(RequestContext.RequestContextBuilder.aRequestContext()
                            .withRequestData(context)
                            .build())
                    .build();
        }
        /* PaymentResponseDoPayment */
        if( "10400".equals( amount ) ){
            Logger.log(this.getClass().getSimpleName(),PAYMENT_REQUEST, amount, "PaymentResponseDoPayment avec partnerTransactionId & paymentMode");
            return PaymentResponseUtil.doPaymentMinimal();
        }

        /* PaymentResponseActiveWaiting */
        if( "10500".equals( amount ) ){
            Logger.log(this.getClass().getSimpleName(),PAYMENT_REQUEST, amount, "PaymentResponseActiveWaiting");
            return new PaymentResponseActiveWaiting();
        }

        /* Generic plugin behaviours */
        return super.generic(this.getClass().getSimpleName(),PAYMENT_REQUEST, amount );
    }
    /**------------------------------------------------------------------------------------------------------------------*/
    /**
     * Performs standard verification of the request content.
     * Checks that every field required to process to a payment is filled in the request
     * (ex: PartnerConfiguration, Amount, etc.)
     *
     * @param paymentRequest
     */
    private void verifyRequest(PaymentRequest paymentRequest) {
        if (paymentRequest.getContractConfiguration() == null
                || paymentRequest.getPartnerConfiguration() == null
                || paymentRequest.getAmount() == null) {
            throw new IllegalArgumentException("The PaymentRequest is missing required data");
        }
    }

    /**------------------------------------------------------------------------------------------------------------------*/

}
