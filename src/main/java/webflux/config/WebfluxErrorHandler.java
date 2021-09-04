package webflux.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**************************************************************************************
 * General error handler for all  WebFlux Rest calls
 *
 * https://www.baeldung.com/spring-webflux-errors
 * A different way of doing it, not used
 * https://medium.com/@akhil.bojedla/exception-handling-spring-webflux-b11647d8608
 ***************************************************************************************/
@Component
@Order(-2)
public class WebfluxErrorHandler extends AbstractErrorWebExceptionHandler {

    @Value("${server.error.include-stacktrace:}")
    private String includeStacktrace;

    @Value("${server.error.include-message:}")
    private String includeMessage;

    private static final Logger LOG = LoggerFactory.getLogger(WebfluxErrorHandler.class);

    public WebfluxErrorHandler(ErrorAttributes errorAttributes,
                               WebProperties.Resources resources,
                               ApplicationContext applicationContext,
                               ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        super.setMessageWriters(configurer.getWriters());
        super.setMessageReaders(configurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(
            ErrorAttributes errorAttributes) {
        return RouterFunctions.route(
                RequestPredicates.all(), this::renderErrorResponse);

    }

    private Mono<ServerResponse> renderErrorResponse(
            ServerRequest request) {
        LOG.info("Inside error handler server response");
        String stack = includeStacktrace;
        boolean stackTraceIncluded = false;
        // Defaults are empty for some reason, read manually from config
        // ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
        // ErrorAttributeOptions options = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE,
        //    ErrorAttributeOptions.Include.STACK_TRACE);
        List<ErrorAttributeOptions.Include> incl = new ArrayList<>();            stackTraceIncluded = true;

        if (includeMessage.equalsIgnoreCase("always")) {
            incl.add(ErrorAttributeOptions.Include.MESSAGE);
        }
        if (includeStacktrace.equalsIgnoreCase("always")) {
            incl.add(ErrorAttributeOptions.Include.STACK_TRACE);
        }
        ErrorAttributeOptions options = ErrorAttributeOptions.of(incl);
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request,
                options);
        HttpStatus httpStatus = HttpStatus.valueOf((int) errorPropertiesMap.get("status"));
        String path = errorPropertiesMap.get("path").toString();
        String message = errorPropertiesMap.get("message").toString();
        LOG.error("Error at: {} HttpStatus: {} Message: {}", path, httpStatus, message);
        if (stackTraceIncluded) {
            LOG.error("Detailed error message: \n {}", errorPropertiesMap.get("trace"));
        }
        return ServerResponse.status(httpStatus)
               // .contentType(MediaType.APPLICATION_JSON) //Why is only JSON supported?
                .body(BodyInserters.fromValue(errorPropertiesMap));
    }
}


