package com.upc.gessi.qrapids.app.domain.repositories.UserGroup;

import com.upc.gessi.qrapids.app.domain.models.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long>, PagingAndSortingRepository<UserGroup,Long>, CustomUserGroupRepository{
}