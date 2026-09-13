package com.rentdb.global.security;

import java.time.Clock;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.rentdb.global.ratelimit.SlidingWindowRateLimiter;
import com.rentdb.global.web.ClientInfoResolver;
import com.rentdb.lead.LeadProperties;

import tools.jackson.databind.json.JsonMapper;

/**
 * 보안 필터 체인 구성 (요청 경로별로 체인을 나눈다)
 *
 * <pre>
 * ① 관리자 체인  /api/admin/**   세션 로그인(JSON 로그인 필터) · CSRF · ROLE_ADMIN 필요 · 로그인 시도 제한
 * ② 공개 체인    /api/** 등      허용 목록에 있는 API만 익명 허용, 나머지 차단 · 세션 미사용 · 상담 신청 요청 제한
 * ③ 기본 체인    그 밖의 모든 경로  전부 차단
 * </pre>
 *
 * 인증 실패는 401, 권한·CSRF 실패는 403 을 JSON(ErrorResponse)으로 응답한다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String LOGOUT_URL = "/api/admin/auth/logout";

	// ---------------------------------------------------------------- 공통 구성 요소

	@Bean
	public PasswordEncoder passwordEncoder() {
		// {bcrypt} 접두어가 붙은 BCrypt 해시 — 추후 알고리즘을 바꿔도 기존 해시와 호환된다
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AdminUserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	public SecurityContextRepository securityContextRepository() {
		// 인증 정보는 세션에 저장하고, 세션은 Spring Session JDBC 가 DB에 보관한다
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	public CsrfTokenRepository csrfTokenRepository() {
		// 토큰을 세션에 저장한다. 프론트(example.com)와 API(api.example.com) 도메인이 달라
		// 쿠키로 토큰을 읽을 수 없으므로 로그인·/me 응답 본문으로 전달하고 X-CSRF-TOKEN 헤더로 돌려받는다.
		return new HttpSessionCsrfTokenRepository();
	}

	@Bean
	public RateLimitFilter rateLimitFilter(Clock clock, LeadProperties leadProperties,
			AppSecurityProperties securityProperties, ClientInfoResolver clientInfoResolver,
			SecurityErrorWriter errorWriter) {
		PathPatternRequestMatcher.Builder path = PathPatternRequestMatcher.withDefaults();
		return new RateLimitFilter(List.of(
				new RateLimitFilter.Rule("lead-create",
						path.matcher(HttpMethod.POST, "/api/leads"),
						new SlidingWindowRateLimiter(clock, leadProperties.rateLimit().maxRequests(),
								leadProperties.rateLimit().windowMinutes())),
				new RateLimitFilter.Rule("admin-login",
						path.matcher(HttpMethod.POST, JsonLoginFilter.LOGIN_URL),
						new SlidingWindowRateLimiter(clock, securityProperties.loginRateLimit().maxRequests(),
								securityProperties.loginRateLimit().windowMinutes()))),
				clientInfoResolver, errorWriter);
	}

	/** SecurityConfig 에서만 쓰는 필터가 서블릿 필터로 한 번 더 등록되지 않게 막는다 */
	@Bean
	public org.springframework.boot.web.servlet.FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
			RateLimitFilter filter) {
		var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(AppSecurityProperties securityProperties) {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		List<String> origins = securityProperties.corsAllowedOriginList();
		if (origins.isEmpty()) {
			// 로컬: Vite 프록시로 같은 출처가 되므로 CORS 허용 주소가 필요 없다
			return source;
		}
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(origins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Content-Type", "Accept", "X-CSRF-TOKEN"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}

	// ---------------------------------------------------------------- ① 관리자 체인

	@Bean
	@Order(1)
	public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http,
			AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository,
			CsrfTokenRepository csrfTokenRepository,
			RateLimitFilter rateLimitFilter,
			AdminLoginHandlers loginHandlers,
			RestAuthenticationEntryPoint authenticationEntryPoint,
			RestAccessDeniedHandler accessDeniedHandler,
			SecurityErrorWriter errorWriter,
			JsonMapper jsonMapper) throws Exception {

		PathPatternRequestMatcher.Builder path = PathPatternRequestMatcher.withDefaults();
		CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();

		// 로그인 성공 시: 세션 ID 교체(세션 고정 공격 방지) + CSRF 토큰 재발급
		CsrfAuthenticationStrategy csrfAuthenticationStrategy = new CsrfAuthenticationStrategy(csrfTokenRepository);
		csrfAuthenticationStrategy.setRequestHandler(csrfRequestHandler);

		JsonLoginFilter loginFilter = new JsonLoginFilter(authenticationManager, jsonMapper);
		loginFilter.setSecurityContextRepository(securityContextRepository);
		loginFilter.setSessionAuthenticationStrategy(new CompositeSessionAuthenticationStrategy(List.of(
				new ChangeSessionIdAuthenticationStrategy(), csrfAuthenticationStrategy)));
		loginFilter.setAuthenticationSuccessHandler(loginHandlers);
		loginFilter.setAuthenticationFailureHandler(loginHandlers);

		http
				.securityMatcher("/api/admin/**")
				.authorizeHttpRequests(auth -> auth
						.anyRequest().hasRole(AdminPrincipal.ROLE_ADMIN))
				.csrf(csrf -> csrf
						.csrfTokenRepository(csrfTokenRepository)
						.csrfTokenRequestHandler(csrfRequestHandler)
						// 로그인 요청은 아직 토큰이 없으므로 제외 (로그인 후 토큰 재발급으로 보호)
						.ignoringRequestMatchers(path.matcher(HttpMethod.POST, JsonLoginFilter.LOGIN_URL)))
				.securityContext(context -> context.securityContextRepository(securityContextRepository))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.requestCache(AbstractHttpConfigurer::disable)
				.logout(logout -> logout
						.logoutRequestMatcher(path.matcher(HttpMethod.POST, LOGOUT_URL))
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies(SessionCookieConfig.SESSION_COOKIE_NAME)
						.logoutSuccessHandler((request, response, authentication) ->
								errorWriter.writeJson(response, 200, java.util.Map.of("result", "ok"))))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.cors(Customizer.withDefaults())
				.headers(headers -> headers
						.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
						.addHeaderWriter(new StaticHeadersWriter("X-Robots-Tag", "noindex, nofollow")))
				.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	// ---------------------------------------------------------------- ② 공개 체인

	@Bean
	@Order(2)
	public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http,
			RateLimitFilter rateLimitFilter,
			RestAuthenticationEntryPoint authenticationEntryPoint,
			RestAccessDeniedHandler accessDeniedHandler) throws Exception {

		http
				.securityMatcher("/api/**", "/actuator/**", "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/error")
				.authorizeHttpRequests(auth -> auth
						// 사용자 화면용 공개 API — 새 공개 API는 여기에 명시적으로 추가해야 열린다
						.requestMatchers(HttpMethod.GET,
								"/api/ping", "/api/site", "/api/brands", "/api/brands/*/models",
								"/api/models/*", "/api/deals", "/api/instant", "/api/banners",
								"/api/files/*/*/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/leads").permitAll()
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
						// API 문서: 운영(prod)에서는 springdoc 자체가 꺼져 있어 404
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
						.requestMatchers("/error").permitAll()
						.anyRequest().denyAll())
				// 공개 API는 쿠키 인증을 쓰지 않으므로 CSRF 공격 대상이 아니다 (스팸은 요청 제한·중복 차단으로 막음)
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.requestCache(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.cors(Customizer.withDefaults())
				.addFilterBefore(rateLimitFilter, AnonymousAuthenticationFilter.class);

		return http.build();
	}

	// ---------------------------------------------------------------- ③ 기본 체인

	@Bean
	@Order(3)
	public SecurityFilterChain denyAllSecurityFilterChain(HttpSecurity http,
			RestAuthenticationEntryPoint authenticationEntryPoint,
			RestAccessDeniedHandler accessDeniedHandler) throws Exception {

		http
				.authorizeHttpRequests(auth -> auth.anyRequest().denyAll())
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler));

		return http.build();
	}

}
