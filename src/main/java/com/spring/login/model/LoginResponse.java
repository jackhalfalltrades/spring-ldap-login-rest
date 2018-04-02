package com.spring.login.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class LoginResponse {

    private static final long serialVersionUID = 6158358640295856403L;
    private String employeeNumber;
    private String email_id;
    private String firstName;
    private String lastName;
    private String displayName;
    private String userId;
    private String employeeStatus;
    private String enterpriseRole;

}
