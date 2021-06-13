package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class ItemService {

    @Autowired
    private ItemDao itemDao;
    @Autowired
    private RestaurantItemDao restaurantItemDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private RestaurantDao restaurantDao;
    @Autowired
    private CategoryItemDao categoryItemDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemDao orderItemDao;

    public List<ItemEntity> getItemsByCategoryAndRestaurant(String restaurantUuid, String categoryUUID){
        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUUID(restaurantUuid);
        CategoryEntity categoryEntity = categoryDao.getCategoryByUuid(categoryUUID);

        List<RestaurantItemEntity> restaurantItemEntities = restaurantItemDao.getItemsByRestaurant(restaurantEntity);
        List<CategoryItemEntity> categoryItemEntities = categoryItemDao.getItemsByCategory(categoryEntity);
        List<ItemEntity> itemEntities = new LinkedList<>();

        for (RestaurantItemEntity restaurantItem:restaurantItemEntities ) {
            for (CategoryItemEntity categoryItem: categoryItemEntities ) {
                if(restaurantItem.getItemId().equals(categoryItem.getItemId())){
                    itemEntities.add(restaurantItem.getItemId());
                }
            }
        }
        return itemEntities;
    }

    public List<ItemEntity> getItemsByPopularity(RestaurantEntity restaurantEntity) {
        return itemDao.getOrdersByRestaurant(restaurantEntity);
    }

}
