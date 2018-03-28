package com.maat.bestbuy.integration.dao;

import com.maat.bestbuy.integration.exception.AuthorizationException;
import com.maat.bestbuy.integration.exception.BadRequestException;
import com.maat.bestbuy.integration.model.LoginResponse;
import com.maat.bestbuy.integration.model.Payload;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.stereotype.Repository;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

@Repository("loginDao")
public class LoginDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDao.class);
    private static final String AUTH_FAILED = "authorization.failed";
    private List<String> groupSearchBase;
    private String groupRoleAttribute;
    private MessageFormat userDnFormat;
    private String groupSearchFilterPrefix = "uniqueMember=";
    private SpringSecurityLdapTemplate ldapTemplate;
    private AuthenticationManager authenticationManager;

    public LoginDao(SpringSecurityLdapTemplate ldapTemplate, AuthenticationManager authenticationManager) {
        this.ldapTemplate = ldapTemplate;
        this.authenticationManager = authenticationManager;
    }

    public LoginResponse login (Payload payload) {
        LoginResponse loginResponse = null;
        LOGGER.info("loginDao {} -> login(): Initiated");
        Authentication authentication = getUserAuthentication(payload);
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                loginResponse = getUserDetails(payload.getUserId());
                loginResponse.setUserId(StringEscapeUtils.escapeJava(payload.getUserId()));
                if (isAuthorized(loginResponse)) {
                    return loginResponse;
                } else {
                    throw new AuthorizationException(AUTH_FAILED, new Object[]{});
                }
            } catch (BadRequestException | AuthenticationException e) {
                LOGGER.error("Error getting user details from ldap: " + e.getMessage(), e);
                throw new AuthorizationException(AUTH_FAILED, new Object[]{});
            }
        }
        return loginResponse;
    }

    private Authentication getUserAuthentication(Payload userDetailsPayload) {
        if (authenticationManager == null) {
            LOGGER.error("authenticationManager is null");
            throw new AuthorizationException(AUTH_FAILED, new Object[]{});
        }
        return getAuthentication(userDetailsPayload.getUserId(), userDetailsPayload.getPassword());
    }

    private Authentication getAuthentication(String userId, String password) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userId, password));
        } catch (Exception e) {
            LOGGER.error("LDAP authentication error for user " + userId + " : " + e.getMessage(), e);
            throw new AuthorizationException(AUTH_FAILED, new Object[]{});
        }
        return authentication;
    }

    @SuppressWarnings("unchecked")
    private LoginResponse getUserDetails(String userId) {
        try {
            UserDetailsAttributesMapper mapper = new UserDetailsAttributesMapper();
            ldapTemplate.search("ou=people", "(uid=" + userId + ")", mapper);
            return mapper.getUserDetails(userId);
        } catch (NamingException e) {
            LOGGER.error("Issue getting LDAP Attributes for user " + userId + ": " + e.getMessage(), e);
            throw new BadRequestException("ldap.get.userdetails", new Object[]{});
        }
    }

    private boolean isAuthorized(LoginResponse loginResponse) {
        boolean isAuthorized = false;
        String userId = loginResponse.getUserId();
        LOGGER.debug("Authorized userDetails: Admin Status " + loginResponse.getEmployeeStatus());
        if ((authorizeUserInLdap(userId)) && "ACTIVE".equalsIgnoreCase(loginResponse.getEmployeeStatus())) {
            LOGGER.debug("Authorized userDetails " + userId);
            isAuthorized = true;
        }
        return isAuthorized;
    }

    private boolean authorizeUserInLdap(String userId) {
        boolean isAuthorized = false;
        String group;
        String userDn = userDnFormat.format(new String[]{userId});
        String groupSearchFilter = groupSearchFilterPrefix + userDn;
        for (String s : getGroupSearchBase()) {
            group = s;
            Set<String> userRoles = ldapTemplate.searchForSingleAttributeValues(group, groupSearchFilter,
                    new String[]{userDn, userId}, groupRoleAttribute);
            if (!userRoles.isEmpty()) {
                LOGGER.debug("userDetails " + userId + "Is part of the group " + group + " Authorizing Access..");
                isAuthorized = true;
                break;
            }
        }
        return isAuthorized;
    }

    private List<String> getGroupSearchBase() {
        return groupSearchBase;
    }
    public void setGroupSearchBase(List<String> groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }
    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }
    public void setGroupRoleAttribute(String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
    }
    public MessageFormat getUserDnFormat() {
        return userDnFormat;
    }
    public void setUserDnFormat(MessageFormat userDnFormat) {
        this.userDnFormat = userDnFormat;
    }

    @SuppressWarnings("rawtypes")
    private static class UserDetailsAttributesMapper implements AttributesMapper {
        private static final String EMPLOYEE_NUMBER = "uid";
        private static final String EMAIL_ID = "mail";
        private static final String FIRST_NAME = "givenName";
        private static final String LAST_NAME = "sn";
        private static final String EMPLOYEE_STATUS = "bbEmploymentStatus";
        private static final String ENTERPRISE_ROLE = "bbyEnterpriseRole";
        private static final String[] ELEMENTS = {EMPLOYEE_NUMBER, FIRST_NAME, LAST_NAME, EMPLOYEE_STATUS, ENTERPRISE_ROLE};
        private Attributes attributes;

        @Override
        public Attributes mapFromAttributes(Attributes attributes) {

            this.attributes = attributes;
            return attributes;
        }

        private LoginResponse getUserDetails(String userId) throws NamingException {
            LoginResponse loginResponse = new LoginResponse();
            validate(userId);
            loginResponse.setEmail_id(attributes.get(EMAIL_ID).get().toString());
            loginResponse.setEmployeeNumber(attributes.get(EMPLOYEE_NUMBER).get().toString());
            loginResponse.setFirstName(attributes.get(FIRST_NAME).get().toString());
            loginResponse.setLastName(attributes.get(LAST_NAME).get().toString());
            loginResponse.setEnterpriseRole(attributes.get(ENTERPRISE_ROLE).get().toString());
            loginResponse.setEmployeeNumber(attributes.get(EMPLOYEE_NUMBER).get().toString());
            loginResponse.setEmployeeStatus(attributes.get(EMPLOYEE_STATUS).get().toString());
            loginResponse.setDisplayName(loginResponse.getFirstName() + " " + loginResponse.getLastName());
            loginResponse.setUserId(userId);
            return loginResponse;
        }

        private void validate(String userId) throws NamingException {
            if (attributes != null) {
                for (String element : ELEMENTS) {
                    if (!(attributes.get(element) != null && attributes.get(element).get() != null
                            && attributes.get(element).get() != null)) {
                        LOGGER.error(StringEscapeUtils
                                .escapeJava("Missing attribute " + element + " for user " + userId + " login denied"));
                        throw new BadRequestException("login.missing.attributes", new Object[]{element});
                    }
                }
            }
        }
    }
}
