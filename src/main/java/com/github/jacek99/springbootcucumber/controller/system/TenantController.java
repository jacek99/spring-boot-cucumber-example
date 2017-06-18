package com.github.jacek99.springbootcucumber.controller.system;

import com.github.jacek99.springbootcucumber.dao.TenantDao;
import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.security.TenantPrincipal;
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
 * Tenant REST service
 * ** Allowed for system admin users only **
 * @author Jacek Furmankiewicz
 */
@RestController
@RequestMapping(value = "/system/tenants",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TenantController {

    @Autowired
    private TenantDao dao;

    @RequestMapping(method = RequestMethod.GET)
    public List<Tenant> getAll(@AuthenticationPrincipal TenantPrincipal user) {
        return dao.findAll(user);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Tenant save(@AuthenticationPrincipal TenantPrincipal user, @RequestBody @Valid Tenant entity) {
        dao.save(user, entity);
        return entity;
    }

    @RequestMapping(value = "/{tenantId}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal TenantPrincipal user, @PathVariable("tenantId") String tenantId,
                       @RequestBody @Valid Tenant entity) {
        // path sanity check
        Preconditions.checkArgument(tenantId.equals(entity.getTenantId()),"tenantId is not consistent with path");

        dao.update(user, entity);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveOrUpdate(@AuthenticationPrincipal TenantPrincipal user, @RequestBody @Valid Tenant entity) {
        dao.saveOrUpate(user, entity);
    }

    @RequestMapping(value = "/{tenantId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal TenantPrincipal user, @PathVariable("tenantId") String tenantId) {
        dao.delete(user,tenantId);
    }

}
