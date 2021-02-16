package webflux.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@ComponentScan("webflux")
@EnableWebFlux
//@EnableAutoConfiguration
public class AppConfig {
}
