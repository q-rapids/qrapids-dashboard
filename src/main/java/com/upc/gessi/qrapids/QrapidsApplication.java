package com.upc.gessi.qrapids;

import com.upc.gessi.qrapids.app.domain.controllers.MetricsController;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.models.MetricCategory;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.presentation.rest.services.Alerts;
import com.upc.gessi.qrapids.app.presentation.rest.services.QualityFactors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		// Check the categories in the SQL database and if they are empty create the default ones
		List<SICategory> siCategoryList = context.getBean(StrategicIndicatorsController.class).getStrategicIndicatorCategories();
		List<QFCategory> factorCategoryList = context.getBean(QualityFactorsController.class).getFactorCategories();
		List<MetricCategory> metricCategoryList = context.getBean(MetricsController.class).getMetricCategories();
		try {
			// Declare default categories
			List<Map<String, String>> categories = new ArrayList<Map<String, String>>();
			Map<String,String> cat1 = new HashMap<String, String>();
			cat1.put("color", "#00ff00");
			cat1.put("name", "Good");
			cat1.put("upperThreshold", "100");
			categories.add(cat1);
			Map<String,String> cat2 = new HashMap<String, String>();
			cat2.put("color", "#ff8000");
			cat2.put("name", "Neutral");
			cat2.put("upperThreshold", "67");
			categories.add(cat2);
			Map<String,String> cat3 = new HashMap<String, String>();
			cat3.put("color", "#ff0000");
			cat3.put("name", "Bad");
			cat3.put("upperThreshold", "33");
			categories.add(cat3);

			// Save Strategic Indicator categories
			if (siCategoryList.size() == 0) {
				context.getBean(StrategicIndicatorsController.class).newStrategicIndicatorCategories(categories);
			}
			// Save Factor categories
			if (factorCategoryList.size() == 0){
				context.getBean(QualityFactorsController.class).newFactorCategories(categories);
			}
			// Save Metric categories
			if (metricCategoryList.size() == 0) {
				context.getBean(MetricsController.class).newMetricCategories(categories);
			}
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(Alerts.class);
			logger.error(e.getMessage(), e);
		}
		/* Commented fetch function for avoid automatically add elasticsearch information to local SQL database.
		try {
			context.getBean(StrategicIndicatorsController.class).fetchStrategicIndicators();
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(Alerts.class);
			logger.error(e.getMessage(), e);
		}
		*/
	}
}
