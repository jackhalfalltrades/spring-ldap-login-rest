package com.maat.bestbuy.integration.configuration;

import com.maat.bestbuy.integration.dao.LoginDao;
import com.maat.bestbuy.integration.security.MaatAuthenticationEntryPoint;
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

@ConfigurationProperties(prefix="dao")
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
    public MaatAuthenticationEntryPoint getMaatAuthenticationEntryPoint() {
        return new MaatAuthenticationEntryPoint();
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