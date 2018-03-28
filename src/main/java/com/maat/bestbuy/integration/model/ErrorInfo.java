package com.maat.bestbuy.integration.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorInfo {

    private String status;
    private String url;
    private HttpStatus statusCode;
    private String message;
    private Date timestamp;

}
