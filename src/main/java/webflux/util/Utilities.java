package webflux.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import webflux.logback.LogbackSupport;

@Component
public class Utilities {

    private static final Logger LOG = LoggerFactory.getLogger(Utilities.class);

    @Autowired
    private ApplicationContext applicationContext;

    public void printBeans() {
        String[] beans = applicationContext.getBeanDefinitionNames();
        LOG.info("Logging all the Spring beans");
        for (String bean: beans) {
            LOG.info(bean);
        }
        LOG.info("completed");
    }
}
