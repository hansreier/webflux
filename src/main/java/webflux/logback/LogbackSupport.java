package webflux.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***********************************************************************************
 * Helper class for logback
 * The purpost is to verify that logback correctly configured i logback.xml
 * Verify by visual inspection in logs (console or file logging)
 * TODO: Logging requirements, look link
 * https://wiki.sits.no/pages/viewpage.action?pageId=34578366
 * console log and file log should be activate according to this
 * perhaps refer to logback file to get rollinng policy like specified in requirements
 ************************************************************************************/
public final  class LogbackSupport {

    private static final Logger LOG = LoggerFactory.getLogger(LogbackSupport.class);

    private LogbackSupport() { }

    //kalles gjerne ved oppstart av container
    public static void outputLogbackConfig() {
        LoggerContext lc = ((ch.qos.logback.classic.Logger) LOG).getLoggerContext();
        StatusPrinter.print(lc);
        lc.getStatusManager().getCopyOfStatusList().forEach(l ->
                LOG.info("Logback: {}", l.getMessage())
        );
        LOG.info("INFO log test message");
        LOG.debug("DEBUG log test message");
        LOG.trace("TRACE log test message");
    }
}

