package com.spring.login.web.controller;

import com.spring.login.model.LoginResponse;
import com.spring.login.model.Payload;
import com.spring.login.service.LoginService;
import com.spring.login.utils.Constants;
import com.spring.login.utils.Encryption;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;

@RestController
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    private LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @ApiOperation("Authenticate Administrators")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody
    @CrossOrigin
    DeferredResult<LoginResponse> login(@RequestBody @Valid Payload payload, HttpServletResponse response, HttpServletRequest request) {
        LOGGER.debug("login {} : payload ->", payload);
        LOGGER.info("login {} : User: " + payload.getUserId() + " initiated");
        Observable<LoginResponse> loginResponse = loginService.login(payload);
        DeferredResult<LoginResponse> deferredResult = new DeferredResult<>();
        loginResponse.subscribe(o -> {
            response.setHeader("x-auth-token", generateJWT(getEncrptedUT(o.getEmployeeNumber() + "^" + o.getEnterpriseRole() + "^" + o.getEmployeeStatus() + "^" + o.getEmail_id())));
            response.setHeader("Access-Control-Expose-Headers", "x-auth-token");
            deferredResult.setResult(o);
        }, deferredResult::setErrorResult);
        LOGGER.info("login {} : User: " + payload.getUserId() + " completed");
        return deferredResult;
    }

    private String getEncrptedUT(String ut) {
        return Encryption.encryptValue(ut);
    }

    private String generateJWT(String token) {
        return Jwts.builder()
                .setSubject(token)
                .setExpiration(new Date(System.currentTimeMillis() + 1800000))
                .signWith(SignatureAlgorithm.HS512, Encryption.getProperty(Constants.APP_SEC_PSSWD, null))
                .compact();

    }
}
