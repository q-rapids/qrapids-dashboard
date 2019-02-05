package com.upc.gessi.qrapids.app.domain.repositories.AppUser;

import com.upc.gessi.qrapids.app.domain.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends JpaRepository<AppUser, Long>, PagingAndSortingRepository<AppUser,Long>, CustomUserRepository {
	AppUser findByUsername(String username);
}
