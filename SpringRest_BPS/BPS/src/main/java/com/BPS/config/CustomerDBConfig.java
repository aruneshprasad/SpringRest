package com.BPS.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.BPS.customer.entities.CustomerDetails;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
@EnableJpaRepositories(entityManagerFactoryRef = "customerEntityManager", 
						transactionManagerRef = "customerTransactionManager", 
						basePackages = "com.BPS.customer.dao")

public class CustomerDBConfig {
	
	@Autowired
	private Environment env;
	
	@Primary
	@Bean
	public DataSource getCustomerDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(env.getProperty("spring.customer.datasource.driver.class"));
		dataSource.setUrl(env.getProperty("spring.customer.datasource.url"));
		dataSource.setUsername(env.getProperty("spring.customer.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.customer.datasource.password"));
		return dataSource;
	}

	@Primary
	@Bean(name = "customerEntityManager")
	public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(EntityManagerFactoryBuilder builder) {
		return builder.dataSource(getCustomerDataSource()).properties(hibernateProperties()).packages(CustomerDetails.class)
				.persistenceUnit("customerPU").build();
	}

	@Primary
	@Bean(name = "customerTransactionManager")
	public PlatformTransactionManager customerTransactionManager(
			@Qualifier("customerEntityManager") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	private Map<String, Object> hibernateProperties() {

		Resource resource = new ClassPathResource("hibernate.properties");
		try {
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			return properties.entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
		} catch (IOException e) {
			return new HashMap<String, Object>();
		}
	}
}