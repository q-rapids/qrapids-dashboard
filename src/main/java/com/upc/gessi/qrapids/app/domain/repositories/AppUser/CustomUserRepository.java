package com.upc.gessi.qrapids.app.domain.repositories.AppUser;

import com.upc.gessi.qrapids.app.domain.models.AppUser;

import java.io.Serializable;

public interface CustomUserRepository extends Serializable {
    AppUser findUserByEmail( String email );
}
