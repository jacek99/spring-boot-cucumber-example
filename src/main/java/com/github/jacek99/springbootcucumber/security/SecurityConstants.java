package com.github.jacek99.springbootcucumber.security;

import com.google.common.collect.ImmutableSet;

/**
 * Common security constants
 *
 * @author Jacek Furmankiewicz
 */
public class SecurityConstants {

    /**
     * Allow to admin entire system, across all tenants
     */
    public static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";

    /**
     * User within single tenant
     */
    public static final String ROLE_TENANT_USER = "TENANT_USER";

    /**
     * Admin within single tenant
     */
    public static final String ROLE_TENANT_ADMIN = "TENANT_ADMIN";

    /**
     * For easy validation
     */
    public static final ImmutableSet<String> ALL_ROLES = ImmutableSet
            .of(ROLE_SYSTEM_ADMIN, ROLE_TENANT_ADMIN, ROLE_TENANT_USER);

}
