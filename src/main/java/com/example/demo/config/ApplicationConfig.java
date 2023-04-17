package com.example.demo.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class ApplicationConfig {

    @Autowired
    private Environment env;

    public ApplicationConfig() {
    }

    @Primary
    @Bean(
            name = {"dataSourceApp"}
    )
    @ConfigurationProperties(
            prefix = "spring.datasource"
    )
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(
            name = {"entityManagerFactory"}
    )
    @DependsOn({"flywayApp"})
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("dataSourceApp") DataSource dataSource) {
        Map<String, Object> jpaProperties = new HashMap();
        jpaProperties.put("hibernate.hbm2ddl.auto", this.env.getProperty("spring.jpa.hibernate.ddl-auto"));
        jpaProperties.put("hibernate.show-sql", this.env.getProperty("spring.jpa.show-sql"));
        return builder.dataSource(dataSource).packages(new String[]{"ru.sbrf.aps.database.application.entity"}).persistenceUnit("application").properties(jpaProperties).build();
    }

    @Bean(
            initMethod = "migrate",
            name = {"flywayApp"}
    )
    public Flyway flywayAppBeforeInitModel(@Qualifier("dataSourceApp") DataSource dataSource) {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setLockRetryCount(-1);
        return new Flyway(configuration);
    }

    @Primary
    @Bean(
            name = {"transactionManager"}
    )
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
