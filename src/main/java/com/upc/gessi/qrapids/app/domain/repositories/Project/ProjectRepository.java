package com.upc.gessi.qrapids.app.domain.repositories.Project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.models.Project;


public interface ProjectRepository extends JpaRepository<Project, Long>, PagingAndSortingRepository<Project,Long>, CustomProjectRepository{

}
