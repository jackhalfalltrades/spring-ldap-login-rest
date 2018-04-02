package com.spring.login.service;

import com.spring.login.dao.LoginDao;
import com.spring.login.model.LoginResponse;
import com.spring.login.model.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

@Service("loginService")
public class LoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);
    private LoginDao loginDao;

    @Autowired
    public LoginService(LoginDao loginDao) {
        this.loginDao = loginDao;
    }

    public Observable<LoginResponse> login(Payload payload) {
        return Observable.just(loginDao.login(payload));
    }
}
