package com.payline.payment.sandbox;

import com.payline.payment.sandbox.service.impl.ConfigurationServiceImpl;
import com.payline.payment.sandbox.utils.Constants;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.configuration.request.RetrievePluginConfigurationRequest;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.payment.*;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.reset.request.ResetRequest;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.*;

/**
 * Utility class that generates mocks of frequently used objects.
 */
public class MockUtils {

    private static final String TRANSACTIONID = "123456789012345678901";
    private static final String PARTNER_TRANSACTIONID = "098765432109876543210";

     /**
     * Generate a valid accountInfo, an attribute of a {@link ContractParametersCheckRequest} instance.
     */
    public static Map<String, String> anAccountInfo(){
        return anAccountInfo( aContractConfiguration() );
    }

    /**
     * Generate a valid accountInfo, an attribute of a {@link ContractParametersCheckRequest} instance,
     * from the given {@link ContractConfiguration}.
     *
     * @param contractConfiguration The model object from which the properties will be copied
     */
    public static Map<String, String> anAccountInfo( ContractConfiguration contractConfiguration ){
        Map<String, String> accountInfo = new HashMap<>();
        for( Map.Entry<String, ContractProperty> entry : contractConfiguration.getContractProperties().entrySet() ){
            accountInfo.put(entry.getKey(), entry.getValue().getValue());
        }
        return accountInfo;
    }

    /**
     * Generate a valid {@link Browser}.
     */
    public static Browser aBrowser(){
        return Browser.BrowserBuilder.aBrowser()
                .withLocale( Locale.getDefault() )
                .withIp( "192.168.0.1" )
                .withUserAgent( aUserAgent() )
                .build();
    }

    /**
     * Generate a valid {@link Buyer}.
     */
    public static Buyer aBuyer(){
        return Buyer.BuyerBuilder.aBuyer()
                .withFullName( new Buyer.FullName( "Marie", "Durand", "1" ) )
                .build();
    }

    public static String getPartnerTransactionid(){
        return PARTNER_TRANSACTIONID;
    }

    /**
     * Generate a valid {@link ContractConfiguration}.
     */
    public static ContractConfiguration aContractConfiguration(){
        Map<String, ContractProperty> contractProperties = new HashMap<>();

        contractProperties.put(Constants.ContractConfigurationKeys.CHECKBOX_PARAMETER,
                new ContractProperty( "checked" ));
        contractProperties.put(Constants.ContractConfigurationKeys.INPUT_PARAMETER,
                new ContractProperty( "input text" ));
        contractProperties.put(Constants.ContractConfigurationKeys.LISTBOX_PARAMETER,
                new ContractProperty( ConfigurationServiceImpl.CHECK_OK ));
        contractProperties.put(Constants.ContractConfigurationKeys.NETWORK_LISTBOX_PARAMETER,
                new ContractProperty( "value 1" ));
        contractProperties.put(Constants.ContractConfigurationKeys.PASSW_PARAMETER,
                new ContractProperty( "secret" ));

        return new ContractConfiguration("Sandbox APM", contractProperties);
    }

