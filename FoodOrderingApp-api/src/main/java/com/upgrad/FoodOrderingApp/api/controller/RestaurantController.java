package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.RestaurantDetailsResponseAddress;
import com.upgrad.FoodOrderingApp.api.model.RestaurantDetailsResponseAddressState;
import com.upgrad.FoodOrderingApp.api.model.RestaurantList;
import com.upgrad.FoodOrderingApp.api.model.RestaurantListResponse;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

@Controller
@RequestMapping("/")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ItemService itemService;

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/restaurant", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getAllRestaurants() {
        //Get restaurants by rating
        List<RestaurantEntity> restaurantEntities = restaurantService.restaurantsByRating();
        //Get the response list
        List<RestaurantList> restaurantLists = restaurantList(restaurantEntities);
        RestaurantListResponse restaurantListResponse = new RestaurantListResponse().restaurants(restaurantLists);
        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);

    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/restaurant/name/{restaurant_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantByName(@PathVariable(value = "restaurant_name") final String restaurantName) throws RestaurantNotFoundException {

        List<RestaurantEntity> restaurantEntities = restaurantService.restaurantsByName(restaurantName);
        //Check made to see if restaurant is empty
        if (!restaurantEntities.isEmpty()) {
            List<RestaurantList> restaurantLists = restaurantList(restaurantEntities);
            RestaurantListResponse restaurantListResponse = new RestaurantListResponse().restaurants(restaurantLists);
            return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);

        } else {
            return new ResponseEntity<RestaurantListResponse>(new RestaurantListResponse(), HttpStatus.OK);
        }
    }


    public List<RestaurantList> restaurantList(List<RestaurantEntity> restaurantEntities){
        List<RestaurantList> restaurantLists = new LinkedList<>();
        for (RestaurantEntity restaurantEntity : restaurantEntities) { //Looping for each restaurant entity in restaurantEntities

            //Calls  getCategoriesByRestaurant to get categories of the corresponding restaurant.
            List<CategoryEntity> categoryEntities = categoryService.getCategoriesByRestaurant(restaurantEntity.getUuid());
            String categories = "";
            //To concat the category names.
            ListIterator<CategoryEntity> listIterator = categoryEntities.listIterator();
            while (listIterator.hasNext()) {
                categories = categories + listIterator.next().getCategoryName();
                if (listIterator.hasNext()) {
                    categories = categories + ", ";
                }
            }

            //Creating the RestaurantDetailsResponseAddressState for the RestaurantDetailsResponseAddress
            RestaurantDetailsResponseAddressState restaurantDetailsResponseAddressState = new RestaurantDetailsResponseAddressState();
            restaurantDetailsResponseAddressState.id(UUID.fromString(restaurantEntity.getAddress().getStateId().getUuid()));
            restaurantDetailsResponseAddressState.stateName(restaurantEntity.getAddress().getStateId().getStateName());

            //Creating the RestaurantDetailsResponseAddress for the RestaurantList
            RestaurantDetailsResponseAddress restaurantDetailsResponseAddress = new RestaurantDetailsResponseAddress();
            restaurantDetailsResponseAddress.id(UUID.fromString(restaurantEntity.getAddress().getUuid()));
            restaurantDetailsResponseAddress.city(restaurantEntity.getAddress().getCity());
            restaurantDetailsResponseAddress.flatBuildingName(restaurantEntity.getAddress().getFlatBuildingNumber());
            restaurantDetailsResponseAddress.locality(restaurantEntity.getAddress().getLocality());
            restaurantDetailsResponseAddress.pincode(restaurantEntity.getAddress().getPincode());
            restaurantDetailsResponseAddress.state(restaurantDetailsResponseAddressState);

            //Creating RestaurantList to add to list of RestaurantList
            RestaurantList restaurantList = new RestaurantList();
            restaurantList.id(UUID.fromString(restaurantEntity.getUuid()));
            restaurantList.restaurantName(restaurantEntity.getRestaurantName());
            restaurantList.averagePrice(restaurantEntity.getAveragePriceForTwo());
            restaurantList.categories(categories);
            restaurantList.customerRating(BigDecimal.valueOf(restaurantEntity.getCustomerRating()));
            restaurantList.numberCustomersRated(restaurantEntity.getNumberOfCustomersRated());
            restaurantList.photoURL(restaurantEntity.getPhotoUrl());
            restaurantList.address(restaurantDetailsResponseAddress);

            //Adding it to the list
            restaurantLists.add(restaurantList);

        }

        return restaurantLists;
    }

}
