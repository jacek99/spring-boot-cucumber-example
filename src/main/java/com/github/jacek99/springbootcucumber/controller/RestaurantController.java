package com.github.jacek99.springbootcucumber.controller;

import com.github.jacek99.springbootcucumber.dao.RestaurantDao;
import com.github.jacek99.springbootcucumber.dao.RestaurantDao;
import com.github.jacek99.springbootcucumber.domain.Restaurant;
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
 * REST resource for creating restaurants within a tenant
 * @author Jacek Furmankiewicz
 */
@RestController
@RequestMapping(value = "/myapp/api/restaurants", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RestaurantController {


    @Autowired
    private RestaurantDao dao;

    @RequestMapping(method = RequestMethod.GET)
    public List<Restaurant> getAll(@AuthenticationPrincipal TenantToken tenantToken) {
        return dao.findAll(tenantToken);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Restaurant save(@AuthenticationPrincipal TenantToken tenantToken, @RequestBody @Valid Restaurant entity) {
        dao.save(tenantToken, entity);
        return entity;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal TenantToken tenantToken, @PathVariable("id") String id,
                       @RequestBody @Valid Restaurant entity) {
        // path sanity check
        Preconditions.checkArgument(id.equals(entity.getId()),"id is not consistent with path");

        dao.update(tenantToken, entity);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveOrUpdate(@AuthenticationPrincipal TenantToken tenantToken, @RequestBody @Valid Restaurant entity) {
        dao.saveOrUpate(tenantToken, entity);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal TenantToken tenantToken, @PathVariable("id") String id) {
        dao.delete(tenantToken,id);
    }
}
