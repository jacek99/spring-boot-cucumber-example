package com.github.jacek99.springbootcucumber.controller.admin;

import com.github.jacek99.springbootcucumber.dao.TenantUserDao;
import com.github.jacek99.springbootcucumber.domain.Tenant;
import com.github.jacek99.springbootcucumber.domain.TenantUser;
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
 * REST APIs for creating users within a tenant
 * For tenant admins only
 * @author Jacek Furmankiewicz *
 */
@RestController
@RequestMapping(value = "/admin/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TenantUserController {

    @Autowired
    private TenantUserDao dao;

    @RequestMapping(method = RequestMethod.GET)
    public List<TenantUser> getAll(@AuthenticationPrincipal TenantPrincipal user) {
        return dao.findAll(user);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public TenantUser save(@AuthenticationPrincipal TenantPrincipal user, @RequestBody @Valid TenantUser entity) {
        dao.save(user, entity);
        return entity;
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal TenantPrincipal user, @PathVariable("userId") String userID,
                       @RequestBody @Valid TenantUser entity) {
        // path sanity check
        Preconditions.checkArgument(userID.equals(entity.getUserId()),"userId is not consistent with path");

        dao.update(user, entity);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveOrUpdate(@AuthenticationPrincipal TenantPrincipal user, @RequestBody @Valid TenantUser entity) {
        dao.saveOrUpate(user, entity);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal TenantPrincipal user, @PathVariable("userId") String userId) {
        dao.delete(user,userId);
    }

}
