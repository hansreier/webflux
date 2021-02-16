package webflux.logback;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

@Slf4j
public class LogbackTest {

    @Test
    public void logbackPrintTest() { LogbackSupport.outputLogbackConfig(); };

    @Test
    public void LogbackConfigTest() {
        Assertions.assertEquals("OK", LogbackTestSupport.verifyLogbackConfig()); }
}
