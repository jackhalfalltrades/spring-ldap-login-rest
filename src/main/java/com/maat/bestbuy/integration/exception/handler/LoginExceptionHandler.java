package com.maat.bestbuy.integration.exception.handler;

import com.maat.bestbuy.integration.model.ErrorInfo;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;

@ControllerAdvice
public class LoginExceptionHandler extends DefaultHandlerExceptionResolver implements MessageSourceAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginExceptionHandler.class);

    private static final String HYSTRIX_TIMEOUT_EXCEPTION = "timeout.exception";

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(HttpClientErrorException.class)
    private ResponseEntity<ErrorInfo> handleHttpClientException(HttpStatusCodeException ex,
                                                                HttpServletRequest request, HttpServletResponse response, Object handler) {
        return new ResponseEntity<>(new ErrorInfo("", "", ex.getStatusCode(), ex.getMessage(), Calendar.getInstance().getTime()), ex.getStatusCode());
    }

    @ExceptionHandler(HystrixTimeoutException.class)
    @ResponseStatus(value = HttpStatus.REQUEST_TIMEOUT)
    @ResponseBody
    public ErrorInfo circuitException(HttpServletRequest req, HystrixTimeoutException ex) {
        LOGGER.error(StringEscapeUtils.escapeJava("[Hystrix Exception] " + ex.getMessage()), ex);
        String errorMessage = messageSource.getMessage(HYSTRIX_TIMEOUT_EXCEPTION, new Object[]{ex.getMessage()}, LocaleContextHolder.getLocale());
        String errorURL = req.getRequestURL().toString();
        return new ErrorInfo(HttpStatus.REQUEST_TIMEOUT.toString(), errorURL, null, ex.getMessage(), Calendar.getInstance().getTime());
    }

    public void setMessageSource(@SuppressWarnings("NullableProblems") MessageSource messageSource) {
        this.messageSource = messageSource;
    }

}
