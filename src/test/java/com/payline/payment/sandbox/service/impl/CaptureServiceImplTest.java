package com.payline.payment.oney.service.impl;


import com.payline.payment.sandbox.MockUtils;
import com.payline.payment.sandbox.service.impl.CaptureServiceImpl;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.capture.response.CaptureResponse;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseFailure;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseSuccess;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.service.CaptureService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CaptureServiceImplTest {

    @InjectMocks
    CaptureService service;

    @Mock
    private CaptureRequest mockCaptureRequest;

    @BeforeAll
    public void setup() {
        service = new CaptureServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void captureRequest() {
        CaptureRequest request = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withTransactionId(MockUtils.aTransactionId())
                .withPartnerTransactionId(MockUtils.getPartnerTransactionid())
                .withAmount( new Amount(new BigInteger("50000"), Currency.getInstance("EUR")))
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.anOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .build();

        // when: calling the method paymentRequest
        CaptureResponse response = service.captureRequest(request);

        // then: the response is a success
        Assertions.assertEquals(CaptureResponseSuccess.class, response.getClass());
    }

    @Test
    void captureRequestWithPartnerTransactionId() {
        CaptureRequest request = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withTransactionId(MockUtils.aTransactionId())
                .withPartnerTransactionId(MockUtils.getPartnerTransactionid())
                .withAmount( new Amount(new BigInteger("50001"), Currency.getInstance("EUR")))
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.anOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .build();

        // when: calling the method paymentRequest
        CaptureResponse response = service.captureRequest(request);

        // then: the response is a success
        Assertions.assertEquals(CaptureResponseSuccess.class, response.getClass());
        Assertions.assertEquals("PARTNER_ID.0123456789",((CaptureResponseSuccess)response).getPartnerTransactionId());
        Assertions.assertEquals("STATUS",((CaptureResponseSuccess)response).getStatusCode());

    }
    @Test
    void captureRequestFailure() {
        CaptureRequest request = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withTransactionId(MockUtils.aTransactionId())
                .withPartnerTransactionId(MockUtils.getPartnerTransactionid())
                .withAmount( new Amount(new BigInteger("50100"), Currency.getInstance("EUR")))
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.anOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .build();

        // when: calling the method paymentRequest
        CaptureResponse response = service.captureRequest(request);

        // then: the response is a success
        Assertions.assertEquals(CaptureResponseFailure.class, response.getClass());

    }
    @Test
    void captureRequestFailureRefused() {
        CaptureRequest request = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withTransactionId(MockUtils.aTransactionId())
                .withPartnerTransactionId(MockUtils.getPartnerTransactionid())
                .withAmount( new Amount(new BigInteger("50101"), Currency.getInstance("EUR")))
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.anOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .build();

        // when: calling the method paymentRequest
        CaptureResponse response = service.captureRequest(request);

        // then: the response is a success
        Assertions.assertEquals(CaptureResponseFailure.class, response.getClass());
        Assertions.assertEquals("PARTNER_ID.0123456789",((CaptureResponseFailure)response).getPartnerTransactionId());
        Assertions.assertEquals("Error code less than 50 characters long",((CaptureResponseFailure)response).getErrorCode());
        Assertions.assertEquals(FailureCause.REFUSED,((CaptureResponseFailure)response).getFailureCause());

    }

    @Test
    void captureRequestWithNullAmount() {
        // when: calling the method CaptureRequest
        doReturn(null).when(mockCaptureRequest).getAmount();
        assertThrows(IllegalArgumentException.class, () -> service.captureRequest(mockCaptureRequest));
    }

    @Test
    void canPartial() {
        Assertions.assertEquals(false, service.canPartial());
    }

    @Test
    void canMultiple() {
        Assertions.assertEquals(false, service.canMultiple());
    }


}