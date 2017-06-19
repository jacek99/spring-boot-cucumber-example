package com.github.jacek99.springbootcucumber.cassandra;

/**
 * Common constants throught the DAO logic
 */
public class CassandraConstants {

    public static final String TABLE_TENANT = "tenant";
    public static final String TABLE_TENANT_USER = "tenant_user";
    public static final String TABLE_RESTAURANT = "restaurant";

    public static final String COLUMN_TENANT_ID = "tenant_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_ROLES = "roles";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_PASSWORD_SALT = "password_salt";
    public static final String COLUMN_PASSWORD_REP = "password_rep";
    public static final String COLUMN_ACTIVE = "active";
    public static final String COLUMN_COUNTRY_CODE = "country_code";
    public static final String COLUMN_STATE_CODE = "state_code";
    public static final String COLUMN_CURRENCY_CODE = "currency_code";
}
