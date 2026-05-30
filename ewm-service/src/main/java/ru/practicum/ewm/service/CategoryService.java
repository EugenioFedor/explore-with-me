package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;

public interface CategoryService {

    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);
}
