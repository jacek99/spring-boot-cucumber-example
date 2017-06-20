package com.github.jacek99.springbootcucumber.domain;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jacek99.springbootcucumber.cassandra.CassandraConstants;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.NotEmpty;

import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.*;

/**
 * A user within a single tenant
 * @author Jacek Furmankiewicz
 */
@Data
@Builder
public class TenantUser implements ITenantEntity, Comparable<TenantUser> {

    @NotEmpty
    private String tenantId;

    @NotEmpty
    private String userId;

    private Set<String> roles;

    private boolean active;

    @NotEmpty
    // used only for input, dummy value on return
    private String password;

    @Override
    public int compareTo(TenantUser o) {
        if (this.userId != null & o != null) {
            return this.userId.compareTo(o.getUserId());
        } else {
            return 0;
        }
    }
}
