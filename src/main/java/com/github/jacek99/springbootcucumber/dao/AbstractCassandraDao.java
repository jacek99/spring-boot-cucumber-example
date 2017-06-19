package com.github.jacek99.springbootcucumber.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Table;
import com.github.jacek99.springbootcucumber.cassandra.CassandraService;
import com.github.jacek99.springbootcucumber.domain.ITenantEntity;
import com.github.jacek99.springbootcucumber.exception.ConflictException;
import com.github.jacek99.springbootcucumber.exception.ConstraintViolationException;
import com.github.jacek99.springbootcucumber.exception.NotFoundException;
import com.github.jacek99.springbootcucumber.security.TenantToken;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.validation.ConstraintViolation;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Abstract ancestor for all entity DAOs
 * @author Jacek Furmankiewicz
 */
public abstract class AbstractCassandraDao<E extends Comparable<E>,ID> implements IGenericDao<E,ID> {

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private CassandraService cassandra;

    @Autowired
    private LocalValidatorFactoryBean validator;

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
        tenantEntity = ITenantEntity.class.isAssignableFrom(entityType);
        tableName = entityType.getAnnotation(Table.class).name();
    }

    protected Mapper<E> getMapper() {
        return cassandra.getMappingManager().mapper(entityType);
    }

    /**
     * Every DAO can override this if the mapping between
     * tenant / entity ID is more complex than the most basic case
     */
    protected Object[] getQueryColumns(TenantToken tenantToken, ID id) {
        if (isTenantEntity()) {
            // every tenant entity should have the tenant ID as the partition key
            // and the entity ID as one or more clustering columns (depending on complexity)
            return new Object[]{tenantToken.getTenant().getTenantId(), id};
        } else {
            return new Object[]{id};
        }
    }

    @Override
    public E findExistingById(TenantToken tenantToken, ID id) {
        E entity = getMapper().get(getQueryColumns(tenantToken,id));
        if (entity == null) {
            throw new NotFoundException(entityType,String.valueOf(id));
        } else {
            return entity;
        }
    }

    @Override
    public Optional<E> findById(TenantToken tenantToken, ID id) {
        return Optional.ofNullable(getMapper().get(getQueryColumns(tenantToken,id)));
    }

    @Override
    public List<E> findAll(TenantToken tenantToken) {
        Select select = QueryBuilder.select().from(tableName);
        ResultSet results = cassandra.getSession().execute(select);
        Result<E> mapped = getMapper().map(results);
        // ensure always sorted for consistency & testability
        List<E> all = mapped.all();
        Collections.sort(all);
        return all;
    }

    protected void processSave(TenantToken tenantToken, E entity) {
        // ensure it belongs to the right tenant
        if (entity instanceof ITenantEntity) {
            ((ITenantEntity)entity).setTenantId(tenantToken.getTenant().getTenantId());
        }

        // ensure it gets validated
        Set<ConstraintViolation<E>> errors = validator.validate(entity);
        if (errors != null && !errors.isEmpty()) {
            // sort them in alphabetical order, for consistency in testing
            Set<ConstraintViolation<E>> sorted = new TreeSet<>(
                    (o1,o2) -> o1.getPropertyPath().toString()
                    .compareTo(o2.getPropertyPath().toString())
            );
            sorted.addAll(errors);

            ConstraintViolation first = sorted.iterator().next();
            throw new ConstraintViolationException(
                    first.getPropertyPath().toString(),
                    first.getMessage());
        }

        // perform actual save
        getMapper().save(entity);

    }

    @Override
    public void save(TenantToken tenantToken, E entity) {
        ID id = getEntityId(entity);
        if (findById(tenantToken,id).isPresent()) {
            throw new ConflictException(entityType,String.valueOf(id),"already exists");
        } else {
            processSave(tenantToken, entity);
        }
    }

    @Override
    public void update(TenantToken tenantToken, E entity) {
        ID id = getEntityId(entity);
        // ensure entity already exists, since this an update
        if (findById(tenantToken,id).isPresent()) {
            processSave(tenantToken, entity);
        } else {
            throw new NotFoundException(entityType,String.valueOf(id));
        }
    }

    @Override
    public void saveOrUpate(TenantToken tenantToken, E entity) {
        processSave(tenantToken, entity);
    }

    @Override
    public void delete(TenantToken tenantToken, ID id) {
        // ensure entity already exists, since this an update
        if (findById(tenantToken,id).isPresent()) {
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
