package com.github.jacek99.springbootcucumber.dao;

import com.github.jacek99.springbootcucumber.security.TenantPrincipal;
import java.util.List;
import java.util.Optional;

/**
 * Standard DAO inteface, independent of any DB implementation
 * @author Jacek Furmankiewicz
 *
 * E = entity type
 * ID = ID type (usually String)
 */
public interface IGenericDao<E extends Comparable<E>,ID> {

    /**
     * Finds the entity. If not found throws 404 error
     */
    E findExistingById(TenantPrincipal user, ID id);

    /**
     * Finds the entity, if present
     */
    Optional<E> findById(TenantPrincipal user, ID id);

    /**
     * Finds all entities for the specified tenant
     */
    List<E> findAll(TenantPrincipal user);

    /**
     * Saves an entity within a tenant.
     * @throws com.github.jacek99.springbootcucumber.exception.ConflictException if entity with this ID already exists
     */
    void save(TenantPrincipal user, E entity);

    /**
     * Updates an existing entity within a tenant
     * @throws com.github.jacek99.springbootcucumber.exception.NotFoundException if entity not found by ID
     */
    void update(TenantPrincipal user, E entity);

    /**
     * Saves the entity, regardless of whether it existed previously or not
     * Good for batch uploads / mass replace, etc
     */
    void saveOrUpate(TenantPrincipal user, E entity);

    /**
     * Deletes an entity by ID
     */
    void delete(TenantPrincipal user, ID id);

}
