package com.github.jacek99.springbootcucumber.dao;

import com.github.jacek99.springbootcucumber.domain.Tenant;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * Tenant Cassandra DAO
 * @author Jacek Furmankiewicz
 */
@Repository
@Slf4j
public class TenantDao extends AbstractCassandraDao<Tenant,String> {

    public TenantDao() {
        super(Tenant.class);
    }

    @Override
    protected String getEntityId(@NonNull Tenant entity) {
        return entity.getTenantId();
    }

    /**
     * Makes sure system tenant is created on an empty DB
     */
    public void createSystemTenant() {
        Tenant system = getMapper().get(Tenant.SYSTEM_TENANT);
        if (system == null) {
            log.info("Creating {} tenant", Tenant.SYSTEM_TENANT);

            system = new Tenant();
            system.setTenantId(Tenant.SYSTEM_TENANT);
            system.setName(Tenant.SYSTEM_TENANT);
            system.setUrl("system");

            getMapper().save(system);
        } else {
            log.info("{} tenant already exists, skipping", Tenant.SYSTEM_TENANT);
        }
    }
}
