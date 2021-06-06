package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
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
        try{
            byte[] decode = Base64.getDecoder().decode(authorization.split(FoodOrderingUtil.BASIC_TOKEN)[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(FoodOrderingUtil.COLON);
            username = decodedArray[0];
            password = decodedArray[1];
            } catch(Exception e){
            throw new AuthenticationFailedException("ATH-003","Incorrect format of decoded customer name and password");
        }

        CustomerEntity customer = customerDao.getUserByContactNumber(username);

        //ATH-001
        if(customer == null){
            throw new AuthenticationFailedException("ATH-001","This contact number has not been registered!");
        }

        final String encryptedPassword = cryptoProvider.encrypt(password, customer.getSalt());
        if (encryptedPassword.equals(customer.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuthEntity =  new CustomerAuthEntity();
            customerAuthEntity.setUuid(UUID.randomUUID().toString());
            customerAuthEntity.setCustomer(customer);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customer.getUuid(), now, expiresAt));
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setLogoutAt(expiresAt);
            customerAuthEntity.setExpiresAt(expiresAt);
            return customerDao.createAuthToken(customerAuthEntity);

        }
        else
        {
            throw new AuthenticationFailedException("ATH-002","Invalid Credentials");
        }



    }

}
