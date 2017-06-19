package com.github.jacek99.springbootcucumber.controller.system;

import com.github.jacek99.springbootcucumber.dao.TenantDao;
import com.github.jacek99.springbootcucumber.domain.Tenant;
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
 * Tenant REST service
 * ** Allowed for system admin tenantTokens only **
 * @author Jacek Furmankiewicz
 */
@RestController
@RequestMapping(value = "/myapp/system/tenants",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TenantController {

    @Autowired
    private TenantDao dao;

    @RequestMapping(method = RequestMethod.GET)
    public List<Tenant> getAll(@AuthenticationPrincipal TenantToken tenantToken) {
        return dao.findAll(tenantToken);
    }

    @RequestMapping(value = "/{tenantId}", method = RequestMethod.GET)
    public Tenant getSingle(@AuthenticationPrincipal TenantToken tenantToken,
                                  @PathVariable("tenantId") String tenantId) {
        return dao.findExistingById(tenantToken, tenantId);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Tenant> save(@AuthenticationPrincipal TenantToken tenantToken, @RequestBody @Valid Tenant entity,
                       UriComponentsBuilder b) {
        dao.save(tenantToken, entity);

        // return 201 and HTTP location header pointing to URI of new resource
        UriComponents uriComponents =
                b.path("/myapp/system/tenants/{id}").buildAndExpand(entity.getTenantId());
        return ResponseEntity.created(uriComponents.toUri()).body(entity);
    }

    @RequestMapping(value = "/{tenantId}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal TenantToken tenantToken, @PathVariable("tenantId") String tenantId,
                       @RequestBody @Valid Tenant entity) {
        // path sanity check
        Preconditions.checkArgument(tenantId.equals(entity.getTenantId()),"tenantId is not consistent with path");

        dao.update(tenantToken, entity);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveOrUpdate(@AuthenticationPrincipal TenantToken tenantToken, @RequestBody @Valid Tenant entity) {
        dao.saveOrUpate(tenantToken, entity);
    }

    @RequestMapping(value = "/{tenantId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal TenantToken tenantToken, @PathVariable("tenantId") String tenantId) {
        dao.delete(tenantToken,tenantId);
    }

}
