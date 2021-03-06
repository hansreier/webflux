package webflux.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.Map;

@Component
@Order(-2)
public class WebfluxErrorHandler extends AbstractErrorWebExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebfluxErrorHandler.class);

    public WebfluxErrorHandler(ErrorAttributes errorAttributes,
                               WebProperties.Resources resources,
                               ApplicationContext applicationContext,
                               ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }
    //A different way of doing it
    //https://medium.com/@akhil.bojedla/exception-handling-spring-webflux-b11647d8608

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(
            ErrorAttributes errorAttributes) {
        LOG.info("Inside error handler router function");
        return RouterFunctions.route(
                RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(
            ServerRequest request) {
        LOG.info("Inside error handler server response");
        //Defaults are empty, why?
        //ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
        ErrorAttributeOptions options = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE,
                ErrorAttributeOptions.Include.STACK_TRACE);
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request,
                options);
        //HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        HttpStatus httpStatus = HttpStatus.valueOf((int) errorPropertiesMap.get("status"));

        LOG.error("HttpStatus {}", httpStatus);
        return ServerResponse.status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorPropertiesMap));
    }
}

