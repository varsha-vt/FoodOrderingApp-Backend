package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.SaveAddressRequest;
import com.upgrad.FoodOrderingApp.api.model.SaveAddressResponse;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.UtilityService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/")
public class AddressController {

    @Autowired
    private AddressService addressService;
    @Autowired
    private UtilityService utilityService;
    @Autowired
    private CustomerService customerService;


    @RequestMapping(method = POST, path = "/address", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(@RequestHeader("authorization") final String authorization, @RequestBody  SaveAddressRequest saveAddressRequest) throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {

        String accessToken = utilityService.splitAuthorization(authorization);
        customerService.getCustomer(accessToken);

        //Check if any field is empty
        String buildName = saveAddressRequest.getFlatBuildingName();
        String locality  = saveAddressRequest.getLocality();
        String city=saveAddressRequest.getCity();
        String pincode = saveAddressRequest.getPincode();
        String stateID = saveAddressRequest.getStateUuid();
        addressService.validateAddressRequest(buildName,locality,city,pincode,stateID);
        AddressEntity newAddress = new AddressEntity();
        newAddress.setUuid(UUID.randomUUID().toString());
        newAddress.setFlatBuildingNumber(buildName);
        newAddress.setLocality(locality);
        newAddress.setCity(city);
        newAddress.setPincode(pincode);
        StateEntity state = addressService.getStateByUUID(stateID);
        if (state == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }
        AddressEntity createdAddress = addressService.saveAddress(newAddress, state);
        SaveAddressResponse response =  new SaveAddressResponse();
        response.setId((createdAddress.getUuid()));
        response.setStatus("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SaveAddressResponse>(response,HttpStatus.CREATED);

    }
}
