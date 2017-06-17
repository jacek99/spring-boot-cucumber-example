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
@Value
@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    private Class<?> type;
    private String id;
    private String reason;

    public ConflictException(Class<?> type, String id, String reason) {
        super(ThreadLocals.STRINGBUILDER.get()
                .append("Entity: ").append(type.getSimpleName())
                .append(" with ID ").append(id)
                .append(" raised conflict: ").append(reason)
                .toString());
        this.type = type;
        this.id = id;
        this.reason = reason;
    }

}
