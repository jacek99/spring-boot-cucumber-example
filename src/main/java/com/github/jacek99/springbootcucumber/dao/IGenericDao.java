package com.github.jacek99.springbootcucumber.dao;

import com.github.jacek99.springbootcucumber.security.TenantToken;
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
    E findExistingById(TenantToken tenantToken, ID id);

    /**
     * Finds the entity, if present
     */
    Optional<E> findById(TenantToken tenantToken, ID id);

    /**
     * Finds all entities for the specified tenant
     */
    List<E> findAll(TenantToken tenantToken);

    /**
     * Saves an entity within a tenant.
     * @throws com.github.jacek99.springbootcucumber.exception.ConflictException if entity with this ID already exists
     */
    void save(TenantToken tenantToken, E entity);

    /**
     * Updates an existing entity within a tenant
     * @throws com.github.jacek99.springbootcucumber.exception.NotFoundException if entity not found by ID
     */
    void update(TenantToken tenantToken, E entity);

    /**
     * Saves the entity, regardless of whether it existed previously or not
     * Good for batch uploads / mass replace, etc
     */
    void saveOrUpate(TenantToken tenantToken, E entity);

    /**
     * Deletes an entity by ID
     */
    void delete(TenantToken tenantToken, ID id);

}
