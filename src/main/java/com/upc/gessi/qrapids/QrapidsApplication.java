package com.upc.gessi.qrapids;

import com.upc.gessi.qrapids.app.domain.controllers.*;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.MetricCategory;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.presentation.rest.services.Alerts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.*;
import eval2.Eval;
import java.time.LocalDate;

@SpringBootApplication
@EnableScheduling
public class QrapidsApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(QrapidsApplication.class);
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Value("${projects.dir:}") // default -> empty string
	private String projectsDir;

	static ConfigurableApplicationContext context;

	/*@Scheduled(cron = "${cron.expression:-}") // default -> disable scheduled task
	public void scheduleTask() throws ParseException, ProjectNotFoundException, IOException, CategoriesException {
		// ToDo: decide if we also copy this code to assessSI function
		Logger logger = LoggerFactory.getLogger(Alerts.class);
		logger.info("Start Scheduled task: " + new Timestamp(System.currentTimeMillis()));
		logger.info("projects dir: " + projectsDir);
		LocalDate evaluationLocalDate = LocalDate.now(); // we need LocalDate for assessStrategicIndicators
		Date evaluationDate= Date.from(evaluationLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()); // we need Date for evaluateQualityModel in qrapids-eval libs

		// params config:
		// 					projects dir path, evaluationDate, null
		//					projects dir path, fromDate, toDate
		Eval.evaluateQualityModel(projectsDir, evaluationDate, null);

		boolean correct = true;
		correct = context.getBean(FactorsController.class).assessQualityFactors(null, evaluationLocalDate);

		if (correct) {
			correct = context.getBean(StrategicIndicatorsController.class).assessStrategicIndicators(null, evaluationLocalDate);
		}

		if (!correct) { // check if the assessment complete with error
			logger.error(evaluationLocalDate + ": factors or strategic indicators assessment complete with error.");
		}
	}*/


	public static void main(String[] args) throws Exception {

		context = SpringApplication.run(QrapidsApplication.class, args);

		// Check the categories in the SQL database and if they are empty create the default ones
		List<SICategory> siCategoryList = context.getBean(StrategicIndicatorsController.class).getStrategicIndicatorCategories();
		List<QFCategory> factorCategoryList = context.getBean(FactorsController.class).getFactorCategories();
		List<MetricCategory> metricCategoryList = context.getBean(MetricsController.class).getMetricCategories();

		try {
			// Declare default categories
			List<Map<String, String>> categories = new ArrayList<>();
			Map<String,String> cat1 = new HashMap<>();
			cat1.put("color", "#00ff00");
			cat1.put("name", "Good");
			cat1.put("upperThreshold", "100");
			categories.add(cat1);
			Map<String,String> cat2 = new HashMap<>();
			cat2.put("color", "#ff8000");
			cat2.put("name", "Normal");
			cat2.put("upperThreshold", "67");
			categories.add(cat2);
			Map<String,String> cat3 = new HashMap<>();
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
				context.getBean(FactorsController.class).newFactorCategories(categories);
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
