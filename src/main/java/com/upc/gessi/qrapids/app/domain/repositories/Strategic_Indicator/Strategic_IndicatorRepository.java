package com.upc.gessi.qrapids.app.domain.repositories.Strategic_Indicator;

import com.upc.gessi.qrapids.app.domain.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface Strategic_IndicatorRepository  extends JpaRepository<Feedback, Long>, PagingAndSortingRepository<Feedback, Long>, CustomStrategic_IndicatorRepository {
}
