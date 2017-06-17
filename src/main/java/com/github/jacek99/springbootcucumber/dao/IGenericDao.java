package com.github.jacek99.springbootcucumber.dao;

import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.security.TenantUser;
import java.util.List;
import java.util.Optional;

/**
 * Standard DAO inteface, independent of any DB implementation
 * @author Jacek Furmankiewicz
 *
 * E = entity type
 * ID = ID type (usually String)
 */
public interface IGenericDao<E,ID> {

    /**
     * Finds the entity. If not found throws 404 error
     */
    E findExistingById(TenantUser user, ID id);

    /**
     * Finds the entity, if present
     */
    Optional<E> findById(TenantUser user, ID id);

    /**
     * Finds all entities for the specified tenant
     */
    List<E> findAll(TenantUser user);

    /**
     * Saves an entity within a tenant.
     * @throws com.github.jacek99.springbootcucumber.exception.ConflictException if entity with this ID already exists
     */
    void save(TenantUser user, E entity);

    /**
     * Updates an existing entity within a tenant
     * @throws com.github.jacek99.springbootcucumber.exception.NotFoundException if entity not found by ID
     */
    void update(TenantUser user, E entity);

    /**
     * Deletes an entity by ID
     */
    void delete(TenantUser user, ID id);

}
