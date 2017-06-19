package com.github.jacek99.springbootcucumber.exception;

import com.datastax.driver.core.Cluster;
import com.github.jacek99.springbootcucumber.ThreadLocals;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when entity cannot be saved to DB due to conflict with existing data (e.g. entity already exists with
 * same ID). Should result in 409
 *
 * @author Jacek Furmankiewcz
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    private final Class<?> type;
    private final String id;
    private final String reason;

    public ConflictException(Class<?> type, String id, String message) {
        super(message);
        this.type = type;
        this.id = id;
        this.reason = message;
    }

}
