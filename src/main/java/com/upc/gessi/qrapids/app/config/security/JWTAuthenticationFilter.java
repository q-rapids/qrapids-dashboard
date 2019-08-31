package com.upc.gessi.qrapids.app.config.security;

import com.upc.gessi.qrapids.app.config.libs.AuthTools;
import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.EXPIRATION_TIME;
import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.HEADER_STRING;
import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.SECRET;
import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.TOKEN_PREFIX;
import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.COOKIE_STRING;


public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;
	// √Tools Auth
	AuthTools authTools;

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	/**
	 * Login Request attempt
	 * @param req
	 * @param res
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest req,
												HttpServletResponse res) throws AuthenticationException {

		// AppUser credentials
		AppUser creds;

		try { // JSON Request
			creds = new ObjectMapper()
					.readValue(req.getInputStream(), AppUser.class);

		} catch (IOException e) {

			// Form validation
			String username = req.getParameter("username");
			String password = req.getParameter("password");

			if( "".equals(username) || username.isEmpty() || "".equals(password) || password.isEmpty() ) {
				throw new RuntimeException(e);
			} else {
				// Form has data of autentication
				creds = new AppUser();
				creds.setUsername( username );
				creds.setPassword( password );
			}
		}

		return authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						creds.getUsername(),
						creds.getPassword(),
						new ArrayList<>())
		);
	}

	/**
	 * Auth Return parameters
	 * @param req
	 * @param res
	 * @param chain
	 * @param auth
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest req,
											HttpServletResponse res,
											FilterChain chain,
											Authentication auth) throws IOException, ServletException {

		// Auth tools
		this.authTools = new AuthTools();

		// Token creation
		String token = Jwts.builder()
				.setSubject(((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
				.compact();

		// Request origin
		boolean origin = this.authTools.originRequest( req );

		if( origin ){

			// Web Application
            // Set token auth in HTTP Only cookie client.
			Cookie qrapids_token_client = new Cookie( COOKIE_STRING, token);

			// Configuration
			qrapids_token_client.setHttpOnly( true );
			qrapids_token_client.setMaxAge(  (int) EXPIRATION_TIME / 1000 );

            res.addCookie( qrapids_token_client );

			res.sendRedirect("StrategicIndicators/CurrentChart");

		} else {
			// API send header with token auth
			res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
		}
	}


}
