package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
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
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

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
        addressService.saveCustomerAddress(createdAddress,customerEntity.getId());
        SaveAddressResponse response =  new SaveAddressResponse();
        response.setId((createdAddress.getUuid()));
        response.setStatus("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SaveAddressResponse>(response,HttpStatus.CREATED);

    }

    @RequestMapping(method = GET, path = "/address/customer" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<AddressListResponse> getAllAddress(@RequestHeader("authorization") String authorization) throws AuthorizationFailedException {
        String accessToken = utilityService.splitAuthorization(authorization);
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        List<AddressEntity> addressList = addressService.getAllAddress(customerEntity);
        AddressListResponse addressListResponse = new AddressListResponse();
        for (AddressEntity address: addressList ) {
            AddressListState state = new AddressListState();
            state.setStateName(address.getStateId().getStateName());
            state.setId(UUID.fromString(address.getStateId().getUuid()));

            AddressList item = new AddressList();
            item.setId(UUID.fromString(address.getUuid()));
            item.setFlatBuildingName(address.getFlatBuildingNumber());
            item.setLocality(address.getLocality());
            item.setCity(address.getCity());
            item.setPincode(address.getPincode());
            item.setState(state);
            addressListResponse.addAddressesItem(item);
        }
        return new ResponseEntity(addressListResponse, HttpStatus.OK);
    }


    @RequestMapping(method = DELETE, path = "/address/customer/{address_id}" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<SaveAddressResponse> deleteAddress(@RequestHeader("authorization") String authorization, @PathVariable("address_id") String addressId) throws AuthorizationFailedException, AddressNotFoundException {
        String accessToken = utilityService.splitAuthorization(authorization);
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        AddressEntity addressEntity = addressService.getAddressByUUID(addressId, customerEntity);

        AddressEntity deletedAddress = addressService.deleteAddress(addressEntity);

        SaveAddressResponse response = new SaveAddressResponse();
        response.setId(deletedAddress.getUuid());
        response.setStatus("ADDRESS DELETED SUCCESSFULLY");
        return new ResponseEntity<SaveAddressResponse>(response,HttpStatus.OK);
    }

    @RequestMapping(path = "/states",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<StatesListResponse> getAllStates() {
        List<StateEntity> listOfStates = addressService.getAllStates();
        StatesListResponse apiResponse = new StatesListResponse();
        if(listOfStates == null){
            apiResponse.setStates(null);
            return new ResponseEntity(apiResponse, HttpStatus.NOT_FOUND);
        }
        else {
            List<StatesList> list = new ArrayList<StatesList>();
            for (StateEntity state : listOfStates) {
                StatesList listItem = new StatesList();
                listItem.setId(UUID.fromString(state.getUuid()));
                listItem.setStateName(state.getStateName());
                list.add(listItem);
            }
            apiResponse.setStates(list);
            return new ResponseEntity(apiResponse, HttpStatus.OK);
        }
    }
}
