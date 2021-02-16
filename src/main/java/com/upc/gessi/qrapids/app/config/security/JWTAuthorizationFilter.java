package com.upc.gessi.qrapids.app.config.security;

import com.upc.gessi.qrapids.app.config.libs.AuthTools;
import com.upc.gessi.qrapids.app.config.libs.RouteFilter;
import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.models.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.*;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	private AuthTools authTools;
	private RouteFilter routeFilter;

    private UserRepository userRepository;

	private boolean DEBUG = false;

	private Logger logger = LoggerFactory.getLogger(JWTAuthorizationFilter.class);

	public JWTAuthorizationFilter(AuthenticationManager authManager) {
		super(authManager);
	}

    public JWTAuthorizationFilter(AuthenticationManager authManager, UserRepository userRepository ) {
        super(authManager);
        this.userRepository = userRepository;
    }

	/**
	 * Extraemos datos de la petición, Cabecera de autenticación
	 * @param req
	 * @param res
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest req,
									HttpServletResponse res,
									FilterChain chain) throws IOException, ServletException {

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        this.authTools = new AuthTools();
        this.routeFilter = new RouteFilter();
        // is an External request? true -> WebPage : false -> external
        //boolean origin = this.authTools.originRequest( req );

        // Authorization object
		UsernamePasswordAuthenticationToken authentication;

		// Get header & cookie auth string variables
        String header = req.getHeader(HEADER_STRING);
        String cookie_token = this.authTools.getCookieToken( req, COOKIE_STRING );
        String token = "";

        if ( cookie_token != null ) {
            // WeaApp Client internal application

            authentication = this.authTools.tokenValidation( cookie_token );
            token = cookie_token;

            logMessage(" Origin - WebApp ");

        } else {

            // External application API Access
            if( header == null || !header.startsWith(TOKEN_PREFIX) ){

                logMessage(" No token API ");

                chain.doFilter(req, res);

                return;
            }

            authentication = getAuthentication( req );
            token = req.getHeader( HEADER_STRING );

            logMessage(" Origin - ApiCall ");

        }

        /** --[ Filter implementation ]-- */

        // Flag valitation
        boolean isAllowed = false;

        // Global route
        String origin_request = req.getRequestURI();

        // User container
        AppUser user = null;

        // List of routes container
        List<Route> routes = new ArrayList<>();

        // Public resources
        isAllowed = this.routeFilter.publicURLAttemp( origin_request );

        if (! isAllowed )
            isAllowed = this.routeFilter.globalURLAttemp( origin_request );

        // We verify if route is a public resource
        if( ! isAllowed ) {

            // User data from DB
            user = this.userRepository.findByUsername( this.authTools.getUserToken( token ) );


            if ( user.getAdmin() )
                isAllowed = true;


            // Test elements and try to verify if the current route is allowed for the current user
            else{
                // Cast set object ot List of AppUSers
                routes.addAll( user.getUserGroup().getRoutes() );
                isAllowed = this.routeFilter.filterShiled( origin_request, token, routes );
            }

        }

        /** [ Route Filtering ] */

        // Verfiy an redirect if user does not have permission to use the current route.
        if ( ! isAllowed ){

            res.sendRedirect( "/login?error=User+does+not+have+permission" );

        } else {

            logMessage(origin_request + " <- -> [Final status] : " + isAllowed);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(req, res);

        }

	}

	private void logMessage (String message) {
        if (this.DEBUG)
            logger.info(message);
    }

	/**
	 * Obtención de token de la cabecera previamente obtenida en doFilterInternal
	 * Obteción de datos de usuario serializados
	 * @param request
	 * @return
	 */
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {

		String token = request.getHeader(HEADER_STRING);

		return this.authTools.tokenValidation( token );

	}

}
