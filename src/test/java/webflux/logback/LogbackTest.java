package webflux.logback;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackTest {

    private static final Logger LOG = LoggerFactory.getLogger(LogbackTest.class);

    @Test
    public void logbackPrintTest() { LogbackSupport.outputLogbackConfig(); };

    @Test
    public void LogbackConfigTest() {
        Assertions.assertEquals("OK", LogbackTestSupport.verifyLogbackConfig()); }
}
