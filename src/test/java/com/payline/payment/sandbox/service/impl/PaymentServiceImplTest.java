package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.MockUtils;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.PaymentFormContext;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.*;
import com.payline.pmapi.bean.paymentform.bean.field.AbstractPaymentFormInputField;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormField;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;


class PaymentServiceImplTest {

    private PaymentServiceImpl service = new PaymentServiceImpl();

    @Mock
    private PaymentRequest mockRequest;

    @BeforeEach
    void setup() {
        mockRequest = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(new Amount(new BigInteger("10000"), Currency.getInstance("EUR")))
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withRequestContext(RequestContext.RequestContextBuilder.aRequestContext()
                        .build())
                .build();

        MockitoAnnotations.initMocks(this);
    }


    /**
     * This test case ensures that, when the service tested is PaymentWithRedirectionService (amount starts by 2),
     * the method returns a PaymentResponseRedirect.
     */
    @Test
    void paymentRequest_redirection() {
        // given: the payment request containing the magic amount
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(
                        new Amount(new BigInteger("20000"), Currency.getInstance("EUR"))
                )
                .build();

        // when: calling the method paymentRequest
        PaymentResponse response = service.paymentRequest(request);

        // then: the response is a success
        assertEquals(PaymentResponseRedirect.class, response.getClass());
    }

    /**
     * This test case ensures that, when an amount starting with any other value than 1 or 2 is given,
     * the method returns a PaymentResponseSuccess.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"3", "4", "5", "6", "7", "8", "9"})
    void paymentRequest_otherServiceTested(int firstDigit) {
        // given: the payment request containing the magic amount
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(
                        new Amount(new BigInteger(firstDigit + "0000"), Currency.getInstance("EUR"))
                )
                .build();

        // when: calling the method paymentRequest
        PaymentResponse response = service.paymentRequest(request);

        // then: the response is a success
        assertEquals(PaymentResponseSuccess.class, response.getClass());
    }

    /**
     * This test case ensures that, when an amount of 10100 is given,
     * the method returns a PaymentResponseSuccess with an INVALID_DATA failure cause.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0", "1", "2", "3"})
    void paymentRequest_PaymentResponseFailure_InvalidData(int lastDigit) {
        // given: the payment request containing the magic amount
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(
                        new Amount(new BigInteger("1010" + lastDigit), Currency.getInstance("EUR"))
                )
                .build();

        // when: calling the method paymentRequest
        PaymentResponse response = service.paymentRequest(request);

        // then: the response is a success
        assertEquals(PaymentResponseFailure.class, response.getClass());
        assertEquals(FailureCause.INVALID_DATA, ((PaymentResponseFailure) response).getFailureCause());
    }

    /**
     * This test case ensures that, when an amount between 10200 and 10201 is given,
     * the method returns a PaymentResponseRedirect.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0", "1"})
    void paymentRequest_PaymentResponseRedirect(int lastDigit) {
        // given: the payment request containing the magic amount
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(
                        new Amount(new BigInteger("1020" + lastDigit), Currency.getInstance("EUR"))
                )
                .build();

        // when: calling the method paymentRequest
        PaymentResponse response = service.paymentRequest(request);

        // then: the response is a success
        assertEquals(PaymentResponseRedirect.class, response.getClass());

    }

    /**
     * This test case ensures that, when an amount starting with any other value than 1 or 2 is given,
     * the method returns a PaymentResponseSuccess.
     */
    @Test
    void paymentRequest_PaymentResponseFormUpdated_CustomForm() {
        // Step
        Map<String, String> step = new HashMap<>();
        step.put("step", "1");

        // given: the payment request containing the magic amount
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(new Amount(new BigInteger("10300"), Currency.getInstance("EUR")))
                .withRequestContext(RequestContext.RequestContextBuilder.aRequestContext()
                        .withRequestData(step)
                        .build())
                .build();

        // when: calling the method paymentRequest
        PaymentResponse response = service.paymentRequest(request);

        // Step Update
        step = ((PaymentResponseFormUpdated) response).getRequestContext().getRequestData();

        // Get form data from PaymentResponseFormUpdated
        CustomForm customForm = (CustomForm) ((PaymentFormConfigurationResponseSpecific) ((PaymentResponseFormUpdated) response).getPaymentFormConfigurationResponse()).getPaymentForm();
        List<PaymentFormField> customField = customForm.getCustomFields();

        // Form filling
        Map<String, String> map = new HashMap<>();
        Integer compteur = 0;
        for (PaymentFormField i : customField) {
            compteur = compteur++;
            if (i instanceof AbstractPaymentFormInputField)
                map.put(((AbstractPaymentFormInputField) i).getKey(), "Value" + compteur);
        }

        // Set form data in a PaymentFormContext
        PaymentFormContext paymentFormContext = PaymentFormContext.PaymentFormContextBuilder.aPaymentFormContext().withPaymentFormParameter(map).build();

        // given: the payment request containing the magic amount
        request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(new Amount(new BigInteger("10300"), Currency.getInstance("EUR")))
                .withRequestContext(RequestContext.RequestContextBuilder.aRequestContext()
                        .withRequestData(step)
                        .build())
                .withPaymentFormContext(paymentFormContext)
                .build();

        // when: calling the method paymentRequest
        response = service.paymentRequest(request);

        // then: the response is a success
        assertEquals(PaymentResponseSuccess.class, response.getClass());
    }

