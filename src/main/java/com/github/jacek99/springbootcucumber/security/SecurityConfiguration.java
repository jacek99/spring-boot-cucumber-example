package com.github.jacek99.springbootcucumber.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Spring Security configuration
 *
 * @author Jacek Furmankiewicz
 */
@Configuration
@EnableWebSecurity
@EnableGlobalAuthentication
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private TenantUserAuthenticationProvider authenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // TODO: temp for now
        http.csrf().disable();

        http.authorizeRequests().anyRequest().fullyAuthenticated();
        http.httpBasic();

//        http.antMatcher("/myapp/system/**")
//                // SYSTEM ADMIN APIs
//                .authorizeRequests()
//                .anyRequest().hasRole(SecurityConstants.ROLE_SYSTEM_ADMIN)
//                .and()
//                // TENANT ADMIN API
//                .antMatcher("/myapp/admin/**")
//                .authorizeRequests()
//                .anyRequest().hasRole(SecurityConstants.ROLE_TENANT_ADMIN)
//                .and()
//                // REGULAR TENANT USER API
//                .antMatcher("/myapp/api/**")
//                .authorizeRequests()
//                .anyRequest().hasRole(SecurityConstants.ROLE_TENANT_USER)
//                .and()
//                .httpBasic();
//


    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }
}
