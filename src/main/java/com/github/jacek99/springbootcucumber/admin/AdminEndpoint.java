package com.github.jacek99.springbootcucumber.admin;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.stereotype.Component;

/**
 * Base Actuator endpoint for admin tasks
 */
@Component
public class AdminEndpoint implements Endpoint<String> {
    @Override
    public String getId() {
        return "admin";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isSensitive() {
        return true;
    }

    @Override
    public String invoke() {
        return "";
    }
}
