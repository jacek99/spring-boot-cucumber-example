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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST APIs for creating tenant users within a tenant
 * For tenant admins only
 * @author Jacek Furmankiewicz *
 */
@RestController
@RequestMapping(value = "/myapp/admin/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TenantUserController {

    @Autowired
    private TenantUserDao dao;

    @RequestMapping(method = RequestMethod.GET)
    public List<TenantUser> getAll(@AuthenticationPrincipal TenantToken tenantToken) {
        return dao.findAll(tenantToken);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TenantUser> save(@AuthenticationPrincipal TenantToken tenantToken,
                                           @RequestBody @Valid TenantUser entity,
                                            UriComponentsBuilder b) {
        dao.save(tenantToken, entity);

        // return 201 and HTTP location header pointing to URI of new resource
        UriComponents uriComponents =
                b.path("/myapp/admin/users/{id}").buildAndExpand(entity.getUserId());
        return ResponseEntity.created(uriComponents.toUri()).body(entity);
    }

    @RequestMapping(value = "/{tenantId}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal TenantToken tenantToken, @PathVariable("tenantId") String tenantTokenID,
                       @RequestBody @Valid TenantUser entity) {
        // path sanity check
        Preconditions.checkArgument(tenantTokenID.equals(entity.getUserId()),"tenantId is not consistent with path");

        dao.update(tenantToken, entity);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveOrUpdate(@AuthenticationPrincipal TenantToken tenantToken, @RequestBody @Valid TenantUser entity) {
        dao.saveOrUpate(tenantToken, entity);
    }

    @RequestMapping(value = "/{tenantId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal TenantToken tenantToken, @PathVariable("tenantId") String tenantId) {
        dao.delete(tenantToken,tenantId);
    }

}
