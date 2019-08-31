package com.upc.gessi.qrapids.app.config.libs;

import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.SECRET;
import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.TOKEN_PREFIX;


public class AuthTools {

	private final boolean DEBUG = false;

	private Logger logger = LoggerFactory.getLogger(AuthTools.class);

	/**
	 * Origin validation (External application or WebApplication )
	 * @param request
	 * @return
	 */
	public boolean originRequest(HttpServletRequest request) {

		// Header elements, validation and structure of request.
		String contentType = request.getHeader("Content-Type");

		// X Origin Session Webpage variable
		String lastPage = (String) request.getSession().getAttribute("lastPage");

		if( contentType != null && contentType.toUpperCase().equals("APPLICATION/JSON"))
			return false;

		if( this.DEBUG )
			logger.info("Origin:" + request.getRequestURI() + " Session_login_page : " + lastPage);

		return ( lastPage != null && "login".equals(lastPage) );
	}

    /**
     * Get cookie token from request
     * @param req
     * @return
     */
    public String getCookieToken( HttpServletRequest req, String name_token ) {
        String token = null;

        // Collect AppUser cookies
        Cookie[] cookies = req.getCookies();

        if ( cookies != null ) {

            // Cacth cookie auth
            for ( int i = 0; i < cookies.length; i++) {

                Cookie curr = cookies[ i ];
                if( curr.getName().equals( name_token ) ) {
                    token = cookies[ i ].getValue();
                }

            }

        } else {
            return null;
        }

        return token;

    }

	/**
	 * Token validation - Decrypt
	 * @param token
	 * @return
	 */
	public UsernamePasswordAuthenticationToken tokenValidation(String token) {

		if (token != null) {

			// parse the token.
			String user = Jwts.parser()
					.setSigningKey(SECRET.getBytes())
					.parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
					.getBody()
					.getSubject();

			if (user != null) {
				return new UsernamePasswordAuthenticationToken( user, null, new ArrayList<>());
			}
			return null;

		}
		return null;
	}

	/**
	 * Token validation - Decrypt
	 * @param token
	 * @return
	 */
	public static String getUserToken(String token) {

		if (token != null) {

			// parse the token.
			String user = Jwts.parser()
					.setSigningKey(SECRET.getBytes())
					.parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
					.getBody()
					.getSubject();

			return user;

		}
		return null;
	}

	// get user entinty from database
    public static String getUser( String token ) {

        // Obtención de nombre de usuario
        return getUserToken( token );

    }

}
