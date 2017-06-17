package com.github.jacek99.springbootcucumber;

import javax.validation.Valid;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties
 *
 * @author Jacek Furmankiewicz
 */
@Component
@ConfigurationProperties
public class AppConfig {

    @Getter
    @Valid
    private CassandraConfig cassandra = new CassandraConfig();

    @Data
    public static class CassandraConfig {
        @NotEmpty
        private String host;
        @Range(min = 1000, max = 32767)
        private int port;
        @NotEmpty
        private String keyspace;
        @Range(min = 1, max = 7)
        private int replicationFactor = 1;
        @NotEmpty
        private String replicationStrategy;
    }
}
