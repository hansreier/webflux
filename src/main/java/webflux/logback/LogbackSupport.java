package webflux.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************
 * Helper class for logback
 * The purpost is to verify that logback correctly configured i logback.xml
 * Verify by visual inspection in logs (console or file logging)
 * TODO: Logging requirements, look link
 * https://wiki.sits.no/pages/viewpage.action?pageId=34578366
 * console log and file log should be activate according to this
 * perhaps refer to logback file to get rollinng policy like specified in requirements
 ************************************************************************************/
@Slf4j
public final  class LogbackSupport {
    private LogbackSupport() { }

    //kalles gjerne ved oppstart av container
    public static void outputLogbackConfig() {
        LoggerContext lc = ((ch.qos.logback.classic.Logger) log).getLoggerContext();
        StatusPrinter.print(lc);
        lc.getStatusManager().getCopyOfStatusList().forEach(l ->
                log.info("Logback: {}", l.getMessage())
        );
        log.info("INFO log test message");
        log.debug("DEBUG log test message");
        log.trace("TRACE log test message");
    }
}

