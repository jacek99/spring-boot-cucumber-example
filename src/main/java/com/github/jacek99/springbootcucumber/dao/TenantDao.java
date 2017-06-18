package com.github.jacek99.springbootcucumber.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.github.jacek99.springbootcucumber.cassandra.CassandraService;
import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.exception.ConflictException;
import com.github.jacek99.springbootcucumber.exception.NotFoundException;
import com.github.jacek99.springbootcucumber.security.TenantPrincipal;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Tenant Cassandra DAO
 * @author Jacek Furmankiewicz
 */
@Repository
public class TenantDao extends AbstractCassandraDao<Tenant,String> {

    public TenantDao() {
        super(Tenant.class);
    }

    @Override
    protected String getEntityId(@NonNull Tenant entity) {
        return entity.getTenantId();
    }
}
