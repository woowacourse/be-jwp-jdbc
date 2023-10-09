package aop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("test;DB_CLOSE_DELAY=-1;MODE=MYSQL;")
                .addScript("classpath:schema.sql")
                .build();
    }

//    @Bean
//    public PlatformTransactionManager platformTransactionManager() {
//        return new DataSourceTransactionManager(dataSource());
//    }
}
