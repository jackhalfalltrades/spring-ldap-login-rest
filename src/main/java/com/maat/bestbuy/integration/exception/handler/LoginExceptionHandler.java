package com.maat.bestbuy.integration.exception.handler;

import com.maat.bestbuy.integration.model.ErrorInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;

@ControllerAdvice
public class LoginExceptionHandler extends DefaultHandlerExceptionResolver {

    @ExceptionHandler(HttpClientErrorException.class)
    private ResponseEntity<ErrorInfo> handleHttpClientException(HttpStatusCodeException ex,
                                                                HttpServletRequest request, HttpServletResponse response, Object handler) {
        return new ResponseEntity<>(new ErrorInfo("", "", ex.getStatusCode(), ex.getMessage(), Calendar.getInstance().getTime()), ex.getStatusCode());
    }

}
