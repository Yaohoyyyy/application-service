package com.application.mongo.service;

import com.application.mongo.dto.CategoryDto;
import com.application.mongo.entity.Category;
import com.application.mongo.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<Category> getAll() {
        return repository.findAll();
    }

    public Category getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public Category create(CategoryDto dto) {
        Category category = Category.builder()
                .name(dto.name())
                .build();
        return repository.save(category);
    }

    public Category update(String id, CategoryDto dto) {
        Category category = getById(id);
        category.setName(dto.name());
        return repository.save(category);
    }

    public void delete(String id) {
        Category category = getById(id);
        repository.delete(category);
    }
}