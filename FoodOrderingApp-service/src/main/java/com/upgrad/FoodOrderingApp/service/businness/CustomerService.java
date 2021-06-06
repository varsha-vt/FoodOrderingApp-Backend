package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import com.upgrad.FoodOrderingApp.service.util.FoodOrderingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class CustomerService {

    @Autowired
    private UtilityService utilityService;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private PasswordCryptographyProvider cryptoProvider;

    @Transactional
    public CustomerEntity signUpNewCustomer(CustomerEntity customerEntity) throws SignUpRestrictedException {
        // 'SGR-001' To validate if contact number is already registered
        if (customerDao.getUserByContactNumber(customerEntity.getContactNumber()) != null) {
            throw new SignUpRestrictedException("SGR-001", "This contact number is already registered! Try other contact number.");
        } // 'SGR-005' To validate of there are any empty fields except for lastname
        else if (
                utilityService.isStringEmptyOrNull(customerEntity.getFirstname()) ||
                        utilityService.isStringEmptyOrNull(customerEntity.getEmail()) ||
                        utilityService.isStringEmptyOrNull(customerEntity.getContactNumber()) ||
                        utilityService.isStringEmptyOrNull(customerEntity.getPassword())
                ) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        } /* 'SGR-002' To validate emailID. This does not allow emails that have . or _ (underscore) in the address as the project statement asks to validate
              in the format of xxx@xx.xx where x is number or letter */ else if (!customerEntity.getEmail().matches("^(([A-Za-z0-9]*))(@)(([A-Za-z0-9]*))(?<!\\.)\\.(?!\\.)(([A-Za-z0-9]*))")) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        } // 'SGR-003' Validates contact no for length = 10 and must contain only digits
        else if (!utilityService.isPhoneNumberValid(customerEntity.getContactNumber())) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        } // 'SGR-004' Validates if the password criteria is met
        else if (!utilityService.isPasswordValid(customerEntity.getPassword())) {
            throw new SignUpRestrictedException("SGR-004", "Weak password!");
        } else {

            String[] encryptedText = cryptoProvider.encrypt(customerEntity.getPassword());
            customerEntity.setSalt(encryptedText[0]);
            customerEntity.setPassword(encryptedText[1]);
            return customerDao.createNewCustomer(customerEntity);
        }
    }

    @Transactional
    public CustomerAuthEntity signin(String authorization) throws AuthenticationFailedException {
        String username;
        String password;
        //'ATH-003' The below try-catch block is used to see if the authrorization is in the correct format
        try {
            byte[] decode = Base64.getDecoder().decode(authorization.split(FoodOrderingUtil.BASIC_TOKEN)[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(FoodOrderingUtil.COLON);
            username = decodedArray[0];
            password = decodedArray[1];
        } catch (Exception e) {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }

        CustomerEntity customer = customerDao.getUserByContactNumber(username);

        //ATH-001 The below checks if the entered user exists in the data base
        if (customer == null) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }

        //The entered password is encrypted with the same salt of the user and then compared to see if the encrypted password matches that present in the DB
        final String encryptedPassword = cryptoProvider.encrypt(password, customer.getSalt());
        if (encryptedPassword.equals(customer.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setUuid(UUID.randomUUID().toString());
            customerAuthEntity.setCustomer(customer);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customer.getUuid(), now, expiresAt));
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setLogoutAt(null);
            customerAuthEntity.setExpiresAt(expiresAt);
            return customerDao.createAuthToken(customerAuthEntity);

        }
        //'ATH-002' An error is thrown is credentials do not match
        else {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }
    }

    @Transactional
    public String getCustomerUUID(String authtoken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = validateUserAuthentication(authtoken);
        customerAuthEntity.setLogoutAt(ZonedDateTime.now());
        customerDao.updateCustomerAuthEntity(customerAuthEntity);
        CustomerEntity customer = customerAuthEntity.getCustomer();
        return customer.getUuid();
    }

    //Common method that will be used by all endpoints to validate the accessToken
    public CustomerAuthEntity validateUserAuthentication(String authtoken) throws AuthorizationFailedException {
        String[] bearerToken = authtoken.split(FoodOrderingUtil.BEARER_TOKEN);
        if (bearerToken != null && bearerToken.length > 1) {
            authtoken = bearerToken[1];
        }
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(authtoken);
        if (customerAuthEntity == null) { // "ATHR-001" to check if authtoken is valid or present in the DB
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }

        if (customerAuthEntity.getLogoutAt() != null) { // "ATHR-002" To check if the authtoken is no more valid since user has logged out
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }
        //"ATHR-003" To check if token has expired, if yes then thelogged out time is updated to current time
        boolean isTokenExpired = utilityService.hasTokenExpired(customerAuthEntity.getExpiresAt().toString());
        if (isTokenExpired) {
            customerAuthEntity.setLogoutAt(ZonedDateTime.now());
            customerDao.updateCustomerAuthEntity(customerAuthEntity);
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        return customerAuthEntity;
    }

    //Update the customer name
    @Transactional
    public String updateCustomer(CustomerEntity customer) {
        customerDao.updateCustomer(customer);
        return customer.getUuid();
    }

    @Transactional
    public String updateCustomerPassword(CustomerEntity customer, String oldPassword, String newPassword) throws UpdateCustomerException {
        //Check if old or new password fields are empty
        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            throw new UpdateCustomerException("UCR-003", "No field should be empty");
        }
        //Check if new Password is weak
        if (!utilityService.isPasswordValid(newPassword)) {
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }
        //Check if old password is correct
        final String encryptedPassword = cryptoProvider.encrypt(oldPassword, customer.getSalt());
        if (!encryptedPassword.equals(customer.getPassword())) {
            throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        }
        String[] encryptedText = cryptoProvider.encrypt(newPassword);
        customer.setSalt(encryptedText[0]);
        customer.setPassword(encryptedText[1]);
        customerDao.updateCustomerPassword(customer);
        return customer.getUuid();
    }

}
