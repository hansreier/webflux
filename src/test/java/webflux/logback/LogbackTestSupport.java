package webflux.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.util.StatusPrinter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogbackTestSupport {
    //use in unit tests to verity logback configuration
    public static String verifyLogbackConfig() {
        String checkMessage = "Found resource [logback-test.xml]";
        String defaultConfigMessage = "Setting up default configuration";
        boolean found = false;
        boolean standardConfig = false;
        LoggerContext lc = ((ch.qos.logback.classic.Logger) log).getLoggerContext();
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
        log.error("ERROR log test messge");
        log.warn("WARNING log test message");
        log.info("INFO log test message ");
        log.debug("DEBUG log test message");
        log.trace("TRACE log test message");

        if (found) {
            return "OK";
        } else {
            return "Cannot find logback-test.xml. Uses standard config: " + standardConfig;
        }
    }
}


