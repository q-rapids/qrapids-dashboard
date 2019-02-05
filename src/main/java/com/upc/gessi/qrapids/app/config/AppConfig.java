package com.upc.gessi.qrapids.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class AppConfig {

	@Value("${spring.datasource.url}")
	private String url;

	@Value("${spring.datasource.driver-class-name}")
	private String driverClass;

	@Value("${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.password}")
	private String password;

	@Value("${spring.jpa.database-platform}")
	private String dialect;


    @Bean
	@Primary
	public DataSource dataSource() {

		DriverManagerDataSource dataSource = new DriverManagerDataSource();

		dataSource.setDriverClassName( this.driverClass );
		dataSource.setUrl( this.url );
		dataSource.setUsername( this.username);
		dataSource.setPassword( this.password );
		return dataSource;
	}


	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
		jpaVendorAdapter.setGenerateDdl(true);
		jpaVendorAdapter.setShowSql(true);
		jpaVendorAdapter.setDatabasePlatform( this.dialect );

		return jpaVendorAdapter;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

		LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
		lef.setPackagesToScan("com.upc.gessi.qrapids.app.domain.models");
		lef.setDataSource(dataSource());
		lef.setJpaVendorAdapter(jpaVendorAdapter());

		Properties properties = new Properties();
		properties.setProperty("hibernate.show_sql", "true");
		properties.setProperty("hibernate.jdbc.fetch_size", "100");
		properties.setProperty("hibernate.hbm2ddl.auto", "update");

		lef.setJpaProperties(properties);
		return lef;
	}

}
