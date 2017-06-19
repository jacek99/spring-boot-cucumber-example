package com.github.jacek99.springbootcucumber.dao;

import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.domain.TenantUser;
import com.github.jacek99.springbootcucumber.security.PasswordHashingService;
import com.github.jacek99.springbootcucumber.security.SecurityConstants;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * DAO for all users within a tenant
 * @author Jacek Furmankiewicz
 */
@Repository
@Slf4j
public class TenantUserDao extends AbstractCassandraDao<TenantUser,String> {

    public static final String ADMIN = "admin";

    @Autowired
    private PasswordHashingService passwordHashingService;

    public TenantUserDao() {
        super(TenantUser.class);
    }

    @Override
    protected String getEntityId(TenantUser entity) {
        return entity.getUserId();
    }

    /**
     * For use in auth filter where the tenant may not be present yet
     */
    public Optional<TenantUser> findById(String tenantId, String userId) {
        return Optional.ofNullable(getMapper().get(tenantId,userId));
    }

    /**
     * Creates the system admin account if not present in DB
     */
    public void createSystemAdmin() {
        TenantUser user = getMapper().get(Tenant.SYSTEM_TENANT, ADMIN);
        if (user == null) {

            log.info("Creating {}.{} user...", Tenant.SYSTEM_TENANT,ADMIN);

            String pwd = "adminadmin";  // default pwd
            PasswordHashingService.HashResult result = passwordHashingService
                    .hashPassword(pwd);

            user = new TenantUser();
            user.setActive(true);
            user.setPasswordHash(result.getPasswordHash());
            user.setPasswordHashRepetitions(result.getRepetitions());
            user.setPasswordSalt(result.getSalt());
            user.setRoles(ImmutableList.of(
                    SecurityConstants.ROLE_SYSTEM_ADMIN,
                    SecurityConstants.ROLE_TENANT_ADMIN,
                    SecurityConstants.ROLE_TENANT_USER
            ));
            user.setTenantId(Tenant.SYSTEM_TENANT);
            user.setUserId(ADMIN);

            getMapper().save(user);

        } else {
            log.info("User {}.{} exists, skipping", Tenant.SYSTEM_TENANT,ADMIN);
        }
    }
}
