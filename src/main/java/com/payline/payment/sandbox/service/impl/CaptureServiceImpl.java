package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.utils.Logger;
import com.payline.payment.sandbox.utils.PaymentResponseUtil;
import com.payline.payment.sandbox.utils.service.AbstractService;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.capture.response.CaptureResponse;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseFailure;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseSuccess;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.service.CaptureService;

public class CaptureServiceImpl extends AbstractService<CaptureResponse> implements CaptureService {

    private static final String CAPTURE_REQUEST = "captureRequest";

    @Override
    public CaptureResponse captureRequest(CaptureRequest captureRequest) {
        this.verifyRequest(captureRequest);

        String amount = captureRequest.getAmount().getAmountInSmallestUnit().toString();

        switch( amount ){
            /* CaptureResponseSuccess */
            case "50000":
                Logger.log(this.getClass().getSimpleName(),CAPTURE_REQUEST, amount, "CaptureResponseSuccess");
                return CaptureResponseSuccess.CaptureResponseSuccessBuilder.aCaptureResponseSuccess()
                        .build();
            case "50001":
                Logger.log(this.getClass().getSimpleName(),CAPTURE_REQUEST, amount, "CaptureResponseSuccess avec partnerTransactionId & statusCode");
                return CaptureResponseSuccess.CaptureResponseSuccessBuilder.aCaptureResponseSuccess()
                        .withPartnerTransactionId( PaymentResponseUtil.PARTNER_TRANSACTION_ID )
                        .withStatusCode("STATUS")
                        .build();

            /* CaptureResponseFailure */
            case "50100":
                Logger.log(this.getClass().getSimpleName(),CAPTURE_REQUEST, amount, "CaptureResponseFailure");
                return CaptureResponseFailure.CaptureResponseFailureBuilder.aCaptureResponseFailure()
                        .build();
            case "50101":
                Logger.log(this.getClass().getSimpleName(),CAPTURE_REQUEST, amount, "CaptureResponseFailure avec failureCause(REFUSED) & errorCode & partnerTransactionId");
                return CaptureResponseFailure.CaptureResponseFailureBuilder.aCaptureResponseFailure()
                        .withErrorCode("Error code less than 50 characters long")
                        .withFailureCause(FailureCause.REFUSED)
                        .withPartnerTransactionId( PaymentResponseUtil.PARTNER_TRANSACTION_ID )
                        .build();

            /* Generic plugin behaviours */
            default:
                return super.generic(this.getClass().getSimpleName(),CAPTURE_REQUEST, amount );
        }
    }

    /**
     * Performs standard verification of the request content.
     * Checks that every field required in the request is filled.
     * @param captureRequest the request
     */
    private void verifyRequest(CaptureRequest captureRequest) {
        if( captureRequest.getAmount() == null ){
            throw new IllegalArgumentException( "The CaptureRequest is missing an amount" );
        }
    }

    @Override
    public boolean canMultiple() {
        return false;
    }

    @Override
    public boolean canPartial() {
        return false;
    }

}