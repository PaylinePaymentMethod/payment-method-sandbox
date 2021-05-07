package com.payline.payment.sandbox.utils;

import com.payline.pmapi.logger.LogManager;

public class Logger {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Logger.class);


    private Logger(){

    }


    public static void log(String service, String method, String magicalAmount, String useCase) {
        LOG.info("Service:{}, method:{}, amount:{}, case:{}", service, method, magicalAmount, useCase);
    }

}
