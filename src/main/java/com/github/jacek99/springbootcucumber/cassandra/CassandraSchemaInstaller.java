package com.github.jacek99.springbootcucumber.cassandra;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.github.jacek99.springbootcucumber.AppConfig;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_ID;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_NAME;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_URL;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.TABLE_TENANT;

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

    @PostConstruct
    public void init() {
        String keyspace = config.getCassandra().getKeyspace();
        createTenantTable(keyspace);
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

}
