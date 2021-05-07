package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.utils.Logger;
import com.payline.payment.sandbox.utils.PaymentResponseUtil;
import com.payline.payment.sandbox.utils.service.AbstractService;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.service.PaymentWithRedirectionService;

public class PaymentWithRedirectionServiceImpl extends AbstractService<PaymentResponse> implements PaymentWithRedirectionService {

    @Override
    public PaymentResponse finalizeRedirectionPayment(RedirectionPaymentRequest redirectionPaymentRequest) {

        if(redirectionPaymentRequest.getContractConfiguration() != null){
            PaymentResponseUtil.apiResponseDelay(redirectionPaymentRequest.getContractConfiguration());
        }

        this.verifyRequest(redirectionPaymentRequest);

        String amount = redirectionPaymentRequest.getAmount().getAmountInSmallestUnit().toString();
        return this.process("finalizeRedirectionPayment",amount);
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {

        if(transactionStatusRequest.getContractConfiguration() != null){
            PaymentResponseUtil.apiResponseDelay(transactionStatusRequest.getContractConfiguration());
        }

        this.verifyRequest(transactionStatusRequest);

        String amount = transactionStatusRequest.getAmount().getAmountInSmallestUnit().toString();
        return this.process("handleSessionExpired",amount);
    }

    /**
     * Performs standard verification of the request content.
     * Checks that every field required to finalize a transaction after a redirection is filled in the request.
     *
     * @param redirectionPaymentRequest the request to verify
     */
    private void verifyRequest(RedirectionPaymentRequest redirectionPaymentRequest){
        if( redirectionPaymentRequest.getAmount() == null ){
            throw new IllegalArgumentException( "The RedirectionPaymentRequest is missing an amount" );
        }
        if( redirectionPaymentRequest.getContractConfiguration() == null
                || redirectionPaymentRequest.getEnvironment() == null
                || redirectionPaymentRequest.getPartnerConfiguration() == null ){
            throw new IllegalArgumentException( "The RedirectionPaymentRequest is missing required data" );
        }
    }

    /**
     * Performs standard verification of the request content.
     * Checks that every field required to recover the state of a transaction when user session has expired is filled in the request.
     * (ex: transactionId)
     * @param transactionStatusRequest the request to verify
     */
    private void verifyRequest(TransactionStatusRequest transactionStatusRequest){
        if( transactionStatusRequest.getAmount() == null ){
            throw new IllegalArgumentException( "The TransactionStatusRequest is missing an amount" );
        }
        if( transactionStatusRequest.getContractConfiguration() == null
                || transactionStatusRequest.getEnvironment() == null
                || transactionStatusRequest.getPartnerConfiguration() == null ){
            throw new IllegalArgumentException( "The TransactionStatusRequest is missing required data" );
        }
    }

    private PaymentResponse process(String method, String amount ){

        // retrieve the last 3 digits of the amount, which identify the response type
        String amountLastDigits = amount.substring(2);

        switch( amountLastDigits ){
            /* PaymentResponseSuccess */
            case "000":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseSuccess avec partnerTransactionId & transactionDetails(EmptyTransactionDetails)");
                return PaymentResponseUtil.successMinimal();
            case "001":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseSuccess avec partnerTransactionId & transactionDetails(EmptyTransactionDetails) & transactionAdditionalData");
                return PaymentResponseUtil.successAdditionalData();
            case "002":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseSuccess avec partnerTransactionId & transactionDetails(BankTransfert)");
                return PaymentResponseUtil.successBankTransferDetails();
            case "003":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseSuccess avec partnerTransactionId & transactionDetails(Email)");
                return PaymentResponseUtil.successEmailDetails();

            /* PaymentResponseFailure */
            case "100":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseFailure avec failureCause(INVALID_DATA) & errorCode (<= 50 caractères)");
                return PaymentResponseUtil.failureClassic();
            case "101":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseFailure avec failureCause(INVALID_DATA)");
                return PaymentResponseUtil.failureMinimal();
            case "102":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseFailure avec failureCause(INVALID_DATA) & errorCode (> 50 caractères)");
                return PaymentResponseUtil.failureLongErrorCode();
            case "103":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseFailure avec failureCause(INVALID_DATA) & errorCode & partnerTransactionId");
                return PaymentResponseUtil.failureWithPartnerTransactionId();

            /* PaymentResponseOnHold */
            case "200":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseOnHold avec onHoldCause(SCORING_ASYNC)) & partnerTransactionId");
                return PaymentResponseUtil.onHoldMinimalScoringAsync();
            case "201":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseOnHold avec onHoldCause(ASYNC_RETRY)) & partnerTransactionId");
                return PaymentResponseUtil.onHoldMinimalAsyncRetry();

            /* PaymentResponseDoPayment */
            case "300":
                Logger.log(this.getClass().getSimpleName(),method, amount, "PaymentResponseDoPayment avec partnerTransactionId & paymentMode");
                return PaymentResponseUtil.doPaymentMinimal();

            /* Generic plugin behaviours */
            default:
                return super.generic(this.getClass().getSimpleName(),method,  amount );
        }
    }

}
