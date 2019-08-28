package com.upc.gessi.qrapids.app.presentation.web.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * Error handle - Main view response.
 * @author Elihu A. Cruz
 * @version 0.1.0
 */

@Controller
public class IndexController implements ErrorController {

    private static final String PATH = "/error";

    /**
     * Return a view showing an error with HTTP error code.
     * @param status
     * @param request
     * @return
     */
    @RequestMapping(value = PATH)
    public ModelAndView error(HttpStatus status, HttpServletRequest request) {

        // original requested URI
        String uri = String.valueOf(request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI));
        // status code
        String code = String.valueOf(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
        // status message
        String msg = String.valueOf(request.getAttribute(RequestDispatcher.ERROR_MESSAGE));

        ModelAndView view = new ModelAndView("Error/index");

        if( "401".equals( code ) )
            view = new ModelAndView("redirect:/login?error=401+Authentication+Failed:+Bad+credentials");

        view.addObject("error",getErrorPath());
        view.addObject("uri", uri);
        view.addObject("code", code );
        view.addObject("msg", msg);

        return view;
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}