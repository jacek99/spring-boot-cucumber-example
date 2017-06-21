package com.github.jacek99.springbootcucumber.security;

import com.github.jacek99.springbootcucumber.dao.TenantDao;
import com.github.jacek99.springbootcucumber.dao.TenantUserDao;
import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.domain.TenantUser;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Connects Spring Security to our TenantUser entity
 * @author Jacek Furmankiewicz
 */
@Component
public class TenantUserAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private PasswordHashingService passwordHashingService;
    @Autowired
    private TenantDao tenantDao;
    @Autowired
    private TenantUserDao userDao;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String userName = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        //  verify tenant
        //  user name format should be <user>@<tenant url>, john@company.com
        String[] parts = StringUtils.split(userName,"@");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Unknown user");
        }

        String tenantId = parts[1];
        Tenant tenant = tenantDao.findById(tenantId)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));

        // find user
        String userId = parts[0];
        TenantUserDao.TenantUserWithHashInformation user = userDao.getPasswordHashInformation(tenantId, userId)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));

        // validate pwd
        PasswordHashingService.HashInfo hashInfo = user.getHashInfo();
        if (passwordHashingService.isHashValid(password, hashInfo.getPasswordHash(), hashInfo.getSalt(),
                hashInfo.getRepetitions())) {

            List<GrantedAuthority> roles = user.getUser().getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority(r))
                    .collect(Collectors.toList());

            return new TenantToken(user.getUser(), tenant, roles);

        } else {
            // delay 1 sec to make brute force attacks harder
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}

            throw new BadCredentialsException("Wrong password");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication));
    }

}
