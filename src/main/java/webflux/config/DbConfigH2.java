package webflux.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.h2.H2ConnectionOption;
import reactor.util.annotation.NonNull;

//https://spring.io/guides/gs/accessing-data-r2dbc/
//https://dassum.medium.com/building-a-reactive-restful-web-service-using-spring-boot-and-postgres-c8e157dbc81d
//https://stackoverflow.com/questions/64355106/setup-h2-in-spring-boot-application-with-r2dbc-and-flyway
@Configuration
@EnableR2dbcRepositories(basePackages = {"webflux.repository"})
@ComponentScan({"webflux"})
@PropertySource("classpath:db_h2.properties")
public class DbConfigH2 extends AbstractR2dbcConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(DbConfigH2.class);

    @Value("${r2dbc.h2.dbname}")
    private String dbname;

    @Value("${r2dbc.h2.username}")
    private String username;

    @Value("${r2dbc.h2.password}")
    private String password;

    @Value("${r2dbc.h2.dbclosedelay}")
    private String dbCloseDelay;

    @Value("${r2dbc.h2.dbcloseonexit}")
    private String dbCloseOnExit;

    @Value("${r2dbc.h2.mode}")
    private String mode;

    //#url: r2dbc:h2:mem:///test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL
    //https://r2dbc.io/spec/0.8.2.RELEASE/spec/html/#overview.connection.url
    //https://hantsy.medium.com/introduction-to-r2dbc-82058417644b
    @Override
    @Bean
    @NonNull
    public H2ConnectionFactory connectionFactory() {
        LOG.info("H2 User {}", username);
        return new H2ConnectionFactory(

                H2ConnectionConfiguration.builder()
                       // .file("testdb")
                        .inMemory(dbname)
                        .property(H2ConnectionOption.DB_CLOSE_DELAY, dbCloseDelay)
                        .property(H2ConnectionOption.DB_CLOSE_ON_EXIT, dbCloseOnExit)
                        .property(H2ConnectionOption.MODE, mode)
                        .username(username)
                        .password(password)
                        .build()
        );
    }

    @Bean
    public ConnectionFactoryInitializer initializer(
            H2ConnectionFactory connectionFactory) {
        LOG.info("H2 config initializing connection and database");
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("create.sql")));
        return initializer;
    }
}

