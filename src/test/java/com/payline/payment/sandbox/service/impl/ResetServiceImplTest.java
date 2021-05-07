package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.MockUtils;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.reset.request.ResetRequest;
import com.payline.pmapi.bean.reset.response.ResetResponse;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseFailure;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseSuccess;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResetServiceImplTest {

    private ResetServiceImpl service = new ResetServiceImpl();

    /**
     * This test case ensures that, when an amount ending with a value of 0 or 1,
     * the method returns a ResetResponseSuccess.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0", "1"})
    void resetRequest_ResetResponseSuccess(int lastDigit) {
        // given: the payment request containing the magic amount
        ResetRequest request = MockUtils.aResetRequestBuilder()
                .withAmount(
                        new Amount(new BigInteger("6000" + lastDigit), Currency.getInstance("EUR"))
                )
                .build();

        // when: calling the method paymentRequest
        ResetResponse response = service.resetRequest(request);

        // then: the response is a success
        assertEquals(ResetResponseSuccess.class, response.getClass());
    }

    /**
     * This test case ensures that, when an amount ending with a value of 0 or 1,
     * the method returns a ResetResponseSuccess.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0", "1"})
    void resetRequest_otherServiceTested(int lastDigit) {
        // given: the payment request containing the magic amount
        ResetRequest request = MockUtils.aResetRequestBuilder()
                .withAmount(
                        new Amount(new BigInteger("6010" + lastDigit), Currency.getInstance("EUR"))
                )
                .build();

        // when: calling the method paymentRequest
        ResetResponse response = service.resetRequest(request);

        // then: the response is a success
        assertEquals(ResetResponseFailure.class, response.getClass());
    }

    /**
     * This test case ensures that, when an amount of 10200  is given with a delay in the contractConfiguration ,
     * the method returns a ResetResponseSuccess after a delay of 2 seconds.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0"})
    void resetRequest_PaymentResponseWithDelay(int lastDigit) {

        Map<String, ContractProperty> contractProperties = new HashMap<>();
        contractProperties.put("delay", new ContractProperty("true"));

        // given: the payment request containing the magic amount and a contractConfiguration
        ResetRequest request = MockUtils.aResetRequestBuilder()
                .withAmount(new Amount(new BigInteger("6000" + lastDigit), Currency.getInstance("EUR")))
                .withContractConfiguration(new ContractConfiguration("Sandbox APM", contractProperties))
                .build();

        long startTime = System.currentTimeMillis();

        // when: calling the method paymentRequest
        ResetResponse response = service.resetRequest(request);

        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);

        // then: the response is a success
        assertEquals(ResetResponseSuccess.class, response.getClass());
        assertTrue(duration > 2000);
    }
}
