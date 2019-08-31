package com.upc.gessi.qrapids.app.presentation.web.controller.Auth;

import com.upc.gessi.qrapids.app.config.libs.AuthTools;
import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.models.Question;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Question.QuestionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.UserGroup.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.util.Optional;

import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.COOKIE_STRING;

/**
 * Authentication Controller
 * @author Elihu A. Cruz
 * @version 0.1.1
 */

@Controller
@RequestMapping("/")
public class AuthController {

    private static final String QUESTIONS = "questions";
    private static final String APPUSER = "appuser";

    @Autowired
	UserRepository userRepository;

	@Autowired
	UserGroupRepository userGroupRepository;

	@Autowired
    QuestionRepository questionRepository;

    private AuthTools authTools;
    private long FirstUserRequired = 0;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthController(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    /**
     * User login
     * @param request
     * @return
     */
	@GetMapping("/login")
	public ModelAndView login (HttpServletRequest request) {

        // First charge user register view
        long users =  this.userRepository.count();

        if( users == this.FirstUserRequired ) {

            ModelAndView view =  new ModelAndView("Auth/FirstLoad");

            view.addObject(QUESTIONS, this.questionRepository.findAll());
            view.addObject(APPUSER, new AppUser());
            view.addObject("all",true);

            return view;

        }

        // Auth client
        this.authTools = new AuthTools();
        String cookie_token = this.authTools.getCookieToken( request, COOKIE_STRING );
        // Tools users validation
        // Current user -> session
        AppUser currenUser = this.userRepository.findByUsername(
                AuthTools.getUser( cookie_token )
        );

        // AppUser login request register
	    request.getSession().setAttribute("lastPage","login");

		ModelAndView view = new ModelAndView("redirect:/StrategicIndicators/CurrentChart");
        view.addObject(APPUSER, currenUser );

        if( "".equals( cookie_token ) || cookie_token == null )
            return new ModelAndView("Auth/Login");

        return view;

	}

    /**
     * Close user session, we delete cookie client
     * @param request
     * @param response
     * @return
     */
	@GetMapping("/logout_user")
	public String logout(HttpServletRequest request, HttpServletResponse response) {

		Cookie cookie = new Cookie(COOKIE_STRING, null); // Not necessary, but saves bandwidth.
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0); // Don't set to -1 or it will become a session cookie!
		response.addCookie(cookie);

		return "redirect:/login";
	}

    /**
     * Display sign up view
     * @return
     */
	@GetMapping("/signup")
    public ModelAndView showRegisterView() {

	    // Count users
        long users =  this.userRepository.count();

        // First load validation
        if( users == this.FirstUserRequired )
            return new ModelAndView("redirect:/");


	    ModelAndView view =  new ModelAndView("Auth/Signup");
        view.addObject(QUESTIONS, this.questionRepository.findAll());
        view.addObject(APPUSER, new AppUser());

        return view;

    }

    /**
     * Reset vie
     * @return
     */
    @GetMapping("/reset-password")
    public String resetPasswordView(){
	    return "Auth/ResquestEmailReset";
    }

    @PostMapping("/reset-password")
    public ModelAndView resetPasswordValidation( @RequestParam("email") String email){
        AppUser current = this.userRepository.findByEmail(email);
        if (current == null) {
            return new ModelAndView("redirect:/login?error=Email+does+not+exist");
        } else{
            ModelAndView view = new ModelAndView("Auth/Reset");
            view.addObject(APPUSER, current);
            view.addObject(QUESTIONS, this.questionRepository.findAll());
            return view;
        }
    }

    @PostMapping("/reset-trigger")
    public String resetTraigger(@ModelAttribute(value = APPUSER) @Valid AppUser user ){
        Optional<AppUser> userOptional = this.userRepository.findById(user.getId());
        if (userOptional.isPresent()) {
            AppUser userUpdate = userOptional.get();

            if (userUpdate.getAppuser_question().getId().equals( user.getAppuser_question().getId())){
                if (bCryptPasswordEncoder.matches( user.getQuestion(), userUpdate.getQuestion() )){
                    userUpdate.setPassword( bCryptPasswordEncoder.encode(userUpdate.getEmail()));
                    this.userRepository.save(userUpdate);
                    return "redirect:/login?success=Password+restarted";
                }
                return "redirect:/login?error=Security+question+is+incorrect";
            }
            return "redirect:/login?error=Security+question+is+not+the+same+as+the+original";
        } else {
            return "redirect:/login?error=User+not+found";
        }
    }


    /**
     * Create User
     * @param user
     * @return
     */
    @PostMapping("/signup")
    public String signupUser(@ModelAttribute(value = APPUSER) @Valid AppUser user ) {

        // First charge user register view
        Long users =  this.userRepository.count();

        // Number of groups
        long groups = this.userGroupRepository.count();

        // App has default group
        boolean hasDefaultGroup = this.userGroupRepository.existsByDefaultGroupIsTrue();


        // User has one group with default property
        if( groups >= 1 && hasDefaultGroup ){
            // Join user to group to the default group, if the aplication doesn't haver default group, is admin.
            user.setAdmin( false );
            user.setUserGroup( this.userGroupRepository.findByDefaultGroupIsTrue() );
        } else {
            user.setAdmin( true );
        }

        try{

            // Encrypted fields
            user.setQuestion( bCryptPasswordEncoder.encode( user.getQuestion() ) );
            user.setPassword( bCryptPasswordEncoder.encode(user.getPassword()) );

            this.userRepository.save( user );

        } catch( Exception e ){
            return "redirect:/login?error=signup error".replace(' ','+');
        }

        return "redirect:/login?success=Success";
    }

    /**
     * Select binding (object with dropdown elements)
     * @param request
     * @param binder
     * @throws Exception
     */
    @InitBinder
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws     Exception {

        binder.registerCustomEditor( Question.class, "question", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                Optional<Question> questionOptional = questionRepository.findById(Long.parseLong(text));
                if (questionOptional.isPresent()) {
                    Question question = questionOptional.get();
                    setValue(question);
                }
            }
        });

    }

}
