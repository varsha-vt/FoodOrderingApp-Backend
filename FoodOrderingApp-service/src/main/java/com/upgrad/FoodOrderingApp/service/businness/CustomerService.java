package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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

}
