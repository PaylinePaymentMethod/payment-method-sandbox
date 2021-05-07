package com.payline.payment.sandbox.service.impl;

import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.TransactionManagerService;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class TransactionManagerServiceImpl implements TransactionManagerService {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(TransactionManagerServiceImpl.class);

    @Override
    public Map<String, String> readAdditionalData(String s, String s1) {
        Map<String, String> queryPairs = new HashMap<>();

        String[] pairs = s.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            try {
                queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Could not deserialize or serialize due to a error :", e);

            }
        }
        return queryPairs;
    }

}
