package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.CategoriesListResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryListResponse;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

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
}
