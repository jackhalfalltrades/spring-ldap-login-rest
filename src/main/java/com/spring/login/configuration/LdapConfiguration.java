package com.spring.login.configuration;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

@EnableEncryptableProperties
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "ldap")
@Configuration("ldapConfiguration")
@Data
public class LdapConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LdapConfiguration.class);

    private String url;
    private String userDn;
    private String password;
    private String[] userDnPatterns;


    public LdapConfiguration() {
        logger.info(" LDAP {}: Loading Ldap configuration");
    }

    @Bean(name = "contextSource")
    public DefaultSpringSecurityContextSource defaultSpringSecurityContextSource() {
        DefaultSpringSecurityContextSource defaultSpringSecurityContextSource = new DefaultSpringSecurityContextSource(url);
        defaultSpringSecurityContextSource.setUserDn(userDn);
        defaultSpringSecurityContextSource.setPassword(password);
        return defaultSpringSecurityContextSource;
    }

    @Bean(name = "ldapTemplate")
    public SpringSecurityLdapTemplate getSpringSecurityLdapTemplate(@Qualifier("contextSource") DefaultSpringSecurityContextSource defaultSpringSecurityContextSource) {
        SpringSecurityLdapTemplate springSecurityLdapTemplate = new SpringSecurityLdapTemplate(defaultSpringSecurityContextSource);
        return springSecurityLdapTemplate;
    }

    @Bean(name = "ldapBindAuthenticator")
    public BindAuthenticator getBindAuthenticator(@Qualifier("contextSource") DefaultSpringSecurityContextSource defaultSpringSecurityContextSource) {
        BindAuthenticator bindAuthenticator = new BindAuthenticator(defaultSpringSecurityContextSource);
        bindAuthenticator.setUserDnPatterns(userDnPatterns);
        return bindAuthenticator;
    }

    @Bean(name = "ldapAuthProvider")
    public LdapAuthenticationProvider getLdapAuthenticationProvider(@Qualifier("ldapBindAuthenticator") BindAuthenticator bindAuthenticator) {
        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator);
        return ldapAuthenticationProvider;
    }

}
