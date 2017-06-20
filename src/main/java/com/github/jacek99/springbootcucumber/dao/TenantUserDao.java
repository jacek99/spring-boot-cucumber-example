package com.github.jacek99.springbootcucumber.dao;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.github.jacek99.springbootcucumber.cassandra.CassandraConstants;
import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.domain.TenantUser;
import com.github.jacek99.springbootcucumber.security.PasswordHashingService;
import com.github.jacek99.springbootcucumber.security.SecurityConstants;
import com.github.jacek99.springbootcucumber.security.TenantToken;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_ACTIVE;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_PASSWORD_HASH;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_PASSWORD_REP;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_PASSWORD_SALT;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_ROLES;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_TENANT_ID;
import static com.github.jacek99.springbootcucumber.cassandra.CassandraConstants.COLUMN_USER_ID;

/**
 * DAO for all users within a tenant
 * @author Jacek Furmankiewicz
 */
@Repository
@Slf4j
public class TenantUserDao extends AbstractCassandraDao<TenantUser,TenantUserDao.TenantUserRow,String> {

    private static final String MASKED_PASSWORD = "**********";

    public static final String ADMIN = "admin";

    @Autowired
    private PasswordHashingService passwordHashingService;

    public TenantUserDao() {
        super(TenantUser.class,TenantUserRow.class);
    }

    @Override
    protected TenantUserRow toRow(@NonNull TenantUser entity) {
        // sanity check
        if (MASKED_PASSWORD.equals(entity.getPassword()) || StringUtils.isEmpty(entity.getPassword())) {
            throw new IllegalArgumentException("Tenant user must have valid password set");
        }

        // hash password
        PasswordHashingService.HashInfo rs = passwordHashingService.hashPassword(entity.getPassword());

        return TenantUserRow.builder()
                .tenantId(entity.getTenantId())
                .userId(entity.getUserId())
                .active(entity.isActive())
                .roles(entity.getRoles())
                .passwordHash(rs.getPasswordHash())
                .passwordSalt(rs.getSalt())
                .passwordHashRepetitions(rs.getRepetitions())
                .build();
    }

    @Override
    protected TenantUser toEntity(TenantUserRow row) {
        if (row == null) {
            return null;
        } else {
            return TenantUser.builder()
                    .tenantId(row.tenantId)
                    .userId(row.userId)
                    .roles(row.roles)
                    .active(row.active)
                    .password(MASKED_PASSWORD)
                    .build();
        }
    }

    @Override
    protected String getEntityId(TenantUser entity) {
        return entity.getUserId();
    }

    /**
     * For use in auth filter where the tenant may not be present yet
     */
    public Optional<TenantUser> findById(String tenantId, String userId) {
        return Optional.ofNullable(toEntity(getMapper().get(tenantId,userId)));
    }

    /**
     * Creates the system admin account if not present in DB
     */
    public void createSystemAdmin() {
        TenantUserRow row = getMapper().get(Tenant.SYSTEM_TENANT, ADMIN);
        if (row == null) {

            log.info("Creating {}.{} user...", Tenant.SYSTEM_TENANT,ADMIN);

            String pwd = "adminadmin";  // default pwd
            PasswordHashingService.HashInfo result = passwordHashingService
                    .hashPassword(pwd);

            row = TenantUserRow.builder()
                    .tenantId(Tenant.SYSTEM_TENANT)
                    .userId(ADMIN)
                    .active(true)
                    .passwordHash(result.getPasswordHash())
                    .passwordHashRepetitions(result.getRepetitions())
                    .passwordSalt(result.getSalt())
                    .roles(ImmutableSet.of(
                        SecurityConstants.ROLE_SYSTEM_ADMIN,
                        SecurityConstants.ROLE_TENANT_ADMIN,
                        SecurityConstants.ROLE_TENANT_USER
                    ))
                    .build();

            getMapper().save(row);

        } else {
            log.info("User {}.{} exists, skipping", Tenant.SYSTEM_TENANT,ADMIN);
        }
    }


    /**
     * Return user with password hash info, required by the authentication layer to verify incoming request
     */
    public Optional<TenantUserWithHashInformation> getPasswordHashInformation(String tenantId, String userId) {
            TenantUserRow row = getMapper().get(tenantId, userId);
            if (row == null) {
                return Optional.empty();
            } else {
                TenantUser user = toEntity(row);
                PasswordHashingService.HashInfo hashInfo = new PasswordHashingService.HashInfo(
                        row.passwordHash, row.passwordSalt, row.passwordHashRepetitions
                );
                return Optional.of(new TenantUserWithHashInformation(user, hashInfo));
            }
    }

    @Data
    @Builder
    @NoArgsConstructor @AllArgsConstructor
    @Table(name = CassandraConstants.TABLE_TENANT_USER)
    public static class TenantUserRow {

        @PartitionKey
        @Column(name = COLUMN_TENANT_ID)
        private String tenantId;

        @ClusteringColumn
        @Column(name = COLUMN_USER_ID)
        private String userId;

        @Column(name = COLUMN_ROLES)
        private Set<String> roles;

        @Column(name = COLUMN_ACTIVE)
        private boolean active;

        @Column(name = COLUMN_PASSWORD_HASH)
        private String passwordHash;

        @Column(name = COLUMN_PASSWORD_SALT)
        private String passwordSalt;

        @Column(name = COLUMN_PASSWORD_REP)
        private int passwordHashRepetitions;

    }

    // needed for the password hashing service for authentication verification
    @Value
    public static class TenantUserWithHashInformation {
        private TenantUser user;
        private PasswordHashingService.HashInfo hashInfo;
    }
}
