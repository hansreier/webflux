package webflux.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
    public class WebfluxErrorAttributes extends DefaultErrorAttributes {

    private static final Logger LOG = LoggerFactory.getLogger(WebfluxErrorAttributes.class);

        @Override
        public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                      ErrorAttributeOptions options) {
            LOG.info("get Error attributes");
            Map<String, Object> map = super.getErrorAttributes(
                    request, options);
            //log.info(map.toString());
            //      map.put("status", HttpStatus.BAD_REQUEST);
            ///      map.put("message", "username is required");
            LOG.info("get Error attributes end");
            return map;
        }

    }
