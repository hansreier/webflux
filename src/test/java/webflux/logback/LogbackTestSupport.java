package webflux.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(LogbackTestSupport.class);
    //use in unit tests to verity logback configuration
    public static String verifyLogbackConfig() {
        String checkMessage = "Found resource [logback-test.xml]";
        String defaultConfigMessage = "Setting up default configuration";
        boolean found = false;
        boolean standardConfig = false;
        LoggerContext lc = ((ch.qos.logback.classic.Logger) LOG).getLoggerContext();
        for (Status s : lc.getStatusManager().getCopyOfStatusList()) {
            String message = s.getMessage();
            if (message.contains(checkMessage)) {
                found = true;
            }
            if (message.contains(defaultConfigMessage)) {
                standardConfig = true;
            }
        }
        StatusPrinter.print(lc);
        LOG.error("ERROR log test messge");
        LOG.warn("WARNING log test message");
        LOG.info("INFO log test message ");
        LOG.debug("DEBUG log test message");
        LOG.trace("TRACE log test message");

        if (found) {
            return "OK";
        } else {
            return "Cannot find logback-test.xml. Uses standard config: " + standardConfig;
        }
    }
}


