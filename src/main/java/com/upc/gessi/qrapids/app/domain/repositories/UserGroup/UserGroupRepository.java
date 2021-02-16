package com.upc.gessi.qrapids.app.domain.repositories.UserGroup;

import com.upc.gessi.qrapids.app.domain.models.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    UserGroup findByName(String name);

    UserGroup findByDefaultGroupIsTrue();

    boolean existsByDefaultGroupIsTrue();

    @Modifying
    @Query("UPDATE UserGroup u SET u.defaultGroup = CASE WHEN u.id = ?1 THEN true ELSE false END")
    void updateUserGroupDefault(long id);
}