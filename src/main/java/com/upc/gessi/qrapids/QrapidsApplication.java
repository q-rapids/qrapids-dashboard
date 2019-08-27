package com.upc.gessi.qrapids;

import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
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
		ConfigurableApplicationContext context = SpringApplication.run(QrapidsApplication.class, args);
		try {
			context.getBean(StrategicIndicatorsController.class).fetchStrategicIndicators();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
