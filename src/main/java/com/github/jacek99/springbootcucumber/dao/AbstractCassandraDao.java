package com.github.jacek99.springbootcucumber.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Table;
import com.github.jacek99.springbootcucumber.cassandra.CassandraService;
import com.github.jacek99.springbootcucumber.domain.ITenantEntity;
import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.exception.ConflictException;
import com.github.jacek99.springbootcucumber.exception.NotFoundException;
import com.github.jacek99.springbootcucumber.security.TenantPrincipal;
import com.google.common.collect.Lists;
import com.sun.org.apache.regexp.internal.RE;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Abstract ancestor for all entity DAOs
 * @author Jacek Furmankiewicz
 */
public abstract class AbstractCassandraDao<E extends Comparable<E>,ID> implements IGenericDao<E,ID> {

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private CassandraService cassandra;

    @Getter(AccessLevel.PUBLIC)
    private final Class<E> entityType;

    private final String tableName;

    /**
     * Tells if this entity is specific to a single tenant (usually the case)
     * or system-wide
     */
    @Getter(AccessLevel.PUBLIC)
    private final boolean tenantEntity;

    protected AbstractCassandraDao(Class<E> entityType) {
        this.entityType = entityType;
        tenantEntity = entityType.isAssignableFrom(ITenantEntity.class);
        tableName = entityType.getAnnotation(Table.class).name();
    }

    protected Mapper<E> getMapper() {
        return cassandra.getMappingManager().mapper(entityType);
    }

    /**
     * Every DAO can override this if the mapping between
     * tenant / entity ID is more complex than the most basic case
     */
    protected Object[] getQueryColumns(TenantPrincipal user, ID id) {
        if (isTenantEntity()) {
            // every tenant entity should have the tenant ID as the partition key
            // and the entity ID as one or more clustering columns (depending on complexity)
            return new Object[]{user.getTenant().getTenantId(), id};
        } else {
            return new Object[]{id};
        }
    }

    @Override
    public E findExistingById(TenantPrincipal user, ID id) {
        E entity = getMapper().get(getQueryColumns(user,id));
        if (entity == null) {
            throw new NotFoundException(entityType,String.valueOf(id));
        } else {
            return entity;
        }
    }

    @Override
    public Optional<E> findById(TenantPrincipal user, ID id) {
        return Optional.ofNullable(getMapper().get(getQueryColumns(user,id)));
    }

    @Override
    public List<E> findAll(TenantPrincipal user) {
        Select select = QueryBuilder.select().from(tableName);
        ResultSet results = cassandra.getSession().execute(select);
        Result<E> mapped = getMapper().map(results);
        // ensure always sorted for consistency & testability
        List<E> all = mapped.all();
        Collections.sort(all);
        return all;
    }

    @Override
    public void save(TenantPrincipal user, E entity) {
        ID id = getEntityId(entity);
        if (findById(user,id).isPresent()) {
            throw new ConflictException(entityType,String.valueOf(id),"already exists");
        } else {
            getMapper().save(entity);
        }
    }

    @Override
    public void update(TenantPrincipal user, E entity) {
        ID id = getEntityId(entity);
        // ensure entity already exists, since this an update
        if (findById(user,id).isPresent()) {
            getMapper().save(entity);
        } else {
            throw new NotFoundException(entityType,String.valueOf(id));
        }
    }

    @Override
    public void saveOrUpate(TenantPrincipal user, E entity) {
        getMapper().save(entity);
    }

    @Override
    public void delete(TenantPrincipal user, ID id) {
        // ensure entity already exists, since this an update
        if (findById(user,id).isPresent()) {
            getMapper().delete(id);
        } else {
            throw new NotFoundException(entityType,String.valueOf(id));
        }
    }

    /**
     * Needs to be overriden so that we can always find what is the ID
     * of an entity without having to resort to slow reflection
     */
    protected abstract ID getEntityId(E entity);
}
