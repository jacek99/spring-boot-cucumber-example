package com.github.jacek99.springbootcucumber.exception;

import com.github.jacek99.springbootcucumber.ThreadLocals;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Validation error
 * @author Jacek Furmankiewicz
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ConstraintViolationException extends RuntimeException {

    public ConstraintViolationException(@NonNull String propertyName, @NonNull String error) {
        super(ThreadLocals.STRINGBUILDER.get()
                .append(propertyName).append(":")
                .append(error).toString());
    }

}