    /**
     * Generate a valid {@link ContractParametersCheckRequest}.
     */
    public static ContractParametersCheckRequest aContractParametersCheckRequest(){
        return aContractParametersCheckRequestBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link ContractParametersCheckRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public static ContractParametersCheckRequest.CheckRequestBuilder aContractParametersCheckRequestBuilder(){
        return ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                .withAccountInfo( anAccountInfo() )
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withLocale( Locale.getDefault() )
                .withPartnerConfiguration( aPartnerConfiguration() );
    }

    /**
     * Generate a valid {@link Environment}.
     */
    public static Environment anEnvironment(){
        return new Environment("http://notificationURL.com",
                "http://redirectionURL.com",
                "http://redirectionCancelURL.com",
                true);
    }

    /**
     * Generate a valid, but not complete, {@link Order}
     */
    public static Order anOrder(){
        return Order.OrderBuilder.anOrder()
                .withDate( new Date() )
                .withAmount( aPaylineAmount() )
                .withReference( "ORDER-REF-123456" )
                .build();
    }

    /**
     * Generate a valid {@link PartnerConfiguration}.
     */
    public static PartnerConfiguration aPartnerConfiguration(){
        return new PartnerConfiguration( new HashMap<>(), new HashMap<>() );
    }

    /**
     * Generate a valid Payline Amount.
     */
    public static com.payline.pmapi.bean.common.Amount aPaylineAmount(){
        return new com.payline.pmapi.bean.common.Amount(BigInteger.valueOf(10000), Currency.getInstance("EUR"));
    }

    /**
     * Generate a valid {@link PaymentRequest}.
     */
    public static PaymentRequest aPaylinePaymentRequest(){
        return aPaylinePaymentRequestBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link PaymentRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public static PaymentRequest.Builder aPaylinePaymentRequestBuilder(){
        return PaymentRequest.builder()
                .withAmount( aPaylineAmount() )
                .withBrowser( aBrowser() )
                .withBuyer( aBuyer() )
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withLocale( Locale.getDefault() )
                .withOrder( anOrder() )
                .withPartnerConfiguration( aPartnerConfiguration() )
                .withPaymentFormContext( aPaymentFormContext() )
                .withPluginConfiguration( aPluginConfiguration() )
                .withSoftDescriptor( "softDescriptor" )
                .withTransactionId( aTransactionId() );
    }

    /**
     * Generate a valid {@link PaymentFormContext}.
     */
    public static PaymentFormContext aPaymentFormContext(){
        Map<String, String> paymentFormParameter = new HashMap<>();

        return PaymentFormContext.PaymentFormContextBuilder.aPaymentFormContext()
                .withPaymentFormParameter( paymentFormParameter )
                .withSensitivePaymentFormParameter( new HashMap<>() )
                .build();
    }

    /**
     * Generate a valid {@link PaymentFormConfigurationRequest}.
     */
    public static PaymentFormConfigurationRequest aPaymentFormConfigurationRequest(){
        return aPaymentFormConfigurationRequestBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link PaymentFormConfigurationRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public static PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder aPaymentFormConfigurationRequestBuilder(){
        return PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder.aPaymentFormConfigurationRequest()
                .withAmount( aPaylineAmount() )
                .withBuyer( aBuyer() )
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withLocale( Locale.getDefault() )
                .withOrder( anOrder() )
                .withPartnerConfiguration( aPartnerConfiguration() )
                .withPluginConfiguration( aPluginConfiguration() );
    }

    /**
     * Generate a valid {@link PaymentFormLogoRequest}.
     */
    public static PaymentFormLogoRequest aPaymentFormLogoRequest(){
        return PaymentFormLogoRequest.PaymentFormLogoRequestBuilder.aPaymentFormLogoRequest()
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withPartnerConfiguration( aPartnerConfiguration() )
                .withLocale( Locale.getDefault() )
                .build();
    }

    /**
     * Generate a valid plugin configuration, as a <code>String</code>.
     */
    public static String aPluginConfiguration(){
        return "pluginConfiguration";
    }

    /**
     * Generate a valid {@link RetrievePluginConfigurationRequest}.
     */
    public static RedirectionPaymentRequest aRedirectionPaymentRequest(){
        return (RedirectionPaymentRequest) aRedirectionPaymentRequestBuilder().build();
    }

    /**
     * Generate a valid {@link RedirectionPaymentRequest}.
     */
    public static RedirectionPaymentRequest.Builder aRedirectionPaymentRequestBuilder(){
        return (RedirectionPaymentRequest.Builder) RedirectionPaymentRequest.builder()
                .withAmount( aPaylineAmount() )
                .withBrowser( aBrowser() )
                .withBuyer( aBuyer() )
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withOrder( anOrder() )
                .withPartnerConfiguration( aPartnerConfiguration() )
                .withTransactionId( aTransactionId());
    }

    /**
     * Generate a valid {@link RetrievePluginConfigurationRequest}.
     */
    public static RetrievePluginConfigurationRequest aRetrievePluginConfigurationRequest(){
        return aRetrievePluginConfigurationRequestBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link RetrievePluginConfigurationRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public static RetrievePluginConfigurationRequest.RetrieveConfigurationRequestBuilder aRetrievePluginConfigurationRequestBuilder(){
        return RetrievePluginConfigurationRequest.RetrieveConfigurationRequestBuilder.aRetrieveConfigurationRequest()
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withPartnerConfiguration( aPartnerConfiguration() )
                .withPluginConfiguration( aPluginConfiguration() );
    }

    /**
     * @return a valid transaction ID.
     */
    public static String aTransactionId(){
        return "123456789012345678901";
    }

    /**
     * Generate a valid {@link TransactionStatusRequest}.
     */
    public static TransactionStatusRequest aTransactionStatusRequest(){
        return TransactionStatusRequest.TransactionStatusRequestBuilder.aNotificationRequest()
                .withAmount( aPaylineAmount() )
                .withBuyer( aBuyer() )
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withOrder( anOrder() )
                .withPartnerConfiguration( aPartnerConfiguration() )
                .withTransactionId( aTransactionId() )
                .build();
    }

    /**
     * @return a valid user agent.
     */
    public static String aUserAgent(){
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0";
    }

    public static PaymentFormContext aPaymentFormContextWithParameter() {
        Map<String, String> paymentFormParameter = new HashMap<>();

        paymentFormParameter.put("Key1", "Value1");
        paymentFormParameter.put("Key2", "Value2");
        paymentFormParameter.put("Key3", "Value3");

        Map<String, String> SensitivePaymentFormParameter = new HashMap<>();

        SensitivePaymentFormParameter.put("Key11", "Value11");
        SensitivePaymentFormParameter.put("Key12", "Value12");
        SensitivePaymentFormParameter.put("Key13", "Value13");

        return PaymentFormContext.PaymentFormContextBuilder.aPaymentFormContext()
                .withPaymentFormParameter( paymentFormParameter )
                .withSensitivePaymentFormParameter( SensitivePaymentFormParameter )
                .build();
    }
    public static ResetRequest aResetRequest() {
        return aResetRequestBuilder().build();
    }

    public static ResetRequest.ResetRequestBuilder aResetRequestBuilder() {
        return ResetRequest.ResetRequestBuilder.aResetRequest()
                .withAmount(aPaylineAmount())
                .withOrder(anOrder())
                .withBuyer(aBuyer())
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withTransactionId(TRANSACTIONID)
                .withPartnerTransactionId(PARTNER_TRANSACTIONID)
                .withPartnerConfiguration(aPartnerConfiguration());
    }

    public static NotificationRequest aNotificationRequest() {
        return aNotificationRequestBuilder().build();
    }

    public static NotificationRequest.NotificationRequestBuilder aNotificationRequestBuilder(){
        return NotificationRequest.NotificationRequestBuilder
                .aNotificationRequest()
                .withContent(new ByteArrayInputStream("40000".getBytes()))
                .withHttpMethod("GET")
                .withPathInfo("foo")
                .withContractConfiguration(aContractConfiguration())
                .withHeaderInfos(new HashMap<>());
    }
}
