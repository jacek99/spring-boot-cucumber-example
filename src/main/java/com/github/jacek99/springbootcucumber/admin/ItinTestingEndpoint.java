package com.github.jacek99.springbootcucumber.admin;

import com.github.jacek99.springbootcucumber.cassandra.CassandraSchemaInstaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.InfoEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * An endpoint that can be used for initializing the DB to an empty state
 * in-between BDD scenarios
 * @author Jacek Furmankiewicz
 */
@Component
public class ItinTestingEndpoint extends EndpointMvcAdapter {

    @Autowired
    private CassandraSchemaInstaller installer;

    /**
     * Create a new {@link EndpointMvcAdapter}.
     *
     * @param delegate the underlying {@link Endpoint} to adapt.
     */
    public ItinTestingEndpoint(AdminEndpoint delegate) {
        super(delegate);
    }


    @RequestMapping(value = "/test/init", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE)
    @ResponseBody
    public String init() {
        installer.initForTesting();
        return "DB reset";
    }
}
