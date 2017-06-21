package com.github.jacek99.springbootcucumber.domain;

import com.github.jacek99.springbootcucumber.validator.SecurityRoles;
import java.util.Set;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

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
    @Size(min=4,max=30)
    private String userId;

    @Size(min = 1, max = 3)
    @SecurityRoles // custom validator to ensure only valid security roles are accepted
    private Set<String> roles;

    private boolean active;

    @NotEmpty
    @Size(min = 5, max = 100)
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
