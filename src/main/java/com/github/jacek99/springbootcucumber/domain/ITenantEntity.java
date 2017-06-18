package com.github.jacek99.springbootcucumber.domain;

/**
 * Interface for all entities that are for a single tenant only
 * @author Jacek Furmankiewicx
 */
public interface ITenantEntity {

    /**
     * Should be the @PartitionKey of the entity as well
     */
    String getTenantId();

    void setTenantId(String id);

}
