package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@Controller
@RequestMapping("/")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    //Implementation of signup - "/customer/signup" API endpoint
    @CrossOrigin
    @RequestMapping(method = POST, path = "/customer/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstname(signupCustomerRequest.getFirstName());
        customerEntity.setLastname(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setPassword(signupCustomerRequest.getPassword());

        final CustomerEntity createdCustomer = customerService.signUpNewCustomer(customerEntity);
        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse();
        signupCustomerResponse.id(createdCustomer.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);
    }

    //Implementation of login- "/customer/login" API
    @CrossOrigin
    @RequestMapping(method = POST, path = "/customer/login", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {
        CustomerAuthEntity customerAuthEntity = customerService.signin(authorization);
        CustomerEntity customerEntity = customerAuthEntity.getCustomer();
        LoginResponse loginResponse = new LoginResponse().id(customerEntity.getUuid()).message("LOGGED IN SUCCESSFULLY");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("access_token", customerAuthEntity.getAccessToken());
        return new ResponseEntity<LoginResponse>(loginResponse, httpHeaders, HttpStatus.OK);
    }

    //Implementation of "/customer/logout" API
    @CrossOrigin
    @RequestMapping(method = POST, path = "/customer/logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        String uuid = customerService.getCustomerUUID(authorization);
        LogoutResponse logoutResponse = new LogoutResponse();
        logoutResponse.setId(uuid);
        logoutResponse.setMessage("LOGGED OUT SUCCESSFULLY");
        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }

    //Implementation of "/customer" API
    @CrossOrigin
    @RequestMapping(method = PUT, path = "/customer", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> updateName(@RequestHeader("authorization") final String authorization, final UpdateCustomerRequest updateCustomerRequest) throws AuthorizationFailedException, UpdateCustomerException {
        CustomerAuthEntity customerAuth = customerService.validateUserAuthentication(authorization);
        //Check if the firstname entered is empty
        if (updateCustomerRequest.getFirstName() == null || updateCustomerRequest.getFirstName().isEmpty()) {
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }
        CustomerEntity customer = customerAuth.getCustomer();
        customer.setFirstname(updateCustomerRequest.getFirstName());
        customer.setLastname(updateCustomerRequest.getLastName());
        String uuid = customerService.updateCustomer(customer);

        UpdateCustomerResponse response = new UpdateCustomerResponse();
        response.setId(uuid);
        response.setFirstName(customer.getFirstname());
        response.setLastName(customer.getLastname());
        response.setStatus("CUSTOMER DETAILS UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdateCustomerResponse>(response, HttpStatus.OK);
    }

    //Implementation of "/customer/password" API
    @CrossOrigin
    @RequestMapping(method = PUT, path = "/customer/password", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> updatePassword(@RequestHeader("authorization") final String authorization, final UpdatePasswordRequest updatePasswordRequest) throws AuthorizationFailedException, UpdateCustomerException {
        CustomerAuthEntity customerAuth = customerService.validateUserAuthentication(authorization);
        String oldPassword = updatePasswordRequest.getOldPassword();
        String newPassword = updatePasswordRequest.getNewPassword();
        CustomerEntity customer = customerAuth.getCustomer();
        String uuid = customerService.updateCustomerPassword(customer, oldPassword, newPassword);

        UpdatePasswordResponse response = new UpdatePasswordResponse();
        response.setId(uuid);
        response.setStatus("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdatePasswordResponse>(response, HttpStatus.OK);
    }

}
