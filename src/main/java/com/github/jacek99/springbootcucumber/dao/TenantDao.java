package com.github.jacek99.springbootcucumber.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.github.jacek99.springbootcucumber.cassandra.CassandraService;
import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.exception.ConflictException;
import com.github.jacek99.springbootcucumber.exception.NotFoundException;
import com.github.jacek99.springbootcucumber.security.TenantUser;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Tenant Cassandra DAO
 * @author Jacek Furmankiewicz
 */
@Repository
public class TenantDao implements IGenericDao<Tenant,String> {

    @Autowired
    private CassandraService cassandra;

    protected Mapper<Tenant> getMapper() {
        return cassandra.getMappingManager().mapper(Tenant.class);
    }

    @Override
    public Tenant findExistingById(TenantUser user, String id) {
        Tenant entity = getMapper().get(id);
        if (entity == null) {
            throw new NotFoundException(Tenant.class,id);
        } else {
            return entity;
        }
    }

    @Override
    public Optional<Tenant> findById(TenantUser user, String id) {
        return Optional.ofNullable(getMapper().get(id));
    }

    @Override
    public List<Tenant> findAll(TenantUser user) {
        ResultSet results = cassandra.getSession().execute("SELECT * FROM tenant");
        Result<Tenant> mapped = getMapper().map(results);
        return mapped.all();
    }

    @Override
    public void save(TenantUser user, Tenant entity) {
        if (findById(user,entity.getTenantId()).isPresent()) {
            throw new ConflictException(Tenant.class,entity.getTenantId(),"already exists");
        } else {
            getMapper().save(entity);
        }
    }

    @Override
    public void update(TenantUser user, Tenant entity) {
        // ensure entity already exists, since this an update
        if (findById(user,entity.getTenantId()).isPresent()) {
            getMapper().save(entity);
        } else {
            throw new NotFoundException(Tenant.class,entity.getTenantId());
        }
    }

    @Override
    public void delete(TenantUser user, String id) {
        // ensure entity already exists, since this an update
        if (findById(user,id).isPresent()) {
            getMapper().delete(id);
        } else {
            throw new NotFoundException(Tenant.class,id);
        }
    }
}
