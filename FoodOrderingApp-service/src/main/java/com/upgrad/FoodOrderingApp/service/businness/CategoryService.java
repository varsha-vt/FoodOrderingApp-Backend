package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantCategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantCategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private RestaurantDao restaurantDao;
    @Autowired
    private RestaurantCategoryDao restaurantCategoryDao;


    public List<CategoryEntity> getCategoriesByRestaurant(String restaurantUuid){
        //Get the restaurant entity and all the categories of the restaurant
        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUUID(restaurantUuid);
        List<RestaurantCategoryEntity> restaurantCategoryEntities = restaurantCategoryDao.getCategoriesByRestaurant(restaurantEntity);

        //Create Category list
        List<CategoryEntity> categoryEntities = new LinkedList<>();
        for ( RestaurantCategoryEntity rce : restaurantCategoryEntities) {
            categoryEntities.add(rce.getCategoryId());
        }

        return categoryEntities;

    }
}


