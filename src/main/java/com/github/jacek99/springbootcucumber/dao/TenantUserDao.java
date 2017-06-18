package com.github.jacek99.springbootcucumber.dao;

import com.github.jacek99.springbootcucumber.domain.TenantUser;
import org.springframework.stereotype.Repository;

/**
 * DAO for all users within a tenant
 * @author Jacek Furmankiewicz
 */
@Repository
public class TenantUserDao extends AbstractCassandraDao<TenantUser,String> {

    protected TenantUserDao() {
        super(TenantUser.class);
    }

    @Override
    protected String getEntityId(TenantUser entity) {
        return entity.getUserId();
    }
}
