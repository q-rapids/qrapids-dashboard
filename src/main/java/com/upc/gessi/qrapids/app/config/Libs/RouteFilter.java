package com.upc.gessi.qrapids.app.config.Libs;

import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.models.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.regex.Pattern;

import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.GLOBAL_MATCHERS;
import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.PUBLIC_MATCHERS;

public class RouteFilter {

    @Autowired
    UserRepository userRepository;

    private boolean DEBUG = false;

    AuthTools authTools;

    public RouteFilter() {
        this.authTools = new AuthTools();
    }

    /**
     * Main function, filter function, cleaning and comparation with user's allowed routes
     * @param originRequest
     * @param token
     * @param routes
     * @return
     */
    public boolean filterShiled(String originRequest, String token, List<Route> routes) {

        // Validation flag
        boolean success = false;

        if( routes != null && !success ) {

            success = userURLAttemp( originRequest, routes );

        }

        if (this.DEBUG)
            System.out.println( originRequest + " <- -> [Final status] : " + success);

        return success;
    }

    public boolean publicURLAttemp( String url ) {
        boolean success = false;
        // Public resources
        for ( String match : PUBLIC_MATCHERS ) {

            if( success ) break;

            else
                success = attempRoute( url , match );
        }
        return success;
    }

    public boolean userURLAttemp( String url, List<Route> routes ) {

        boolean success = false;

        for ( Route route : routes ) {

            if( success ) break;

            else
                success = attempRoute( url, route.getPath() );
        }

        return success;
    }

    public  boolean globalURLAttemp( String url ){
        boolean success = false;

        for ( String route : GLOBAL_MATCHERS ) {

            if( success ) break;

            else
                success = attempRoute( url, route );
        }

        return success;
    }

    /**
     * Attemp URL user access
     * @param origin
     * @param path
     * @return
     */
    public boolean attempRoute(String origin, String path) {

        // Base url Request - tranformation to REGEX
        String pattern = urlToPattern( path );


        if (DEBUG)
            System.out.printf("Pattern: " + pattern);

        // Regext match
        return origin.matches( pattern );

    }

    /**
     * Filter URL user, URL conversion and globalization.
     * @param url
     * @return
     */
    public String urlToPattern( String url ){

        // We generalize url's client, after this pont the url accept everything.
        boolean globar_tool = ( url.contains("*") || url.contains("**") );

        // Requiremento to Pattern quote url, the function doesn't accept *
        // We remove the character
        url = url.replace("*","");
        char last_element = url.charAt( url.length() - 1 );

        // Catch last slash

        if ( last_element == ( (char) 47) && url.length() > 1)
            url = url.substring( 0, url.length() - 1 );


        // Create Regex to base url
        String pattern = Pattern.quote( url );

        if ( globar_tool )
            pattern = pattern + "|" + pattern + "(.{1,2083})" + "|" + pattern + "\\/";

        // System.out.println("pattern: " + pattern);
        // Return successfully pattern
        return pattern;

    }

}
