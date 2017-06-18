package com.github.jacek99.springbootcucumber.security;

/**
 * Common security constants
 *
 * @author Jacek Furmankiewicz
 */
public class SecurityConstants {

    /**
     * Allow to admin entire system, across all tenants
     */
    public static final String ROLE_SYSTEM_ADMIN = "ROLE_SYSTEM_ADMIN";

    /**
     * User within single tenant
     */
    public static final String ROLE_TENANT_USER = "ROLE_TENANT_ADMIN";

    /**
     * Admin within single tenant
     */
    public static final String ROLE_TENANT_ADMIN = "ROLE_TENANT_ADMIN";


}
