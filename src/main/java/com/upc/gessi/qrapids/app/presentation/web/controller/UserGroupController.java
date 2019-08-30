package com.upc.gessi.qrapids.app.presentation.web.controller;

import com.upc.gessi.qrapids.app.config.Libs.AuthTools;
import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Route.RouteRepository;
import com.upc.gessi.qrapids.app.domain.repositories.UserGroup.UserGroupRepository;
import com.upc.gessi.qrapids.app.domain.models.Route;
import com.upc.gessi.qrapids.app.domain.models.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.COOKIE_STRING;

/**
 * User group controller
 * @author Elihu A. Cruz
 * @version 0.1.0
 */

@Controller
@RequestMapping("/usergroups")
public class UserGroupController {

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private UserRepository userRepository;

    private Logger logger = LoggerFactory.getLogger(UserGroupController.class);

    private final String redirectTo = "/usergroups";

    /**
     * Read all elements
     * @param page
     * @return
     */
    @GetMapping
    public ModelAndView index(@CookieValue(COOKIE_STRING) String token, Pageable page ){

        List<UserGroup> userGroups = this.userGroupRepository.findAll( page ).getContent();
        Iterable<Route> routesIterable = this.routeRepository.findAll();
        List<Route> routes = new ArrayList<>();
        routesIterable.forEach(routes::add);

        // Tools users validation
        // Current user -> session
        AppUser currenUser = this.userRepository.findByUsername(
                AuthTools.getUser( token )
        );

        ModelAndView view = new ModelAndView("/UserGroup/index");

        view.addObject( "userGroup", new UserGroup());
        view.addObject("userGroups", userGroups);
        view.addObject("routes", routes );
        view.addObject("appuser", currenUser );

        return view;
    }

    /**
     * Create element
     * @param userGroup
     * @return
     */
    @PostMapping
    public String createEntity(@ModelAttribute(value = "userGroup") @Valid UserGroup userGroup) {

        try{
            // Save form
            this.userGroupRepository.save(userGroup);
            return "redirect:" + this.redirectTo + "?success=" + "User group created".replace(" ","+");

        } catch( Exception e ){
            return "redirect:" + this.redirectTo + "?error=" + "User group no created".replace(" ","+");
        }
    }

    /**
     * Return users view update
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/update", method = RequestMethod.GET)
    public ModelAndView showUpdateView(@CookieValue(COOKIE_STRING) String token, @PathVariable("id") Long id) {

        ModelAndView view = new ModelAndView("/UserGroup/Update");

        // Current user -> session
        AppUser currenUser = this.userRepository.findByUsername(
                AuthTools.getUser( token )
        );

        UserGroup userGroup = this.userGroupRepository.getOne( id );
        Iterable<Route> routesIterable = this.routeRepository.findAll();
        List<Route> routes = new ArrayList<>();
        routesIterable.forEach(routes::add);

        view.addObject("userGroup", userGroup);
        view.addObject("routes", routes );
        view.addObject("appuser", currenUser );

        return view;
    }

    /**
     * Update persistent unit
     * @param userGroup
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public String updateEntity(@ModelAttribute(value = "userGroup") @Valid UserGroup userGroup) {

        try{
            this.userGroupRepository.save(userGroup);

            return "redirect:" + this.redirectTo + "?success=" + "Group updated".replace(" ","+");

        } catch( Exception e ){

            return "redirect:" + this.redirectTo + "?error=" + "Group can not be updated".replace(" ","+");

        }

    }

    @RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
    public ModelAndView showDeleteView(@CookieValue(COOKIE_STRING) String token, @PathVariable("id") Long id) {

        ModelAndView view = new ModelAndView("/UserGroup/Delete");

        // Current user -> session
        AppUser currenUser = this.userRepository.findByUsername(
                AuthTools.getUser( token )
        );

        try {
            UserGroup user = this.userGroupRepository.getOne( id );
            view.addObject("id", user.getId() );
            view.addObject("name", user.getName() );
            view.addObject("appuser", currenUser );

        } catch ( Exception err ) {
            view.addObject("errors", "Error" );
        }

        return view;

    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String deleteEntity( @RequestParam("id") Long id, @RequestParam("name") String name ) {

        UserGroup userGroup = this.userGroupRepository.getOne( id );

        String name_string = userGroup.getName();

        if (! name.equals( name_string )){

            return "redirect:" + this.redirectTo + "?error=" + "Confirmation error".replace(" ","+");

        } else {

            try{

                this.userGroupRepository.delete(userGroup);

                return "redirect:" + this.redirectTo + "?success=" + "Group deleted".replace(" ","+");

            } catch( Exception e ){
                logger.error(e.getMessage(), e);

                return "redirect:" + this.redirectTo + "?error=" + "Something went wrong".replace(" ","+");

            }

        }

    }

    @PostMapping("/updateDefaultGroup")
    public String updateDefaultGroup( HttpServletRequest request, @RequestParam(value="id", required=true) long id){

        try {
            this.userGroupRepository.updateUserGroupDefault( id );
            return "redirect:" + this.redirectTo + "?success=" + "Updated".replace(" ","+");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "redirect:" + this.redirectTo + "?error=" + "Something went wrong".replace(" ","+");
        }

    }

    @InitBinder
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws     Exception {

        binder.registerCustomEditor(Route.class, "routes", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                Optional<Route> routeOptional = routeRepository.findById(Long.parseLong(text));
                if (routeOptional.isPresent()) {
                    Route route = routeOptional.get();
                    setValue(route);
                }
            }
        });

    }

}
