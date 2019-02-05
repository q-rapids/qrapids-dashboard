package com.upc.gessi.qrapids.app.domain.repositories.UserGroup;

import com.upc.gessi.qrapids.app.domain.models.UserGroup;

import java.io.Serializable;

public interface CustomUserGroupRepository extends Serializable {
    UserGroup findName( String name );
    UserGroup findDefaultUserGroup();
    boolean hasDefaultGroup();
    boolean updateUserGroupDefault( long id );
}
