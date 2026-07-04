package com.example.lockly.config.repository;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.lockly.repository.location",
        entityManagerFactoryRef = "locationEntityManagerFactory",
        transactionManagerRef = "locationTransactionManager"
)
@EntityScan(basePackages = "com.example.lockly.domain.entity.location")
public class LocationDatabaseConfig {

    @Bean
    @ConfigurationProperties("location.datasource")
    public DataSourceProperties locationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource locationDataSource() {
        return locationDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "locationEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean locationEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {

        return builder
                .dataSource(locationDataSource())
                .packages("com.example.lockly.domain.entity.location")
                .persistenceUnit("location")
                .build();
    }

    @Bean(name = "locationTransactionManager")
    public PlatformTransactionManager locationTransactionManager(
            @Qualifier("locationEntityManagerFactory")
            EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(entityManagerFactory);
    }

}