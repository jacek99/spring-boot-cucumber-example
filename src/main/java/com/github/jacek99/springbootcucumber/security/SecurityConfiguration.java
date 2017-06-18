package com.github.jacek99.springbootcucumber.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Spring Security configuration
 *
 * @author Jacek Furmankiewicz
 */
@Configuration
@EnableWebSecurity
@EnableGlobalAuthentication
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/system/**")
                // SYSTEM ADMIN APIs
                .authorizeRequests()
                .anyRequest().hasRole(SecurityConstants.ROLE_SYSTEM_ADMIN)
                .and()
                // TENANT ADMIN API
                .antMatcher("/admin/**")
                .authorizeRequests()
                .anyRequest().hasRole(SecurityConstants.ROLE_TENANT_ADMIN)
                .and()
                // REGULAR TENANT USER API
                .antMatcher("/api/**")
                .authorizeRequests()
                .anyRequest().hasRole(SecurityConstants.ROLE_TENANT_USER)
                .and()
                .httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //TODO: fix to fetch from Cassandra
        auth.inMemoryAuthentication()
                .withUser("admin")
                .password("adminadmin")
                .roles(SecurityConstants.ROLE_SYSTEM_ADMIN);
    }
}