    /**
     * This test case ensures that, when an amount starting with any other value than 1 or 2 is given,
     * the method returns a PaymentResponseSuccess.
     */
    @Test
    void paymentRequest_PaymentResponseFormUpdated_PartnerWidgetForm() {
        // Step
        Map<String, String> step = new HashMap<>();
        step.put("step", "1");

        // given: the payment request containing the magic amount
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(new Amount(new BigInteger("10301"), Currency.getInstance("EUR")))
                .withRequestContext(RequestContext.RequestContextBuilder.aRequestContext()
                        .withRequestData(step)
                        .build())
                .build();

        // when: calling the method paymentRequest
        PaymentResponse response = service.paymentRequest(request);

        assertTrue(response instanceof PaymentResponseFormUpdated);

        // Step Update
        step = ((PaymentResponseFormUpdated) response).getRequestContext().getRequestData();

        // given: the payment request containing the magic amount
        request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(new Amount(new BigInteger("10301"), Currency.getInstance("EUR")))
                .withRequestContext(RequestContext.RequestContextBuilder.aRequestContext()
                        .withRequestData(step)
                        .build())
                .build();

        // when: calling the method paymentRequest
        response = service.paymentRequest(request);

        // then: the response is a success
        assertEquals(PaymentResponseSuccess.class, response.getClass());
    }

    /**
     * This test case ensures that, when an amount between 10400 and more is given,
     * the method returns a PaymentResponseDoPayment.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0"})
    void paymentRequest_PaymentResponseDoPayment(int lastDigit) {
        // given: the payment request containing the magic amount
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(
                        new Amount(new BigInteger("1040" + lastDigit), Currency.getInstance("EUR"))
                )
                .build();

        // when: calling the method paymentRequest
        PaymentResponse response = service.paymentRequest(request);

        // then: the response is a success
        assertEquals(PaymentResponseDoPayment.class, response.getClass());

    }

    /**
     * This test case ensures that, when an amount between 10500 and more is given,
     * the method returns a PaymentResponseDoPayment.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0"})
    void paymentRequest_PaymentResponseActiveWaiting(int lastDigit) {
        // given: the payment request containing the magic amount
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(
                        new Amount(new BigInteger("1050" + lastDigit), Currency.getInstance("EUR"))
                )
                .build();

        // when: calling the method paymentRequest
        PaymentResponse response = service.paymentRequest(request);

        // then: the response is a success
        assertEquals(PaymentResponseActiveWaiting.class, response.getClass());

    }

    /**
     * This test case ensures that, when an null amount is given,
     * the method returns throw an IllegalArgumentException.
     */
    @Test
    void paymentRequest_VerifyRequest_Amount() {
        doReturn(null).when(mockRequest).getAmount();
        assertThrows(IllegalArgumentException.class, () -> service.paymentRequest(mockRequest));
    }

    /**
     * This test case ensures that, when an null ContractConfiguration is given,
     * the method returns throw an IllegalArgumentException.
     */
    @Test
    void paymentRequest_VerifyRequest_ContractConfiguration() {
        doReturn(null).when(mockRequest).getContractConfiguration();
        assertThrows(IllegalArgumentException.class, () -> service.paymentRequest(mockRequest));
    }

    /**
     * This test case ensures that, when an PartnerConfiguration is given,
     * the method returns throw an IllegalArgumentException.
     */
    @Test
    void paymentRequest_VerifyRequest_PartnerConfiguration() {
        doReturn(null).when(mockRequest).getPartnerConfiguration();
        assertThrows(IllegalArgumentException.class, () -> service.paymentRequest(mockRequest));
    }

    /**
     * This test case ensures that, when an PartnerConfiguration, an Amount and a ContractConfiguration are given,
     * the method returns throw an IllegalArgumentException.
     */
    @Test
    void paymentRequest_VerifyRequest() {
        doReturn(null).when(mockRequest).getAmount();
        doReturn(null).when(mockRequest).getContractConfiguration();
        doReturn(null).when(mockRequest).getPartnerConfiguration();
        assertThrows(IllegalArgumentException.class, () -> service.paymentRequest(mockRequest));
    }

    /**
     * This test case ensures that, when an amount of 10200  is given with a delay in the contractConfiguration ,
     * the method returns a PaymentResponseRedirect after a delay of 2 seconds.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0"})
    void paymentRequest_PaymentResponseWithDelay(int lastDigit) {
        // given: the payment request containing the magic amount

        Map<String, ContractProperty> contractProperties = new HashMap<>();
        contractProperties.put("delay",
                new ContractProperty("true"));


        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withAmount(
                        new Amount(new BigInteger("1020" + lastDigit), Currency.getInstance("EUR"))
                )
                .withContractConfiguration(
                        new ContractConfiguration("Sandbox APM", contractProperties)
                )
                .build();
        long startTime = System.currentTimeMillis();
        // when: calling the method paymentRequest
        PaymentResponse response = service.paymentRequest(request);
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);

        // then: the response is a success
        assertEquals(PaymentResponseRedirect.class, response.getClass());
        assertTrue(duration > 2000);

    }
}
