package com.payline.payment.sandbox.service.impl;

import com.payline.payment.sandbox.utils.Constants;
import com.payline.payment.sandbox.utils.PaymentResponseUtil;
import com.payline.payment.sandbox.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.AvailableNetwork;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.*;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.configuration.request.RetrievePluginConfigurationRequest;
import com.payline.pmapi.service.ConfigurationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConfigurationServiceImpl implements ConfigurationService {

    private static final String CHECK_ERROR_NON_REQUIRED = "CHECK_ERROR_NON_REQUIRED";
    private static final String CHECK_ERROR_REQUIRED = "CHECK_ERROR_REQUIRED";
    private static final String CHECK_EXCEPTION = "CHECK_EXCEPTION";
    private static final String CHECK_NULL_RESPONSE = "CHECK_NULL_RESPONSE";
    public static final String CHECK_OK = "CHECK_OK";

    private ReleaseProperties releaseProperties = ReleaseProperties.getInstance();

    @Override
    public List<AbstractParameter> getParameters(Locale locale) {
        List<AbstractParameter> parameters = new ArrayList<>();

        /* Possible behaviours for the check method */
        Map<String, String> behaviours = new HashMap<>();
        behaviours.put(CHECK_OK, "OK");
        behaviours.put(CHECK_ERROR_REQUIRED, "Error on a required field");
        behaviours.put(CHECK_ERROR_NON_REQUIRED, "Error on a non required field");
        behaviours.put(CHECK_NULL_RESPONSE, "The check method returns null");
        behaviours.put(CHECK_EXCEPTION, "The check method throws a exception");

        /* ListBoxParameter */
        ListBoxParameter checkBehaviour = new ListBoxParameter();
        checkBehaviour.setDescription("Comportement attendu pour la méthode check()");
        checkBehaviour.setKey(Constants.ContractConfigurationKeys.LISTBOX_PARAMETER);
        checkBehaviour.setLabel("Check behaviour");
        checkBehaviour.setRequired(true);
        checkBehaviour.setList( behaviours );

        /* InputParameter */
        InputParameter inputParameter = new InputParameter();
        inputParameter.setDescription("Input parameter");
        inputParameter.setKey(Constants.ContractConfigurationKeys.INPUT_PARAMETER);
        inputParameter.setLabel("Input");
        inputParameter.setRequired(false);

        /* CheckboxParameter */
        CheckboxParameter checkboxParameter = new CheckboxParameter();
        checkboxParameter.setDescription("Checkbox parameter");
        checkboxParameter.setKey(Constants.ContractConfigurationKeys.CHECKBOX_PARAMETER);
        checkboxParameter.setLabel("Checkbox");
        checkboxParameter.setRequired(false);

        /* NetworkListBoxParameter */
        Map<String, String> nlbList = new HashMap<>();
        nlbList.put("key1", "value 1");
        nlbList.put("key2", "value 2");
        nlbList.put("key3", "value 3");
        NetworkListBoxParameter nlbParameter = new NetworkListBoxParameter();
        nlbParameter.setDescription("Network list box parameter");
        nlbParameter.setKey(Constants.ContractConfigurationKeys.NETWORK_LISTBOX_PARAMETER);
        nlbParameter.setLabel("NetworkListBox");
        nlbParameter.setList( nlbList );
        nlbParameter.setNetwork(AvailableNetwork.CB);
        nlbParameter.setRequired(false);

        /* PasswordParameter */
        PasswordParameter passwordParameter = new PasswordParameter();
        passwordParameter.setDescription("Password parameter");
        passwordParameter.setKey(Constants.ContractConfigurationKeys.PASSW_PARAMETER);
        passwordParameter.setLabel("Password");
        passwordParameter.setRequired(false);

        return parameters;
    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {

        if(contractParametersCheckRequest.getContractConfiguration() != null){
            PaymentResponseUtil.apiResponseDelay(contractParametersCheckRequest.getContractConfiguration());
        }

        Map<String, String> errors = new HashMap<>();

        // Retrieve the expected behaviour in the listbox parameter
        String behaviour = contractParametersCheckRequest.getAccountInfo()
                .get(Constants.ContractConfigurationKeys.LISTBOX_PARAMETER);

        if (behaviour == null) {
            errors.put("listbox", "ListBox parameter is null");
            return errors;
        }

        switch( behaviour ){
            case CHECK_OK:
                return errors;
            case CHECK_ERROR_REQUIRED:
                errors.put("listbox", "Error on the field listbox");
                return errors;
            case CHECK_ERROR_NON_REQUIRED:
                errors.put("input", "Error on the field input");
                return errors;
            case CHECK_NULL_RESPONSE:
                return null;
            case CHECK_EXCEPTION:
                throw new NullPointerException("Simulate a NullPointerException thrown by the plugin");
            default:
                throw new IllegalArgumentException("Unknown expected behaviour: " + behaviour);
        }
    }

    @Override
    public ReleaseInformation getReleaseInformation() {
        return ReleaseInformation.ReleaseBuilder.aRelease()
                .withDate(LocalDate.parse(releaseProperties.get("release.date"), DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .withVersion(releaseProperties.get("release.version"))
                .build();
    }

    @Override
    public String getName(Locale locale) {
        return "Sandbox APM";
    }

    @Override
    public String retrievePluginConfiguration(RetrievePluginConfigurationRequest retrievePluginConfigurationRequest) {
        return "pluginConfiguration";
    }
}
