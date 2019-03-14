package com.upc.gessi.qrapids.app.presentation.controller;

import com.upc.gessi.qrapids.app.config.Libs.AuthTools;
import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Question.QuestionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.UserGroup.UserGroupRepository;
import com.upc.gessi.qrapids.app.domain.models.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.util.List;

import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.COOKIE_STRING;

/**
 * User controller : AppUser entity relation
 * @author Elihu A. Cruz
 * @version 0.1.0
 */

@Controller
@RequestMapping("/users")
public class AppUserController {

    @Autowired
	private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private QuestionRepository questionRepository;

	private BCryptPasswordEncoder bCryptPasswordEncoder;

    // redirection url, after process finish
	private String redirectTo = "/users";

    /**
     * Implementation
     * @param userRepository
     * @param bCryptPasswordEncoder
     */
    public AppUserController(UserRepository userRepository,
                             BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * Read all clients by pages
     * @return
     */
    @GetMapping
	private ModelAndView index(@CookieValue(COOKIE_STRING) String token, Pageable page) {

        // Tools users validation
        // Current user -> session
        AppUser currenUser = this.userRepository.findByUsername(
                AuthTools.getUser( token )
        );

        List<UserGroup> userGroups = this.userGroupRepository.findAll();

        List<AppUser> users = this.userRepository.findAll( page ).getContent();

        UserGroup defautlUserGroup = this.userGroupRepository.findDefaultUserGroup();

        if (defautlUserGroup == null)
            defautlUserGroup = new UserGroup();

        // View creation
        ModelAndView view = new ModelAndView("/AppUser/index");

        view.addObject("appuser", currenUser );
        view.addObject("users", users );
        view.addObject("userGroups", userGroups);
        view.addObject("defautlUserGroup", defautlUserGroup );
        view.addObject("user", new AppUser());

        boolean admin_select = ! this.userGroupRepository.hasDefaultGroup();
        view.addObject("admin_select", admin_select);


        return view;

    }

    /**
     * Create element
     * @param user
     * @return
     */
    @PostMapping
    public String createEntity(@ModelAttribute(value = "appuser") @Valid AppUser user ) {

        System.out.println(user.toString());
        // Number of groups
        //long groups = this.userGroupRepository.count();
        //if ( groups >= 1 && user.getUserGroup() != null ){
        //    user.setAdmin( false );
        //} else {
        //    user.setAdmin( true );
        //}
        user.setAdmin(true);

        try{

            user.setPassword(bCryptPasswordEncoder.encode(user.getEmail()));
            this.userRepository.save( user );

        } catch( Exception e ){
            return("redirect:" + this.redirectTo + "?error=" +
                    "User was registered, check the current users available");
        }

        return "redirect:" + this.redirectTo + "?success=User+created";
    }

    /**
     * Return users view update
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/update", method = RequestMethod.GET)
    public ModelAndView showUpdateView(@PathVariable("id") Long id) {

        ModelAndView view = new ModelAndView("/AppUser/update");

        AppUser user = this.userRepository.findOne( id );
        List<UserGroup> userGroups = this.userGroupRepository.findAll();

        view.addObject("userGroups", userGroups);
        view.addObject( "questions", this.questionRepository.findAll());
        view.addObject("defautlUserGroup", this.userGroupRepository.findDefaultUserGroup() );
        view.addObject("appuser", user);


        return view;
    }

    /**
     * Update persistent unit
     * @param user
     * @return
     */
    @PostMapping("/update")
    public String updateEntity(@ModelAttribute(value = "appuser") @Valid AppUser user ) {

        try{
            AppUser update = this.userRepository.findOne( user.getId() );

            if (! "".equals(user.getEmail())  )
                update.setEmail( user.getEmail() );

            if (! (user.getAppuser_question() == null))
                update.setAppuser_question( user.getAppuser_question() );

            if (! (user.getUserGroup() == null))
                update.setUserGroup( user.getUserGroup() );

            if (! "".equals( user.getQuestion() ))
                update.setQuestion( user.getQuestion() );


            if (! ( user.getPassword() == null || "".equals( user.getPassword()) ))
                update.setPassword( bCryptPasswordEncoder.encode( user.getPassword() ) );

            System.out.println(update.toString());
            this.userRepository.save( update );

            return "redirect:" + this.redirectTo + "?success=Success";

        } catch( Exception e ){
            return "redirect:" + this.redirectTo + "?error=Something+went+wrong";
        }

    }

    /**
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}/delete")
    public ModelAndView showDeleteView(@PathVariable("id") Long id) {

        ModelAndView view = new ModelAndView("/AppUser/delete");

        try {

            AppUser user = this.userRepository.findOne( id );
            view.addObject("id", user.getId() );
            view.addObject("name", user.getUsername() );
            view.addObject("appuser", user );

        } catch ( Exception err ) {
            view.addObject("errors", "Error" );
        }

        return view;

    }


    @PostMapping("/delete")
    public String deleteEntity(@CookieValue(COOKIE_STRING) String token, @RequestParam("id") Long id, @RequestParam("name") String name ) {

        AppUser user = this.userRepository.findOne( id );

        // Is current user
        AuthTools authTools = new AuthTools();
        String username = authTools.getUserToken( token );

        if ( username.equals(user.getUsername()))
            return "redirect:" + this.redirectTo + "?error=" + "You can not delete the current user administrator".replace(" ","+");

        // Last admin user
        if (this.userRepository.count() <= 1)
            return "redirect:" + this.redirectTo + "?error=" + "The application needs one administrator user".replace(" ","+");


        String name_string = user.getUsername();

        if (! name.equals( name_string )){

            return "redirect:" + this.redirectTo + "?error=" + "Something went wrong".replace(" ","+");

        } else {

            try{

                this.userRepository.delete( user );

                return "redirect:" + this.redirectTo + "?success=" + "User deleted".replace(" ","+");

            } catch( Exception e ){

                return "redirect:" + this.redirectTo + "?error=" + "User can not be deleted".replace(" ","+");

            }

        }

    }

    /**
     * Update persistent unit
     * @param user
     * @return
     */
    @RequestMapping(value = "/resetpassword", method = RequestMethod.POST)
    public String resetPassword(@ModelAttribute(value = "appuser") @Valid AppUser user ) {

        try{
            AppUser update = this.userRepository.findOne( user.getId() );

            update.setPassword( bCryptPasswordEncoder.encode( update.getEmail() ) );

            this.userRepository.save( update );

            return "redirect:" + this.redirectTo + "?success=" + "Success".replace(" ","+");

        } catch( Exception e ){
            return "redirect:" + this.redirectTo + "?error=" + "Error".replace(" ","+");
        }

    }

    /**
     * Give admin access
     * @param user
     * @return
     */
    @RequestMapping(value = "/updateadmin", method = RequestMethod.POST)
    public String updateAdminAccess(@ModelAttribute(value = "appuser") @Valid AppUser user ) {

        try{
            AppUser update = this.userRepository.findOne( user.getId() );

            update.setAdmin(true);
            update.setUserGroup(null);

            this.userRepository.save( update );

            return "redirect:" + this.redirectTo + "?success=" + "User updated".replace(" ","+");

        } catch( Exception e ){
            return "redirect:" + this.redirectTo + "?error=" + "Error".replace(" ","+");
        }

    }

    /**
     * Give admin access
     * @param user
     * @return
     */
    @RequestMapping(value = "/setusergroup", method = RequestMethod.POST)
    public String setUpUserGroup(@ModelAttribute(value = "appuser") @Valid AppUser user ) {

        try{
            AppUser update = this.userRepository.findOne( user.getId() );

            update.setAdmin(false);

            if (user.getUserGroup() == null)
                update.setUserGroup(this.userGroupRepository.findDefaultUserGroup());

            update.setUserGroup(user.getUserGroup());

            this.userRepository.save( update );

            return "redirect:" + this.redirectTo + "?success=User+was+updated";

        } catch( Exception e ){
            return "redirect:" + this.redirectTo + "?error=" + e.toString();
        }

    }

    /**
     * Select form object binding
     * @param request
     * @param binder
     * @throws Exception
     */
    @InitBinder
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws     Exception {
        binder.registerCustomEditor(UserGroup.class, "area", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                UserGroup route = userGroupRepository.findOne( Long.parseLong( text ) );
                setValue( route );
            }
        });
    }

}
