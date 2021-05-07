package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.MockUtils;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RefundServiceImplTest {
    private RefundServiceImpl service = new RefundServiceImpl();

    /**
     * This test case ensures that, when a RefundRequest is given with a delay in the contractConfiguration ,
     * the method returns a empty map after a delay of 2 seconds.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0"})
    void contractParametersCheckRequest_CheckWithDelay(int lastDigit) {

        Map<String, ContractProperty> contractProperties = new HashMap<>();
        contractProperties.put("delay", new ContractProperty("true"));

        // given: the payment request containing the magic amount and a contractConfiguration
        RefundRequest request = RefundRequest.RefundRequestBuilder.aRefundRequest()
                .withAmount(new Amount(new BigInteger("7000" + lastDigit), Currency.getInstance("EUR")))
                .withContractConfiguration(new ContractConfiguration("Sandbox APM", contractProperties))
                .withOrder(MockUtils.anOrder())
                .withBuyer(MockUtils.aBuyer())
                .withEnvironment(MockUtils.anEnvironment())
                .withPartnerTransactionId("0001")
                .withTransactionId("0000")
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .build();


        long startTime = System.currentTimeMillis();

        // when: calling the method paymentRequest
        RefundResponse response = service.refundRequest(request);

        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);

        // then: the response is a success
        assertEquals(RefundResponseSuccess.class, response.getClass());
        assertTrue(duration > 2000);
    }
}
