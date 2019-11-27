package com.upc.gessi.qrapids;

import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.presentation.rest.services.Alerts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class QrapidsApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(QrapidsApplication.class);
	}

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

	public static void main(String[] args) {
		// Commented fetch function for avoid automatic add elasticsearch information to local SQL database.
		ConfigurableApplicationContext context = SpringApplication.run(QrapidsApplication.class, args);
		/*
		try {
			context.getBean(StrategicIndicatorsController.class).fetchStrategicIndicators();
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(Alerts.class);
			logger.error(e.getMessage(), e);
		}
		*/
	}
}
