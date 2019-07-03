package com.upc.gessi.qrapids.app.presentation.controller;

import com.upc.gessi.qrapids.app.config.Libs.AuthTools;
import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Question.QuestionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.UserGroup.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.validation.Valid;
import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.COOKIE_STRING;

/**
 * Home controller
 * @author Elihu A. Cruz
 * @version 0.1.0
 */

@Controller
@RequestMapping("/")
public class HomeController {

    @Value("${security.enable}")
    private boolean securityEnable;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserGroupRepository userGroupRepository;

    @Autowired
    QuestionRepository questionRepository;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * Implementation
     * @param bCryptPasswordEncoder
     */
    public HomeController(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
	 * Return Home view
	 * @return
	 */
	@GetMapping
	public String showWelcomeView () {
	    if (securityEnable)
            return "redirect:/login";
        else
	        return "redirect:/StrategicIndicators/CurrentChart";
	}

    /**
     * Display main view, controller used to test.
     * @param token
     * @return
     */
    @GetMapping("/home")
    public ModelAndView showHomeView (@CookieValue(COOKIE_STRING) String token) {


        // Current user -> session !important
        AppUser currenUser = this.userRepository.findByUsername(
                AuthTools.getUser( token )
        );

        if( bCryptPasswordEncoder.matches( currenUser.getEmail(), currenUser.getPassword() ) ) {

            // View loader
            ModelAndView view = new ModelAndView("Auth/SetupUser");

            view.addObject( "questions", this.questionRepository.findAll());
            view.addObject( "appuser", currenUser);
            view.addObject( "security_enable", false);

            return view;

        }

        // View loader
        // Redirect to route home
        ModelAndView view = new ModelAndView("Home/index");
        view.addObject( "appuser", currenUser);

        return view;
    }

    /**
     * Update user information in a first load user.
     * @param user
     * @return
     */
    @PostMapping("/setupUser")
    public String setupUser(@ModelAttribute(value = "appuser") @Valid AppUser user ) {

        try{
            // Fetch user from db
            AppUser update = this.userRepository.getOne( user.getId() );

            update.setEmail( user.getEmail() );
            update.setAppuser_question( user.getAppuser_question() );
            update.setQuestion( bCryptPasswordEncoder.encode( user.getQuestion() ) );

            // Password update
            if (! "".equals( user.getPassword() ) )
                update.setPassword( bCryptPasswordEncoder.encode( user.getPassword() ) );

            this.userRepository.save( update );
            return "redirect:/home?success=User+is+ready";

        } catch( Exception e ){
            return "redirect:/home?error=Something+went+worng";
        }

    }

}
