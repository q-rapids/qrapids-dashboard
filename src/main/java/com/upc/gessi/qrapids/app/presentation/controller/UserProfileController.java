package com.upc.gessi.qrapids.app.presentation.controller;

import com.upc.gessi.qrapids.app.config.Libs.AuthTools;
import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Question.QuestionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.UserGroup.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.COOKIE_STRING;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserGroupRepository userGroupRepository;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private final String redirectTo = "/profile";

    public UserProfileController(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * Read all clients by pages
     * @return
     */
    @GetMapping
    private ModelAndView index(@CookieValue(COOKIE_STRING) String token, Pageable page) {

        // Tools users validation
        AuthTools authTools = new AuthTools();
        String userName = authTools.getUserToken( token );

        ModelAndView view = new ModelAndView("/AppUser/Profile");

        try{

            view.addObject( "questions", this.questionRepository.findAll());
            view.addObject("defautlUserGroup", this.userGroupRepository.findDefaultUserGroup() );
            view.addObject("appuser", this.userRepository.findByUsername( userName ));


        }catch (Exception e){
            System.out.println(e);
        }

        return view;

    }

    /**
     * Return users view update
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/update", method = RequestMethod.GET)
    public ModelAndView showUpdateView(@PathVariable("id") Long id) {

        ModelAndView modelAndView = new ModelAndView("/AppUser/update");

        AppUser user = this.userRepository.getOne( id );

        modelAndView.addObject("appuser", user);

        return modelAndView;
    }

    /**
     * Update persistent unit
     * @param user
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public String updateEntity(@ModelAttribute(value = "appuser") @Valid AppUser user ) {

        System.out.println(user.toString());

        try{
            AppUser update = this.userRepository.getOne( user.getId() );

            update.setEmail( user.getEmail() );

            if(! ( user.getAppuser_question() == null ))
                update.setAppuser_question( user.getAppuser_question() );

            if ( !( user.getQuestion() == null ) )
                update.setQuestion( bCryptPasswordEncoder.encode( user.getQuestion() ) );

            if ( !( user.getPassword() == null ) )
                update.setPassword( bCryptPasswordEncoder.encode( user.getPassword() ) );

            this.userRepository.save( update );

            return "redirect:" + this.redirectTo + "?success=" + "Data updated".replace(" ","+");

        } catch( Exception e ){
            return "redirect:" + this.redirectTo + "?error=" + "Something went wrong".replace(" ","+");
        }

    }

}

