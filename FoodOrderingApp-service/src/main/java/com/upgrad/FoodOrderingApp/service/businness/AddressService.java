package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.plaf.nimbus.State;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressDao addressDao;
    @Autowired
    private PasswordCryptographyProvider cryptoProvider;
    @Autowired
    private UtilityService utilityService;


    public void validateAddressRequest(String buildName, String locality, String city, String pincode , String stateID ) throws SaveAddressException {
        if(utilityService.isStringEmptyOrNull(buildName) ||
                utilityService.isStringEmptyOrNull(locality) ||
                utilityService.isStringEmptyOrNull(city) ||
                utilityService.isStringEmptyOrNull(pincode) ||
                utilityService.isStringEmptyOrNull(stateID)) {
            throw new SaveAddressException("SAR-001","No field can be empty");
        }
    }

//    public StateEntity getStateByUUID(String uuid) throws AddressNotFoundException {
//        StateEntity state = addressDao.getStateByUuid(uuid);
//        if (state == null) {
//            throw new AddressNotFoundException("ANF-002", "No state by this id");
//        } else {
//            return state;
//        }
//    }

    public StateEntity getStateByUUID(String uuid) throws AddressNotFoundException {
        StateEntity state = addressDao.getStateByUuid(uuid);
        if (state == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }
        return state;
    }

    @Transactional
    public AddressEntity saveAddress(AddressEntity addressEntity, StateEntity state) throws AddressNotFoundException, SaveAddressException {
        if (state == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }
        //Check if pincode is valid
        if(!utilityService.isPincodeValid(addressEntity.getPincode())){
            throw new SaveAddressException("SAR-002","Invalid pincode");
        }
        addressEntity.setStateId(state);
        return addressDao.saveAddress(addressEntity);
    }
    @Transactional
    public List<AddressEntity> getAllAddress(CustomerEntity customerEntity){
        return addressDao.getAllAddresses();
    }

}
