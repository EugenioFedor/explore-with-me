package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.service.CategoryService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Adding new category: {}", newCategoryDto.getName());
        Category category = categoryMapper.toEntity(newCategoryDto);
        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Category with name " + newCategoryDto.getName() + " already exists");
        }
        return categoryMapper.toDto(category);
    }

    @Override
    public void deleteCategory(Long catId) {
        log.info("Deleting category with id: {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        boolean hasEvents = eventRepository.findAll().stream()
                .anyMatch(event -> event.getCategory().getId().equals(catId));
        if (hasEvents) {
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Updating category with id: {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        category.setName(categoryDto.getName());
        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Category with name " + categoryDto.getName() + " already exists");
        }
        return categoryMapper.toDto(category);
    }
}
