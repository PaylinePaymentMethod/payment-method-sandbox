package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.exception.PluginException;
import com.payline.payment.sandbox.utils.Logger;
import com.payline.payment.sandbox.utils.PaymentResponseUtil;
import com.payline.payment.sandbox.utils.service.AbstractService;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.paymentform.bean.PaymentFormLogo;
import com.payline.pmapi.bean.paymentform.bean.field.SelectOption;
import com.payline.pmapi.bean.paymentform.bean.form.BankTransferForm;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.bean.form.NoFieldForm;
import com.payline.pmapi.bean.paymentform.bean.form.PartnerWidgetForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseFailure;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseProvided;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.bean.paymentform.response.logo.PaymentFormLogoResponse;
import com.payline.pmapi.bean.paymentform.response.logo.impl.PaymentFormLogoResponseFile;
import com.payline.pmapi.bean.paymentform.response.logo.impl.PaymentFormLogoResponseLink;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentFormConfigurationService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PaymentFormConfigurationServiceImpl extends AbstractService<PaymentFormConfigurationResponse> implements PaymentFormConfigurationService {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(PaymentFormConfigurationServiceImpl.class);
    private static final String GET_PAYMENT_FORM_CONFIGURATION = "getPaymentFormConfiguration";
    private static final String PAYMENT_FORM_LOGO_RESPONSE_FILE = "PaymentFormLogoResponseFile";
    private static final String PAYMENT_FORM_LOGO_RESPONSE_LINK = "PaymentFormLogoResponseLink";
    private static final String SANDBOX_APM = "Sandbox APM";
    private static final String SANDBOX_APM_LOGO = "Sandbox APM logo";

    @Override
    public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest paymentFormConfigurationRequest) {
        this.verifyRequest(paymentFormConfigurationRequest);

        String amount = paymentFormConfigurationRequest.getAmount().getAmountInSmallestUnit().toString();

        /* Default case */
        NoFieldForm noFieldForm = NoFieldForm.NoFieldFormBuilder.aNoFieldForm()
                .withDisplayButton(true)
                .withButtonText("Button text")
                .withDescription("")
                .build();

        PaymentFormConfigurationResponseSpecific noFieldResponse = PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder
                .aPaymentFormConfigurationResponseSpecific()
                .withPaymentForm( noFieldForm )
                .build();


        // The service tested by this request is not PaymentFormConfigurationService. So the plugin returns a NoField form.
        if( !amount.startsWith("3") ){
            return noFieldResponse;
        }

        switch( amount ){
            /* PaymentFormConfigurationResponseSpecific */
            case "30000":
                Logger.log(this.getClass().getSimpleName(),GET_PAYMENT_FORM_CONFIGURATION, amount, "PaymentFormConfigurationResponseSpecific NoField");
                return noFieldResponse;
            case "30001":
                Logger.log(this.getClass().getSimpleName(),GET_PAYMENT_FORM_CONFIGURATION, amount, "PaymentFormConfigurationResponseSpecific BankTransferForm");
                // retrieve the banks list from PluginConfiguration
                if( paymentFormConfigurationRequest.getPluginConfiguration() == null ){
                    throw new IllegalArgumentException("PaymentFormConfigurationRequest is missing a PluginConfiguration");
                }

                // Fake bank list creation
                final List<SelectOption> banks = new ArrayList<>();
                for (int x = 1; x < 3; x++) {
                    banks.add(SelectOption.SelectOptionBuilder.aSelectOption()
                            .withKey("bankId" + x)
                            .withValue("bank name " + x)
                            .build());
                }

                // Build form
                CustomForm form = BankTransferForm.builder()
                        .withBanks( banks )
                        .withDescription( "Description" )
                        .withDisplayButton( true )
                        .withButtonText( "Payer" )
                        .withCustomFields( new ArrayList<>() )
                        .build();

                return PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder
                        .aPaymentFormConfigurationResponseSpecific()
                        .withPaymentForm( form )
                        .build();

            case "30002":
                Logger.log(this.getClass().getSimpleName(), GET_PAYMENT_FORM_CONFIGURATION, amount, "PaymentFormConfigurationResponseSpecific avec un CustomForm complet");

                // Build form
                CustomForm customForm = PaymentResponseUtil.aCustomForm();

                return PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder
                        .aPaymentFormConfigurationResponseSpecific()
                        .withPaymentForm(customForm)
                        .build();
            case "30003":
                Logger.log(this.getClass().getSimpleName(), GET_PAYMENT_FORM_CONFIGURATION, amount, "PaymentFormConfigurationResponseSpecific avec un PartnerWidgetForm complet");

                // Build form
                PartnerWidgetForm partnerWidgetForm = PaymentResponseUtil.aPartnerWidgetForm();

                return PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder
                        .aPaymentFormConfigurationResponseSpecific()
                        .withPaymentForm(partnerWidgetForm)
                        .build();

            /* PaymentFormConfigurationResponseFailure */
            case "30100":
                Logger.log(this.getClass().getSimpleName(),GET_PAYMENT_FORM_CONFIGURATION, amount, "PaymentFormConfigurationResponseFailure avec failureCause (INVALID_DATA) &  errorCode (<= 50 caractères)");
                return PaymentFormConfigurationResponseFailure.PaymentFormConfigurationResponseFailureBuilder.aPaymentFormConfigurationResponseFailure()
                        .withErrorCode("Error code less than 50 characters long")
                        .withFailureCause( FailureCause.INVALID_DATA )
                        .build();
            case "30101":
                Logger.log(this.getClass().getSimpleName(),GET_PAYMENT_FORM_CONFIGURATION, amount, "PaymentFormConfigurationResponseFailure avec failureCause (INVALID_DATA) &  errorCode (> 50 caractères)");
                return PaymentFormConfigurationResponseFailure.PaymentFormConfigurationResponseFailureBuilder.aPaymentFormConfigurationResponseFailure()
                        .withErrorCode("This error code has not been truncated and is more than 50 characters long")
                        .withFailureCause( FailureCause.INVALID_DATA )
                        .build();
            case "30102":
                Logger.log(this.getClass().getSimpleName(),GET_PAYMENT_FORM_CONFIGURATION, amount, "PaymentFormConfigurationResponseFailure avec failureCause (INVALID_DATA) &  errorCode (<= 50 caractères) & partnerTransactionId");
                return PaymentFormConfigurationResponseFailure.PaymentFormConfigurationResponseFailureBuilder.aPaymentFormConfigurationResponseFailure()
                        .withErrorCode("Error code less than 50 characters long")
                        .withFailureCause( FailureCause.INVALID_DATA )
                        .withPartnerTransactionId( PaymentResponseUtil.PARTNER_TRANSACTION_ID )
                        .build();

            /* PaymentFormConfigurationResponseProvided */
            case "30200":
                Logger.log(this.getClass().getSimpleName(),GET_PAYMENT_FORM_CONFIGURATION, amount, "PaymentFormConfigurationResponseProvided");
                return PaymentFormConfigurationResponseProvided.PaymentFormConfigurationResponseBuilder.aPaymentFormConfigurationResponse()
                        .withContextPaymentForm( new HashMap<>() )
                        .build();

            /* Generic plugin errors */
            default:
                return super.generic(this.getClass().getSimpleName(),GET_PAYMENT_FORM_CONFIGURATION, amount );
        }
    }

    /**
     * Return a PaymentFormLogoResponse according the magic amount set in the contractConfiguration of a PaymentFormLogoRequest
     * @param paymentFormLogoRequest
     * @return
     */
    @Override
    public PaymentFormLogoResponse getPaymentFormLogo(PaymentFormLogoRequest paymentFormLogoRequest) {
        this.verifyRequest(paymentFormLogoRequest);

        // PaymentFormLogoResponse builder parameter initialisation
        String type = "";
        int height = 58;
        int width = 156;
        String title = SANDBOX_APM;
        String alt = SANDBOX_APM_LOGO;
        URL url = null;

        try {
            url = new URL("http://www.google.fr");
        } catch (MalformedURLException e) {
           LOGGER.info("Unable to create an url : {0    } ",  e);
        }

        // Set PaymentFormLogoResponse parameter according to the amount
        if (paymentFormLogoRequest.getContractConfiguration().getContractProperties().containsKey("PaymentFormLogoResponseType")) {
            String paymentFormLogoResponseType = paymentFormLogoRequest.getContractConfiguration().getContractProperties().get("PaymentFormLogoResponseType").getValue();
            switch (paymentFormLogoResponseType) {
                case "30401":
                    width = -156;
                    type = PAYMENT_FORM_LOGO_RESPONSE_LINK;
                    break;
                case "30402":
                    height = -58;
                    type = PAYMENT_FORM_LOGO_RESPONSE_LINK;
                    break;
                case "30403":
                    url = null;
                    type = PAYMENT_FORM_LOGO_RESPONSE_LINK;
                    break;
                case "30404":
                    title = null;
                    type = PAYMENT_FORM_LOGO_RESPONSE_LINK;
                    break;
                case "30405":
                    alt = null;
                    type = PAYMENT_FORM_LOGO_RESPONSE_LINK;
                    break;
                case "30406":
                    width = -156;
                    type = PAYMENT_FORM_LOGO_RESPONSE_FILE;
                    break;
                case "30407":
                    height = -58;
                    type = PAYMENT_FORM_LOGO_RESPONSE_FILE;
                    break;
                case "30408":
                    title = null;
                    type = PAYMENT_FORM_LOGO_RESPONSE_FILE;
                    break;
                case "30409":
                    alt = null;
                    type = PAYMENT_FORM_LOGO_RESPONSE_FILE;
                    break;
                default:
                    LOGGER.info("Unable to find the amount set in the PaymentFormLogoResponseType key");
            }

            try {
                // Return the PaymentFormLogoResponse according to the amount set in the contractConfiguration
                if (type.equals(PAYMENT_FORM_LOGO_RESPONSE_LINK)) {
                    return PaymentFormLogoResponseLink.PaymentFormLogoResponseLinkBuilder.aPaymentFormLogoResponseLink()
                            .withHeight(height)
                            .withWidth(width)
                            .withTitle(title)
                            .withAlt(alt)
                            .withUrl(url)
                            .build();
                } else if (type.equals(PAYMENT_FORM_LOGO_RESPONSE_FILE)) {
                    return PaymentFormLogoResponseFile.PaymentFormLogoResponseFileBuilder.aPaymentFormLogoResponseFile()
                            .withHeight(height)
                            .withWidth(width)
                            .withTitle(title)
                            .withAlt(alt)
                            .build();
                }


            } catch (IllegalStateException e) {
                // if the PaymentFormLogoResponse builder throw an exception because of an error in the parameter, we log it and return a correct PaymentLogoResponse
                LOGGER.error("Unable create an incorrect PaymentFormLogoResponse :", e);
            }
        }
        // Return a correct PaymentFormLogoResponse according to the type of response expected by the caller
        if (type.equals(PAYMENT_FORM_LOGO_RESPONSE_LINK)) {
            try {
                return PaymentFormLogoResponseLink.PaymentFormLogoResponseLinkBuilder.aPaymentFormLogoResponseLink()
                        .withHeight(58)
                        .withWidth(156)
                        .withTitle(SANDBOX_APM)
                        .withAlt(SANDBOX_APM_LOGO)
                        .withUrl(new URL("http://www.google.fr"))
                        .build();
            } catch (MalformedURLException e) {
                LOGGER.info("Unable to create an url : ", e);
            }
        }

        return PaymentFormLogoResponseFile.PaymentFormLogoResponseFileBuilder.aPaymentFormLogoResponseFile()
                .withHeight(58)
                .withWidth(156)
                .withTitle(SANDBOX_APM)
                .withAlt(SANDBOX_APM_LOGO)
                .build();
    }


    @Override
    public PaymentFormLogo getLogo(String s, Locale locale) {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream("payline_logo.png")) {
            if (input == null) {
                LOGGER.error("Unable to load the logo file" );
                throw new PluginException("Plugin error: unable to load the logo file");
            }
                // Read logo file
                BufferedImage logo = ImageIO.read(input);
                return recoverByteArrayFromImage(logo);

        } catch (IOException e) {
            LOGGER.error("Unable to load the logo file: ", e);
            throw new PluginException("Plugin error: unable to load the logo file : ", e);
        }
    }

    /**
     *   Recover byte array from image to return a PaymentFormLogo
     * @param logo
     * @return
     */
    private PaymentFormLogo recoverByteArrayFromImage(BufferedImage logo){
        // Recover byte array from image
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(logo, "png", baos);

            return PaymentFormLogo.PaymentFormLogoBuilder.aPaymentFormLogo()
                    .withFile(baos.toByteArray())
                    .withContentType("image/png")
                    .build();

        }catch (IOException e) {
            LOGGER.error("Unable to recover byte array from image : ", e);
            throw new PluginException("Plugin error: unable to recover byte array from image : ", e);
        }
    }

    /**
     * Performs standard verification of the request content.
     * Checks that every field required in the request is filled
     * @param paymentFormConfigurationRequest the request
     */
    private void verifyRequest(PaymentFormConfigurationRequest paymentFormConfigurationRequest) {
        if( paymentFormConfigurationRequest.getAmount() == null ){
            throw new IllegalArgumentException( "The PaymentFormConfigurationRequest is missing an amount" );
        }
        if( paymentFormConfigurationRequest.getLocale() == null ){
            throw new IllegalArgumentException( "The PaymentFormConfigurationRequest is missing a locale" );
        }
    }

    /**
     * Performs standard verification of the request content.
     *      * Checks that every field required in the request is filled
     * @param paymentFormLogoRequest the request
     */
    private void verifyRequest(PaymentFormLogoRequest paymentFormLogoRequest) {
        if( paymentFormLogoRequest.getLocale() == null ){
            throw new IllegalArgumentException( "The PaymentFormLogoRequest is missing a locale" );
        }
    }

}
