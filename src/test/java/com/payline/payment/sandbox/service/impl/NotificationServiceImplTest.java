package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.MockUtils;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.TransactionStateChangedResponse;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotificationServiceImplTest {

    private NotificationServiceImpl service = new NotificationServiceImpl();

    /**
     * This test case ensures that, when a NotificationRequest is given with a delay in the contractConfiguration ,
     * the method returns a empty map after a delay of 2 seconds.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0"})
    void parse_CheckWithDelay(int lastDigit) {


        Map<String, ContractProperty> contractProperties = new HashMap<>();
        contractProperties.put("delay", new ContractProperty("true"));

        // given: the payment request containing the magic amount and a contractConfiguration
        NotificationRequest request = MockUtils.aNotificationRequestBuilder()
                .withContractConfiguration(new ContractConfiguration("Sandbox APM", contractProperties))
                .build();

        long startTime = System.currentTimeMillis();

        // when: calling the method paymentRequest
        NotificationResponse response = service.parse(request);

        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);

        // then: the response is a success
        assertEquals(IgnoreNotificationResponse.class, response.getClass());
        assertTrue(duration > 2000);
    }
    @Test
    void parse_IgnoreNotificationResponse() {

        // given: the payment request containing the magic amount and a contractConfiguration
        NotificationRequest request = MockUtils.aNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream("40000".getBytes()))
                .build();

        // when: calling the method paymentRequest
        NotificationResponse response = service.parse(request);

        // then: the response is a success
        assertEquals(IgnoreNotificationResponse.class, response.getClass());

    }
    @Test
    void parse_IgnoreNotificationResponsewithHttpStatus() {
        // given: the payment request containing the magic amount and a contractConfiguration
        NotificationRequest request = MockUtils.aNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream("40001".getBytes()))
                .build();

        // when: calling the method paymentRequest
        NotificationResponse response = service.parse(request);

        // then: the response is a success
        assertEquals(IgnoreNotificationResponse.class, response.getClass());
        assertEquals(204,((IgnoreNotificationResponse)response).getHttpStatus());

    }
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0", "1", "2"})
    void parse_PaymentResponseByNotificationResponse(int lastDigit) {

        // given: the payment request containing the magic amount and a contractConfiguration
        NotificationRequest request = MockUtils.aNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(("4010" + lastDigit).getBytes()))
                .build();

        // when: calling the method paymentRequest
        NotificationResponse response = service.parse(request);

        // then: the response is a success
        assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());

    }

    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0", "1", "2","3"})
    void parse_TransactionStateChangedResponse(int lastDigit) {

        // given: the payment request containing the magic amount and a contractConfiguration
        NotificationRequest request = MockUtils.aNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(("4020" + lastDigit).getBytes()))
                .build();

        // when: calling the method paymentRequest
        NotificationResponse response = service.parse(request);

        // then: the response is a success
        assertEquals(TransactionStateChangedResponse.class, response.getClass());

    }
}
