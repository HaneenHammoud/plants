package com.proposal.Nature.Heaven.controller;

import com.proposal.Nature.Heaven.model.Category;
import com.proposal.Nature.Heaven.model.Plant;
import com.proposal.Nature.Heaven.service.CategoryService;
import com.proposal.Nature.Heaven.service.PlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final PlantService plantService;

    @Autowired
    public CategoryController(CategoryService categoryService,PlantService plantService ) {
        this.categoryService = categoryService;
        this.plantService=plantService;
    }

    // Get all categories
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @GetMapping("/{categoryId}/plants")
    public String viewPlantsByCategory(@PathVariable("categoryId") Long categoryId, Model model) {
        List<Plant> plantsList = plantService.getPlantsByCategory(categoryId);
        if (plantsList == null || plantsList.isEmpty()) {
            System.out.println("No plants found for category " + categoryId);
        } else {
            System.out.println("Plants found: " + plantsList.size());
        }
        model.addAttribute("plants", plantsList);
        return "plantsByCategory";
    }



    @GetMapping("/")
    public String getAllCategories1(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "category"; // Thymeleaf template to display categories
    }


    // Create a new category
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category savedCategory = categoryService.saveCategory(category);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    // Update a category
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Optional<Category> existingCategory = categoryService.getCategoryById(id);
        if (existingCategory.isPresent()) {
            Category updatedCategory = categoryService.saveCategory(category);
            return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete a category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Optional<Category> existingCategory = categoryService.getCategoryById(id);
        if (existingCategory.isPresent()) {
            categoryService.deleteCategory(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
