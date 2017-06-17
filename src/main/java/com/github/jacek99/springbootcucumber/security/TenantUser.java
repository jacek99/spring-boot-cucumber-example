package com.github.jacek99.springbootcucumber.security;

import com.github.jacek99.springbootcucumber.domain.Tenant;
import java.security.Principal;
import lombok.Data;

/**
 * A user within a specific tenant
 *
 * @author Jacek Furmankiewicz
 */
@Data
public class TenantUser implements Principal {

    private String name;

    private Tenant tenant;
}
