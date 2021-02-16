package com.upc.gessi.qrapids.app.config.security;

import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Bean;

import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.LOGIN_VIEW_URL;
import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.WELCOME_VIEW_URL;
import static com.upc.gessi.qrapids.app.config.security.SecurityConstants.PUBLIC_MATCHERS;

@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

	private UserDetailsService userDetailsService;
	private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    UserRepository userRepository;

	@Value("${security.enable}")
	private boolean securityEnable;

    @Value("${security.api.enable}")
    private boolean apiEnable;

	public WebSecurity(UserDetailsService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.userDetailsService = userDetailsService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	@Override
	protected void configure( HttpSecurity http ) throws Exception {

		String public_and_secure = ( this.securityEnable )? "/resources/**" : "/**";
        String public_api = ( this.apiEnable )?  "/api/**" : "/fonts/**";

		http.cors().and().csrf().disable().authorizeRequests()

				// View Filter's exception
				.antMatchers(HttpMethod.GET, WELCOME_VIEW_URL).permitAll()
				.antMatchers(HttpMethod.GET, LOGIN_VIEW_URL).permitAll()
				.antMatchers(PUBLIC_MATCHERS).permitAll()

                .antMatchers(public_api).permitAll()
				.antMatchers(public_and_secure).permitAll()

				.anyRequest().authenticated()
				.and()

				.addFilter(new JWTAuthenticationFilter(authenticationManager()))
				.addFilter(new JWTAuthorizationFilter(authenticationManager(), userRepository ))

				// this disables session creation on Spring Security
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
		return source;
	}
}
