package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsersController {

    @Autowired
    private UserRepository userRepository;

    public AppUser findUserByName (String userName) {
        return userRepository.findByUsername(userName);
    }
}
