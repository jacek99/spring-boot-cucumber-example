package com.github.jacek99.springbootcucumber.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Table;
import com.github.jacek99.springbootcucumber.ThreadLocals;
import com.github.jacek99.springbootcucumber.cassandra.CassandraConstants;
import com.github.jacek99.springbootcucumber.cassandra.CassandraService;
import com.github.jacek99.springbootcucumber.domain.ITenantEntity;
import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.domain.TenantUser;
import com.github.jacek99.springbootcucumber.exception.ConflictException;
import com.github.jacek99.springbootcucumber.exception.ConstraintViolationException;
import com.github.jacek99.springbootcucumber.exception.NotFoundException;
import com.github.jacek99.springbootcucumber.security.TenantToken;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Abstract ancestor for all entity DAOs
 *
 * E = entity type
 * R = row type (usually the same as E, but can be different if the type exposed via DAO is different
 *     than type actually saved as row in the DB)
 * ID = entity ID type (usually String)
 *
 * @author Jacek Furmankiewicz
 */
public abstract class AbstractCassandraDao<E extends Comparable<E>,R,ID> implements IGenericDao<E,ID> {

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private CassandraService cassandra;

    @Autowired
    private LocalValidatorFactoryBean validator;

    @Getter(AccessLevel.PUBLIC)
    private final Class<E> entityType;

    @Getter(AccessLevel.PROTECTED)
    private final Class<R> rowType;

    private final String tableName;

    /**
     * Tells if this entity is specific to a single tenant (usually the case)
     * or system-wide
     */
    @Getter(AccessLevel.PUBLIC)
    private final boolean tenantEntity;

    /**
     * Constructor
     * For simple entities where the entity type & row type are the same
     */
    protected AbstractCassandraDao(Class<E> entityType) {
        this(entityType,(Class<R>)entityType);
    }

    /**
     * Constructor
     * For more complex types, where the entity type and row type are different
     */
    protected AbstractCassandraDao(Class<E> entityType, Class<R> rowType) {
        this.entityType = entityType;
        this.rowType = rowType;
        tenantEntity = ITenantEntity.class.isAssignableFrom(entityType);

        Table table = rowType.getAnnotation(Table.class);
        Preconditions.checkArgument(table != null,"Row entity needs to be annotated with @Table");
        tableName = table.name();
    }

    protected Mapper<R> getMapper() {
        return cassandra.getMappingManager().mapper(rowType);
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
        E entity = toEntity(getMapper().get(getQueryColumns(tenantToken,id)));
        if (entity == null) {
            throw new NotFoundException(entityType,String.valueOf(id));
        } else {
            return entity;
        }
    }

    @Override
    public Optional<E> findById(TenantToken tenantToken, ID id) {
        return Optional.ofNullable(toEntity(getMapper().get(getQueryColumns(tenantToken,id))));
    }

    @Override
    public List<E> findAll(TenantToken tenantToken) {
        // limit queries to tenant (unless system tenant)
        Select select = QueryBuilder.select().from(tableName);
        if (isTenantEntity() && !tenantToken.isSystemTenant()) {
            select.where(QueryBuilder.eq(CassandraConstants.COLUMN_TENANT_ID,tenantToken.getTenant().getTenantId()));
        }

        ResultSet results = cassandra.getSession().execute(select);
        Result<R> mapped = getMapper().map(results);
        // ensure always sorted for consistency & testability
        List<E> all = toEntities(mapped.all());
        Collections.sort(all);
        return all;
    }

    protected void validateSecurity(TenantToken tenantToken, ITenantEntity entity) {

        if (StringUtils.isEmpty(entity.getTenantId())) {
            // should never happen, should have gotten validated at the REST layer
            throw new IllegalArgumentException(ThreadLocals.STRINGBUILDER.get()
                    .append(getEntityType().getSimpleName())
                    .append(" identified by ID ").append(getEntityId((E)entity))
                    .append(" is missing tenantId")
                    .toString());
        } else if (!tenantToken.getTenant().getTenantId().equals(Tenant.SYSTEM_TENANT) &&
            !tenantToken.getTenant().getTenantId().equals(entity.getTenantId())) {
            // system tenant can create entities for any tenant,
            // but otherwise tenants can only create entities for their own tenant
            throw new SecurityException(ThreadLocals.STRINGBUILDER.get()
                    .append("Tenant ").append(tenantToken.getTenant().getTenantId())
                    .append(" user ").append(tenantToken.getUser().getUserId())
                    .append(" attempted to create entity of type ").append(getEntityType().getSimpleName())
                    .append(" for tenant ").append(entity.getTenantId())
                    .toString());
        }
    }

    protected void processSave(TenantToken tenantToken, E entity) {
        // ensure it belongs to the right tenant
        if (isTenantEntity()) {
            validateSecurity(tenantToken, (ITenantEntity) entity);
        }

        // ensure it gets validated, should have already happened at the REST layer
        // but just in case
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
        getMapper().save(toRow(entity));

    }

    @Override
    public void save(TenantToken tenantToken, E entity) {
        ID id = getEntityId(entity);
        if (findById(tenantToken,id).isPresent()) {
            throw new ConflictException(entityType,String.valueOf(id),
                    ThreadLocals.STRINGBUILDER.get()
                    .append(getEntityType().getSimpleName())
                    .append(" identified by ID ")
                    .append(id)
                    .append(" already exists").toString());

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
     * Needs to be overriden if the entity type & row type are different
     */
    protected R toRow(E entity) {
       if (rowType.equals(entityType)) {
           return (R)entity;
       } else {
           throw new NotImplementedException("Need to override to toRow() due to entity/row types being different");
       }
    }

    /**
     * Needs to be overriden if the entity type & row type are different
     */
    protected E toEntity(R row) {
        if (rowType.equals(entityType)) {
            return (E)row;
        } else {
            throw new NotImplementedException("Need to override to toRow() due to entity/row types being different");
        }
    }

    // converts a list of entities to rows
    protected List<R> toRows(@NonNull List<E> entities) {
        return entities.stream()
                .map(this::toRow)
                .collect(Collectors.toList());
    }

    protected List<E> toEntities(@NonNull List<R> rows) {
        return rows.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Needs to be overriden so that we can always find what is the ID
     * of an entity without having to resort to slow reflection
     */
    protected abstract ID getEntityId(E entity);

}
