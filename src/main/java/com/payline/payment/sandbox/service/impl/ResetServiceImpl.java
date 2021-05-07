package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.utils.Logger;
import com.payline.payment.sandbox.utils.PaymentResponseUtil;
import com.payline.payment.sandbox.utils.service.AbstractService;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.reset.request.ResetRequest;
import com.payline.pmapi.bean.reset.response.ResetResponse;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseFailure;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseSuccess;
import com.payline.pmapi.service.ResetService;

public class ResetServiceImpl extends AbstractService<ResetResponse> implements ResetService {
    private static final String RESET_REQUEST = "resetRequest";
    @Override
    public ResetResponse resetRequest(ResetRequest resetRequest) {

        if(resetRequest.getContractConfiguration() != null){
            PaymentResponseUtil.apiResponseDelay(resetRequest.getContractConfiguration());
        }

        this.verifyRequest(resetRequest);

        String amount = resetRequest.getAmount().getAmountInSmallestUnit().toString();

        switch( amount ){
            /* ResetResponseSuccess */
            case "60000":
                Logger.log(this.getClass().getSimpleName(),RESET_REQUEST, amount, "ResetResponseSuccess");
                return ResetResponseSuccess.ResetResponseSuccessBuilder.aResetResponseSuccess()
                        .build();
            case "60001":
                Logger.log(this.getClass().getSimpleName(),RESET_REQUEST, amount, "ResetResponseSuccess avec partnerTransactionId & statusCode");
                return ResetResponseSuccess.ResetResponseSuccessBuilder.aResetResponseSuccess()
                        .withPartnerTransactionId(PaymentResponseUtil.PARTNER_TRANSACTION_ID)
                        .withStatusCode("STATUS")
                        .build();

            /* ResetResponseFailure */
            case "60100":
                Logger.log(this.getClass().getSimpleName(),RESET_REQUEST, amount, "ResetResponseFailure");
                return ResetResponseFailure.ResetResponseFailureBuilder.aResetResponseFailure()
                        .build();
            case "60101":
                Logger.log(this.getClass().getSimpleName(),RESET_REQUEST, amount, "ResetResponseFailure avec failureCause(REFUSED) & errorCode & partnerTransactionId");
                return ResetResponseFailure.ResetResponseFailureBuilder.aResetResponseFailure()
                        .withErrorCode("Error code less than 50 characters long")
                        .withFailureCause(FailureCause.REFUSED)
                        .withPartnerTransactionId( PaymentResponseUtil.PARTNER_TRANSACTION_ID )
                        .build();

            default:
                return super.generic(this.getClass().getSimpleName(),RESET_REQUEST, amount );
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

    /**
     * Performs standard verification of the request content.
     * Checks that every field required in the request is filled.
     * @param resetRequest the request
     */
    private void verifyRequest(ResetRequest resetRequest) {
        if( resetRequest.getAmount() == null ){
            throw new IllegalArgumentException( "The ResetRequest is missing an amount" );
        }
    }

}