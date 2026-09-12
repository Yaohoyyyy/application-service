package com.application.mongo.service;

import com.application.mongo.dto.ProductDto;
import com.application.mongo.entity.Product;
import com.application.mongo.repository.CategoryRepository;
import com.application.mongo.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product getById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Product create(ProductDto dto) {
        validateCategory(dto.categoryId());
        Product product = Product.builder()
                .name(dto.name())
                .categoryId(dto.categoryId())
                .quantity(dto.quantity())
                .build();
        return productRepository.save(product);
    }

    public Product update(String id, ProductDto dto) {
        Product product = getById(id);
        validateCategory(dto.categoryId());
        product.setName(dto.name());
        product.setCategoryId(dto.categoryId());
        product.setQuantity(dto.quantity());
        return productRepository.save(product);
    }

    public void delete(String id) {
        Product product = getById(id);
        productRepository.delete(product);
    }

    private void validateCategory(String categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
    }
}