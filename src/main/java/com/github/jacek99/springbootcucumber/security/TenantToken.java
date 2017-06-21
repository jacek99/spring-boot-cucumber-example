package com.github.jacek99.springbootcucumber.security;

import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.domain.TenantUser;
import java.util.Collection;
import java.util.Collections;
import lombok.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Request context, which serves as a security token
 * as well as providing information about current logged in user, roles
 * and most importantly, which TENANT the user belongs to
 * @author Jacek Furmankiewicx
 */
@Value
public class TenantToken implements Authentication {

    private TenantUser user;
    private Tenant tenant;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        // ignore
    }

    @Override
    public String getName() {
        return user.getUserId();
    }

    public boolean isSystemTenant() {
        return Tenant.SYSTEM_TENANT.equals(tenant.getTenantId());
    }

    /**
     * Helper method for common use cases (to cut down on verbosity)
     */
    public String getTenantId() {
        return getTenant().getTenantId();
    }
}
