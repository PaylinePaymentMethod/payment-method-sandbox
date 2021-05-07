package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.MockUtils;
import com.payline.payment.sandbox.utils.Constants;
import com.payline.payment.sandbox.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigurationServiceImplTest {
    @InjectMocks
    private ConfigurationServiceImpl service = new ConfigurationServiceImpl();

    private static final String CHECK_ERROR_NON_REQUIRED = "CHECK_ERROR_NON_REQUIRED";
    private static final String CHECK_ERROR_REQUIRED = "CHECK_ERROR_REQUIRED";
    private static final String CHECK_EXCEPTION = "CHECK_EXCEPTION";
    private static final String CHECK_NULL_RESPONSE = "CHECK_NULL_RESPONSE";


    @Mock
    private ContractParametersCheckRequest MockContractParametersCheckRequest;
    @Mock
    private ReleaseProperties releaseProperties;

    @BeforeAll
    public void setup() {
        service = new ConfigurationServiceImpl();
        MockitoAnnotations.initMocks(this);
        Locale.setDefault(Locale.ENGLISH);
    }

    /**
     * This test case ensures that, when a ContractParametersCheckRequest is given with a delay in the contractConfiguration ,
     * the method returns a empty map after a delay of 2 seconds.
     */
    @ParameterizedTest(name = "[{index}] first digit: {0}")
    @ValueSource(strings = {"0"})
    void contractParametersCheckRequest_CheckWithDelay(int lastDigit) {

        Map<String, ContractProperty> contractProperties = new HashMap<>();
        contractProperties.put("delay", new ContractProperty("true"));

        // given: the payment request containing the magic amount and a contractConfiguration
        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder()
                .withContractConfiguration(new ContractConfiguration("Sandbox APM", contractProperties))
                .build();

        long startTime = System.currentTimeMillis();

        // when: calling the method paymentRequest
        Map<String, String> response = service.check(request);

        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);

        // then: the response is a success
        assertTrue(response.isEmpty());
        assertTrue(duration > 2000);
    }



    @Test
    void contractParametersCheckRequest_Behaviour() {

        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(Constants.ContractConfigurationKeys.LISTBOX_PARAMETER,null);

        // given: the payment request containing the magic amount and a contractConfiguration
        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder()
                .withAccountInfo(accountInfo)
                .build();

        Map<String, String> response = service.check(request);

        Map<String, String> errors = new HashMap<>();
        errors.put("listbox", "ListBox parameter is null");

        // then: the response is a success
        assertEquals(errors, response);
    }

    @Test
    void contractParametersCheckRequest_CHECK_ERROR_REQUIRED() {

        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(Constants.ContractConfigurationKeys.LISTBOX_PARAMETER,CHECK_ERROR_REQUIRED);

        // given: the payment request containing the magic amount and a contractConfiguration
        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder()
                .withAccountInfo(accountInfo)
                .build();

        Map<String, String> response = service.check(request);

        Map<String, String> errors = new HashMap<>();
        errors.put("listbox", "Error on the field listbox");

        // then: the response is a success
        assertEquals(errors, response);
    }

    @Test
    void contractParametersCheckRequest_CHECK_ERROR_NON_REQUIRED() {

        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(Constants.ContractConfigurationKeys.LISTBOX_PARAMETER,CHECK_ERROR_NON_REQUIRED);

        // given: the payment request containing the magic amount and a contractConfiguration
        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder()
                .withAccountInfo(accountInfo)
                .build();

        Map<String, String> response = service.check(request);

        Map<String, String> errors = new HashMap<>();
        errors.put("input", "Error on the field input");

        // then: the response is a success
        assertEquals(errors, response);
    }

    @Test
    void contractParametersCheckRequest_CHECK_NULL_RESPONSE() {

        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(Constants.ContractConfigurationKeys.LISTBOX_PARAMETER,CHECK_NULL_RESPONSE);

        // given: the payment request containing the magic amount and a contractConfiguration
        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder()
                .withAccountInfo(accountInfo)
                .build();

        Map<String, String> response = service.check(request);

        // then: the response is a success
        assertEquals(null, response);
    }

    @Test
    void contractParametersCheckRequest_CHECK_EXCEPTION() {
        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(Constants.ContractConfigurationKeys.LISTBOX_PARAMETER,CHECK_EXCEPTION);

        doReturn(accountInfo).when(MockContractParametersCheckRequest).getAccountInfo();

        assertThrows(NullPointerException.class,() -> service.check(MockContractParametersCheckRequest),"Simulate a NullPointerException thrown by the plugin");
    }

    @Test
    void contractParametersCheckRequest_Unknown() {


        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(Constants.ContractConfigurationKeys.LISTBOX_PARAMETER,"");

        doReturn(accountInfo).when(MockContractParametersCheckRequest).getAccountInfo();

        assertThrows(IllegalArgumentException.class,() -> service.check(MockContractParametersCheckRequest),"Unknown expected behaviour: ");
    }

    @Test
    void getName() {
        // when: calling the method getName
        String name = service.getName(Locale.getDefault());

        // then: the method returns the name
        assertNotNull(name);
    }

    @Test
    void getReleaseInformation() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String version = "M.m.p";

        // given: the release properties are OK
        doReturn(version).when(releaseProperties).get("release.version");
        Calendar cal = new GregorianCalendar();
        cal.set(2019, Calendar.AUGUST, 19);

        doReturn(formatter.format(cal.getTime())).when(releaseProperties).get("release.date");

        // when: calling the method getReleaseInformation
        ReleaseInformation releaseInformation = service.getReleaseInformation();

        // then: releaseInformation contains the right values
        assertEquals(version, releaseInformation.getVersion());
        assertEquals(2019, releaseInformation.getDate().getYear());
        assertEquals(Month.AUGUST, releaseInformation.getDate().getMonth());
        assertEquals(19, releaseInformation.getDate().getDayOfMonth());
    }

}
