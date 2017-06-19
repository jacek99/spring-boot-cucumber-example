package com.github.jacek99.springbootcucumber.cassandra;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.github.jacek99.springbootcucumber.AppConfig;
import com.github.jacek99.springbootcucumber.dao.TenantDao;
import com.github.jacek99.springbootcucumber.dao.TenantUserDao;
import com.github.jacek99.springbootcucumber.domain.Tenant;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_ACTIVE;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_COUNTRY_CODE;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_ID;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_NAME;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_PASSWORD_HASH;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_PASSWORD_REP;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_PASSWORD_SALT;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_ROLES;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_STATE_CODE;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_TENANT_ID;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_URL;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_USER_ID;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.TABLE_RESTAURANT;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.TABLE_TENANT;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.TABLE_TENANT_USER;

/**
 * Takes care of automatic schema installation
 *
 * @author Jacek Furmankiewicz
 */
@Component
@Slf4j
public class CassandraSchemaInstaller {

    @Autowired
    private CassandraService cassandra;
    @Autowired
    private AppConfig config;

    @Autowired
    private TenantDao tenantDao;
    @Autowired
    private TenantUserDao tenantUserDao;

    @PostConstruct
    public void init() {
        initDatabase();
    }

    /**
     * Creates all the tables and the min required data
     */
    private void initDatabase() {
        String keyspace = config.getCassandra().getKeyspace();

        // tables
        createTenantTable(keyspace);
        createTenantUserTable(keyspace);
        createRestaurantTable(keyspace);

        // base data
        tenantDao.createSystemTenant();
        tenantUserDao.createSystemAdmin();
    }

    private void createTenantTable(String keyspace) {
        TableMetadata meta = cassandra.getCluster().getMetadata().getKeyspace(keyspace)
                .getTable(TABLE_TENANT);
        if (meta == null) {
            log.info("Table {}.{} not found, creating....", keyspace, TABLE_TENANT);

            String cql = SchemaBuilder.createTable(TABLE_TENANT)
                    .addPartitionKey(COLUMN_ID, DataType.ascii())
                    .addColumn(COLUMN_NAME, DataType.ascii())
                    .addColumn(COLUMN_URL, DataType.ascii())
                    .withOptions()
                    .caching(SchemaBuilder.Caching.NONE)
                    .compactionOptions(SchemaBuilder.leveledStrategy())
                    .buildInternal();

            log.info("Executing CQL:\n{}", cql);
            cassandra.getSession().execute(cql);

        } else {
            log.info("Table {}.{} already found, skipping creation", keyspace, TABLE_TENANT);
        }
    }

    private void createTenantUserTable(String keyspace) {
        TableMetadata meta = cassandra.getCluster().getMetadata().getKeyspace(keyspace)
                .getTable(TABLE_TENANT_USER);
        if (meta == null) {
            log.info("Table {}.{} not found, creating....", keyspace, TABLE_TENANT_USER);

            String cql = SchemaBuilder.createTable(TABLE_TENANT_USER)
                    .addPartitionKey(COLUMN_TENANT_ID, DataType.ascii())
                    .addClusteringColumn(COLUMN_USER_ID, DataType.ascii())
                    .addColumn(COLUMN_ROLES, DataType.list(DataType.ascii()))
                    .addColumn(COLUMN_PASSWORD_HASH, DataType.ascii())
                    .addColumn(COLUMN_PASSWORD_SALT, DataType.ascii())
                    .addColumn(COLUMN_PASSWORD_REP, DataType.cint())
                    .addColumn(COLUMN_ACTIVE, DataType.cboolean())
                    .buildInternal();

            log.info("Executing CQL:\n{}", cql);
            cassandra.getSession().execute(cql);

        } else {
            log.info("Table {}.{} already found, skipping creation", keyspace, TABLE_TENANT_USER);
        }
    }

    private void createRestaurantTable(String keyspace) {
        TableMetadata meta = cassandra.getCluster().getMetadata().getKeyspace(keyspace)
                .getTable(TABLE_RESTAURANT);
        if (meta == null) {

            log.info("Table {}.{} not found, creating....", keyspace, TABLE_RESTAURANT);

            String cql = SchemaBuilder.createTable(TABLE_RESTAURANT)
                    .addPartitionKey(COLUMN_TENANT_ID, DataType.ascii())
                    .addClusteringColumn(COLUMN_ID, DataType.ascii())
                    .addColumn(COLUMN_NAME, DataType.ascii())
                    .addColumn(COLUMN_COUNTRY_CODE, DataType.ascii())
                    .addColumn(COLUMN_STATE_CODE, DataType.ascii())
                    .buildInternal();

            log.info("Executing CQL:\n{}", cql);
            cassandra.getSession().execute(cql);

        } else {
            log.info("Table {}.{} already found, skipping creation", keyspace, TABLE_TENANT_USER);
        }
    }

    /**
     * Truncates all the tables and resets database back to empty state
     * For testing support only
     *
     * Requires ENV variable to be set in order to allow it
     *
     * TEST_MODE = true
     *
     * otherwise wil get rejected
     */
    public void initForTesting() {

        String testMode = System.getenv("TEST_MODE");
        if ("true".equals(testMode)) {
            log.debug("Truncating DB to reset to empty state for testing");

            cassandra.getSession().execute("TRUNCATE " + TABLE_RESTAURANT);
            cassandra.getSession().execute("TRUNCATE " + TABLE_TENANT_USER);
            cassandra.getSession().execute("TRUNCATE " + TABLE_TENANT);

            // add any base data that is mandatory
            initDatabase();

        } else {
            throw new SecurityException("Attempt to truncate production DB!");
        }
    }

}
