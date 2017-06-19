package com.github.jacek99.springbootcucumber.controller.admin;

import com.github.jacek99.springbootcucumber.dao.TenantUserDao;
import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.domain.TenantUser;
import com.github.jacek99.springbootcucumber.security.TenantToken;
import com.google.common.base.Preconditions;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST APIs for creating tenantTokens within a tenant
 * For tenant admins only
 * @author Jacek Furmankiewicz *
 */
@RestController
@RequestMapping(value = "/myapp/admin/tenantTokens", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TenantUserController {

    @Autowired
    private TenantUserDao dao;

    @RequestMapping(method = RequestMethod.GET)
    public List<TenantUser> getAll(@AuthenticationPrincipal TenantToken tenantToken) {
        return dao.findAll(tenantToken);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public TenantUser save(@AuthenticationPrincipal TenantToken tenantToken, @RequestBody @Valid TenantUser entity) {
        dao.save(tenantToken, entity);
        return entity;
    }

    @RequestMapping(value = "/{tenantTokenId}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal TenantToken tenantToken, @PathVariable("tenantTokenId") String tenantTokenID,
                       @RequestBody @Valid TenantUser entity) {
        // path sanity check
        Preconditions.checkArgument(tenantTokenID.equals(entity.getUserId()),"tenantTokenId is not consistent with path");

        dao.update(tenantToken, entity);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveOrUpdate(@AuthenticationPrincipal TenantToken tenantToken, @RequestBody @Valid TenantUser entity) {
        dao.saveOrUpate(tenantToken, entity);
    }

    @RequestMapping(value = "/{tenantTokenId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal TenantToken tenantToken, @PathVariable("tenantTokenId") String tenantTokenId) {
        dao.delete(tenantToken,tenantTokenId);
    }

}
