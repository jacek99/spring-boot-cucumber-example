package com.github.jacek99.springbootcucumber.domain;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.github.jacek99.springbootcucumber.cassandra.CassandraConstants;
import java.util.List;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.*;

/**
 * A user within a single tenant
 * @author Jacek Furmankiewicz
 */
@Data
@Table(name = CassandraConstants.TABLE_TENANT_USER)
public class TenantUser implements ITenantEntity, Comparable<TenantUser> {

    @PartitionKey
    @Column(name = COLUMN_TENANT_ID)
    @NotEmpty
    private String tenantId;

    @ClusteringColumn
    @Column(name = COLUMN_TENANT_ID)
    @NotEmpty
    private String userId;

    @Column(name = COLUMN_ROLES)
    private List<String> roles;

    @Column(name = COLUMN_PASSWORD_HASH)
    private String passwordHash;

    @Column(name = COLUMN_ACTIVE)
    private boolean active;

    @Override
    public int compareTo(TenantUser o) {
        if (this.userId != null & o != null) {
            return this.userId.compareTo(o.getUserId());
        } else {
            return 0;
        }
    }
}
