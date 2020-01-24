package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.utils.PaymentResponseUtil;
import com.payline.payment.sandbox.utils.service.AbstractService;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.service.PaymentWithRedirectionService;

public class PaymentWithRedirectionServiceImpl extends AbstractService<PaymentResponse> implements PaymentWithRedirectionService {

    @Override
    public PaymentResponse finalizeRedirectionPayment(RedirectionPaymentRequest redirectionPaymentRequest) {
        this.verifyRequest(redirectionPaymentRequest);

        String amount = redirectionPaymentRequest.getAmount().getAmountInSmallestUnit().toString();
        return this.process(amount);
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
        this.verifyRequest(transactionStatusRequest);

        String amount = transactionStatusRequest.getAmount().getAmountInSmallestUnit().toString();
        return this.process(amount);
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

    private PaymentResponse process( String amount ){
        // retrieve the last 3 digits of the amount, which identify the response type
        String amountLastDigits = amount.substring(2);

        switch( amountLastDigits ){
            /* PaymentResponseSuccess */
            case "000":
                return PaymentResponseUtil.successMinimal();
            case "001":
                return PaymentResponseUtil.successAdditionalData();
            case "002":
                return PaymentResponseUtil.successBankTransferDetails();
            case "003":
                return PaymentResponseUtil.successEmailDetails();

            /* PaymentResponseFailure */
            case "100":
                return PaymentResponseUtil.failureClassic();
            case "101":
                return PaymentResponseUtil.failureMinimal();
            case "102":
                return PaymentResponseUtil.failureLongErrorCode();
            case "103":
                return PaymentResponseUtil.failureWithPartnerTransactionId();

            /* PaymentResponseOnHold */
            case "200":
                return PaymentResponseUtil.onHoldMinimalScoringAsync();
            case "201":
                return PaymentResponseUtil.onHoldMinimalAsyncRetry();

            /* PaymentResponseDoPayment */
            case "300":
                return PaymentResponseUtil.doPaymentMinimal();

            /* Generic plugin behaviours */
            default:
                return super.generic( amount );
        }
    }

}
