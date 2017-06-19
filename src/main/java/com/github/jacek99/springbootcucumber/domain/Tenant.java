package com.github.jacek99.springbootcucumber.domain;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.github.jacek99.springbootcucumber.cassandra.CassandraConstants;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

/**
 * Tenant entity
 *
 * @author Jacek Furmankiewicz
 */
@Table(name = CassandraConstants.TABLE_TENANT)
@Data
public class Tenant implements Comparable<Tenant> {

    public static final String SYSTEM_TENANT = "system";

    @PartitionKey
    @Column(name = CassandraConstants.COLUMN_ID)
    @NotEmpty
    @Size(min=2,max=10)
    private String tenantId;

    @Column(name = CassandraConstants.COLUMN_NAME)
    @NotEmpty
    @Size(min=2,max=30)
    private String name;

    @Column(name = CassandraConstants.COLUMN_URL)
    @URL
    private String url;

    @Override
    public int compareTo(Tenant o) {
        if (this.tenantId != null && o != null) {
            return this.tenantId.compareTo(o.getTenantId());
        } else {
            return 0;
        }
    }
}
