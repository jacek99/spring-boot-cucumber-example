package com.github.jacek99.springbootcucumber.domain;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.Data;
import org.springframework.web.bind.annotation.CrossOrigin;

import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.*;

/**
 * A restaurant entity, within each tenant
 * @author Jacek Furmankiewicz
 */
@Data
@Table(name = TABLE_RESTAURANT)
public class Restaurant implements ITenantEntity, Comparable<Restaurant> {

    @PartitionKey
    @Column(name = COLUMN_TENANT_ID)
    private String tenantId;

    @ClusteringColumn
    @Column(name = COLUMN_ID)
    private String id;

    @Column(name = COLUMN_NAME)
    private String name;

    @Column(name = COLUMN_COUNTRY_CODE)
    private String countryCode;

    @Column(name = COLUMN_STATE_CODE)
    private String stateCode;

    @Override
    public int compareTo(Restaurant o) {
        if (this.id != null && o != null) {
            return this.id.compareTo(o.getId());
        } else {
            return 0;
        }
    }
}
