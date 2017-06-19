package com.github.jacek99.springbootcucumber.exception;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Common handling logic for standard exceptions
 * @author Jacek Furmankiewicz
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException e) {
        ModelAndView mv = new ModelAndView("error");
        // TODO: not working , investigate
        mv.setStatus(HttpStatus.BAD_REQUEST);
        mv.addObject("error",e.getMessage());
        return mv;
    }
}
