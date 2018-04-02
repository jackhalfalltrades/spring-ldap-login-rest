package com.spring.login.configuration;

import com.spring.login.dao.LoginDao;
import com.spring.login.security.AuthenticationEntryPoint;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import java.text.MessageFormat;
import java.util.List;

@ConfigurationProperties(prefix = "dao")
@EnableConfigurationProperties
@Configuration("appConfiguration")
@Data
public class AppConfiguration {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private SpringSecurityLdapTemplate ldapTemplate;

    private String groupRoleAttribute;
    private MessageFormat userDnFormat;
    private List<String> groupSearchBase;

    @Bean("maatAuthenticationEntryPoint")
    public AuthenticationEntryPoint getMaatAuthenticationEntryPoint() {
        return new AuthenticationEntryPoint();
    }

    @Bean("loginDao")
    public LoginDao getLoginDao() {
        LoginDao loginDao = new LoginDao(ldapTemplate, authenticationManager);
        loginDao.setGroupSearchBase(groupSearchBase);
        loginDao.setGroupRoleAttribute(groupRoleAttribute);
        loginDao.setUserDnFormat(userDnFormat);
        return loginDao;
    }
}