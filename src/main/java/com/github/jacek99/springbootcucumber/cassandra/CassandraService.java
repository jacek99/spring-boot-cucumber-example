package com.github.jacek99.springbootcucumber.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.schemabuilder.CreateKeyspace;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.mapping.MappingManager;
import com.github.jacek99.springbootcucumber.AppConfig;
import com.google.common.collect.ImmutableMap;
import java.net.InetSocketAddress;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Provides Cassandra connectivity
 *
 * @author Jacek Furmankiewicz
 */
@Component
@Slf4j
public class CassandraService {

    @Autowired
    private AppConfig config;

    @Getter
    private Cluster cluster;
    @Getter
    private Session session;
    @Getter
    private MappingManager mappingManager;

    @PostConstruct
    public void init() {
        // connect to DB
        log.info("Connecting to Cassandra {}:{}", config.getCassandra().getHost(), config.getCassandra().getPort());

        QueryOptions queryOptions = new QueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        cluster = Cluster.builder()
                .addContactPointsWithPorts(new InetSocketAddress(config.getCassandra().getHost(),
                        config.getCassandra().getPort()))
                .withQueryOptions(queryOptions)
                .build();

        session  = connectToKeyspace();
        mappingManager = new MappingManager(session);
    }

    @PreDestroy
    public void cleanup() {

        if (session != null) {
            session.close();
        }
        if (cluster != null) {
            cluster.close();
        }
    }

    private Session connectToKeyspace() {
        String keyspace = config.getCassandra().getKeyspace();

        KeyspaceMetadata meta = cluster.getMetadata().getKeyspace(keyspace);
        if (meta == null) {
            createKeyspace(keyspace);
        }

        log.info("Connecting to Cassandra keyspace {}",keyspace);
        return cluster.connect(keyspace);
    }

    private void createKeyspace(String keyspace) {
        Statement create = SchemaBuilder.createKeyspace(keyspace)
                .ifNotExists()
                .with()
                .replication(ImmutableMap.of(
                        "class", config.getCassandra().getReplicationStrategy(),
                        "replication_factor", config.getCassandra().getReplicationFactor()));

        log.info("Creating Cassandra keyspace {}",keyspace);
        cluster.connect().execute(create);
    }
}
