package com.github.jacek99.springbootcucumber.exception;

import com.github.jacek99.springbootcucumber.ThreadLocals;
import javax.validation.Valid;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an entity was not found in DB
 * @author Jacek Furmankiewicz
 */
@Value
@ResponseStatus(value= HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    private Class<?> type;
    private String id;

    public NotFoundException(Class<?> type, String id) {
        super(ThreadLocals.STRINGBUILDER.get()
            .append("Entity ").append(type.getSimpleName())
                .append(" with ID ").append(id)
                .append(" does not exist")
                .toString()
        );
        this.type = type;
        this.id = id;
    }
}
