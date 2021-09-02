package com.mamunsproject.food_delevery.EventBus;

import com.mamunsproject.food_delevery.Model.CategoryModel;

public class CategorClick {
    private boolean success;
    private CategoryModel categoryModel;

    public CategorClick(boolean success, CategoryModel categoryModel) {
        this.success = success;
        this.categoryModel = categoryModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public CategoryModel getCategoryModel() {
        return categoryModel;
    }

    public void setCategoryModel(CategoryModel categoryModel) {
        this.categoryModel = categoryModel;
    }
}

