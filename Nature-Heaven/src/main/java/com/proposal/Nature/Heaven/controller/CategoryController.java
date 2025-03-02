package com.proposal.Nature.Heaven.controller;

import com.proposal.Nature.Heaven.model.Category;
import com.proposal.Nature.Heaven.model.Plant;
import com.proposal.Nature.Heaven.model.PlantGuide;
import com.proposal.Nature.Heaven.service.CategoryService;
import com.proposal.Nature.Heaven.service.PlantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final PlantService plantService;
    @Value("${upload.path}")
    private final String uploadDir = "uploads/";

    @Autowired
    public CategoryController(CategoryService categoryService, PlantService plantService) {
        this.categoryService = categoryService;
        this.plantService = plantService;
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

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Category> existingCategory = categoryService.getCategoryById(id);

        if (existingCategory.isPresent()) {
            // Remove the category
            categoryService.deleteCategory(id); // Call the service to delete the category

            redirectAttributes.addFlashAttribute("message", "Category deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Category not found!");
        }

        return "redirect:/api/categories/"; // Redirect to the category list page
    }


    // Show the add category form
    @GetMapping("/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "add-category"; // This should match your Thymeleaf template name
    }

    // Handle form submission
    @PostMapping("/add")
    public String saveCategory(@ModelAttribute Category category,
                               @RequestParam("imageUrl") MultipartFile file,
                               RedirectAttributes redirectAttributes) throws IOException {
        // Validate the category object
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Image file cannot be empty");
            return "redirect:/add-category";  // Redirect back to the category add page
        }

        // Generate a unique filename to avoid conflicts
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File uploadDirectory = new File(uploadDir); // Use the path from the properties file
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs(); // Create directories if they don't exist
        }

        // Define the destination file path
        File destinationFile = new File(uploadDirectory, fileName);

        // Save the file to the server
        file.transferTo(destinationFile);

        // Store the file path in the category's imageUrl field
        category.setImageUrl(uploadDir + File.separator + fileName); // Ensure full path is set

        // Save the category in the database
        categoryService.saveCategory(category);

        // Add success message and redirect
        redirectAttributes.addFlashAttribute("message", "Category added successfully!");
        return "redirect:/api/categories/";
    }



}
