package om.cgi.formation.jhipster.ecom.config;

import om.cgi.formation.jhipster.ecom.security.*;
import om.cgi.formation.jhipster.ecom.security.jwt.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;
import tech.jhipster.config.JHipsterProperties;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JHipsterProperties jHipsterProperties;

    private final TokenProvider tokenProvider;

    private final CorsFilter corsFilter;
    private final SecurityProblemSupport problemSupport;

    private static final String API_ALL = "/api/**";

    public SecurityConfiguration(
        TokenProvider tokenProvider,
        CorsFilter corsFilter,
        JHipsterProperties jHipsterProperties,
        SecurityProblemSupport problemSupport
    ) {
        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.problemSupport = problemSupport;
        this.jHipsterProperties = jHipsterProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {
        web
            .ignoring()
            .antMatchers(HttpMethod.OPTIONS, "/**")
            .antMatchers("/app/**/*.{js,html}")
            .antMatchers("/i18n/**")
            .antMatchers("/content/**")
            .antMatchers("/h2-console/**")
            .antMatchers("/swagger-ui/**")
            .antMatchers("/test/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf()
            .disable()
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
        .and()
            .headers()
            .contentSecurityPolicy(jHipsterProperties.getSecurity().getContentSecurityPolicy())
        .and()
            .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
        .and()
            .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; fullscreen 'self'; payment 'none'")
        .and()
            .frameOptions()
            .deny()
        .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers("/api/register").permitAll()
            .antMatchers("/api/activate").permitAll()
            .antMatchers("/api/account/reset-password/init").permitAll()
            .antMatchers("/api/account/reset-password/finish").permitAll()
            .antMatchers("/api/account/**").authenticated()
            .antMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/contact-details/**").authenticated()
            
            .antMatchers(HttpMethod.GET,"/api/products").permitAll()
            .antMatchers(HttpMethod.GET, "/api/products/**").permitAll()
            .antMatchers(HttpMethod.GET,"/api/stocks").permitAll()
            .antMatchers(HttpMethod.PATCH,"/api/stocks").permitAll()
            .antMatchers(HttpMethod.PATCH,"/api/stocks/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/stocks/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/myCart").permitAll()
            .antMatchers(HttpMethod.PATCH, "/api/addStocksInCart/{id}").permitAll()
            .antMatchers(HttpMethod.PATCH, "/api/finalbuy/{id}").authenticated()
            .antMatchers(HttpMethod.PATCH, "/api/deleteStocksInCart/{id}").authenticated()
            .antMatchers(HttpMethod.POST, "/api/orders").authenticated()
            .antMatchers(HttpMethod.PATCH, "/api/orders/{id}").authenticated()
            .antMatchers(HttpMethod.DELETE, "/api/orders/{id}").authenticated()
            .antMatchers("/api/order-lines").permitAll()
            .antMatchers(HttpMethod.PUT, "/api/order-lines").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers(HttpMethod.PUT, "/api/order-lines/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/order-lines/**").permitAll()
            .antMatchers("/api/addresses").authenticated()
            .antMatchers(HttpMethod.DELETE, "/api/addresses/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/addresses/**").authenticated()
            .antMatchers("/api/users").authenticated()
            .antMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/users/**").authenticated()
            .antMatchers(HttpMethod.DELETE, API_ALL).hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers(HttpMethod.PUT, API_ALL).hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers(HttpMethod.PATCH, API_ALL).hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers(HttpMethod.POST, API_ALL).hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers(API_ALL).authenticated()
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/health/**").permitAll()
            .antMatchers("/management/info").permitAll()
            .antMatchers("/management/prometheus").permitAll()
            .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
        .and()
            .httpBasic()
        .and()
            .apply(securityConfigurerAdapter());
        // @formatter:on
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
    }
}
