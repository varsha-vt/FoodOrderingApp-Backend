package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.CategoriesListResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryDetailsResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryListResponse;
import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    //Implementation of Get All Categories - “/category” API
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET,path = "/category",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoriesListResponse> getCategories() {
        List<CategoryEntity> allCategories = categoryService.getAllCategoriesOrderedByName();
        List<CategoryListResponse> categoryListResponses = null;
        if (allCategories.size() > 0) {
            categoryListResponses = new ArrayList<>();

            for (CategoryEntity categoryEntity : allCategories) {
                CategoryListResponse categoryListResponse = new CategoryListResponse();
                categoryListResponse.setId(UUID.fromString(categoryEntity.getUuid()));
                categoryListResponse.setCategoryName(categoryEntity.getCategoryName());
                categoryListResponses.add(categoryListResponse);
            }
        }
        CategoriesListResponse categoriesListResponse = new CategoriesListResponse();
        categoriesListResponse.setCategories(categoryListResponses);
        return new ResponseEntity<>(categoriesListResponse, HttpStatus.OK);
    }

    //IMplementation of Get Category by Id - “/category/{category_id}” API
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/category/{category_id}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoryDetailsResponse> getCategoryDetail(
            @PathVariable("category_id") final String categoryUuid) throws CategoryNotFoundException {

        CategoryEntity categoryEntity = categoryService.getCategoryById(categoryUuid);

        CategoryDetailsResponse categoryDetailsResponse = new CategoryDetailsResponse();
        categoryDetailsResponse.setCategoryName(categoryEntity.getCategoryName());
        categoryDetailsResponse.setId(UUID.fromString(categoryEntity.getUuid()));
        List<ItemEntity> itemEntities = categoryEntity.getItems();
        List<ItemList> itemLists = new ArrayList<>();
        for (ItemEntity itemEntity : itemEntities) {
            ItemList itemList = new ItemList();
            itemList.setId(UUID.fromString(itemEntity.getUuid()));
            itemList.setItemName(itemEntity.getItemName());
            itemList.setPrice(itemEntity.getPrice());
            if (itemEntity.getType().equals("0")) {
                itemList.setItemType(ItemList.ItemTypeEnum.valueOf("VEG"));
            } else {
                itemList.setItemType(ItemList.ItemTypeEnum.valueOf("NON_VEG"));
            }
            itemLists.add(itemList);
        }
        categoryDetailsResponse.setItemList(itemLists);
        return new ResponseEntity<CategoryDetailsResponse>(categoryDetailsResponse, HttpStatus.OK);
    }
}
