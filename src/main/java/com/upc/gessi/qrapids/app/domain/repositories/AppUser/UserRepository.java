package com.upc.gessi.qrapids.app.domain.repositories.AppUser;

import com.upc.gessi.qrapids.app.domain.models.AppUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends CrudRepository<AppUser, Long>, PagingAndSortingRepository<AppUser,Long> {

	AppUser findByUsername(String username);

	AppUser findByEmail (String email);
}
