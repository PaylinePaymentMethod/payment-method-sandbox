package com.payline.payment.sandbox.utils;

/**
 * Support for constants used everywhere in the plugin sources.
 */
public class Constants {

    /**
     * Keys for the entries in ContractConfiguration map.
     */
    public static class ContractConfigurationKeys {

        public static final String CHECKBOX_PARAMETER = "checkbox";
        public static final String INPUT_PARAMETER = "input";
        public static final String LISTBOX_PARAMETER = "listbox";
        public static final String NETWORK_LISTBOX_PARAMETER = "networkListBox";
        public static final String PASSW_PARAMETER = "password";
        public static final String PAYMENT_FORM_LOGO_RESPONSE_TYPE = "PaymentFormLogoResponseType";

        /* Static utility class : no need to instantiate it (Sonar bug fix) */
        private ContractConfigurationKeys(){}
    }

    /**
     * Keys for the entries in PartnerConfiguration maps.
     */
    public static class PartnerConfigurationKeys {

        /* Static utility class : no need to instantiate it (Sonar bug fix) */
        private PartnerConfigurationKeys(){}
    }

    /**
     * Keys for the entries in RequestContext data.
     */
    public static class RequestContextKeys {

        /* Static utility class : no need to instantiate it (Sonar bug fix) */
        private RequestContextKeys(){}
    }

    /* Static utility class : no need to instantiate it (Sonar bug fix) */
    private Constants(){}

}
