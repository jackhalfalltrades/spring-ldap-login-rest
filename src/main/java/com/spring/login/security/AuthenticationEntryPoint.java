package com.spring.login.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.login.model.ErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("maatAuthenticationEntryPoint")
public class AuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationEntryPoint.class);

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException)
            throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ErrorInfo error = new ErrorInfo();
        error.setMessage(authException.getMessage());
        error.setStatus(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
        error.setUrl(request.getRequestURL().toString());

        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        MediaType jsonMimeType = MediaType.APPLICATION_JSON;
        if (jsonConverter.canWrite(error.getClass(), jsonMimeType)) {
            try {
                jsonConverter.write(error, jsonMimeType, new ServletServerHttpResponse(response));
            } catch (Exception e) {
                String eMessage = e.getMessage();
                LOGGER.error("AUTHENTICATION ERROR: " + eMessage, e);
                response.getOutputStream().println(getResponseContent(eMessage));
            }
        }
    }


    private String getResponseContent(String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(new Content(message));
        } catch (Exception e) {
            LOGGER.error("getResponseContent threw error: " + e.getMessage(), e);
            return null;
        }
    }

    @SuppressWarnings("all")
    class Content {

        private static final String ERROR = "Authorized Denied";

        private String message;

        public Content(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public String getError() {
            return ERROR;
        }
    }
}
