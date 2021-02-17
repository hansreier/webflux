package webflux.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;

@TestConfiguration
@ComponentScan("webflux")
@EnableWebFlux
//@EnableAutoConfiguration
public class AppTestConfig {
}
