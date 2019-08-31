package com.upc.gessi.qrapids.app.config.security;

public class SecurityConstants {

    // change value configuration
	public static final String SECRET = "SecretKeyToGenJWTs"; // Seed
	public static final long EXPIRATION_TIME = 864_000_000; // 10 days


	public static final String TOKEN_PREFIX = "Bearer "; // API header validation
	public static final String HEADER_STRING = "Authorization"; // Request header
	public static final String COOKIE_STRING = "xFOEto4jYAjdMeR3Pas6_"; // hashed name cookie

    /**
     * View Cosntants Handlers to display Login Window
     */
    public static final String WELCOME_VIEW_URL = "/";
    public static final String LOGIN_VIEW_URL   = "/login";

	/** Public URLs. */
	public static final String[] PUBLIC_MATCHERS = {

			// public resources

			"/icons/**",
			"/css/**",
			"/js/**",
            "/fonts/**",
			"/images/**",
			"/bootstrap.css",
			"/bootstrap.min.css",
			"/favicon.ico",
			"/styles.css",

			// Public routes

            "/signup",
            "/reset-password",
            "/reset-trigger",
            "/error",
            "/success",
            "/"

	};

    public static final String[] GLOBAL_MATCHERS = {
            LOGIN_VIEW_URL,
            "/home",
            "/setupUser",
            "/reset-password",
            "/logout_user",
            "/QRapids-0.0.1/CurrentEvaluation" // API CALL
    };

}
