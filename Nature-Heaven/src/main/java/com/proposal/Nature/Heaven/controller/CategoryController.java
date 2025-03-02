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
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final PlantService plantService;
    @Value("${upload.path}")
    private  String uploadDir ;

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
    public String saveCategory(
            @RequestParam("name") String name, // Get category name
            @RequestParam("imageUrl") MultipartFile file, // Handle file upload
            RedirectAttributes redirectAttributes) throws IOException {

        // Create a new category object
        Category category = new Category();
        category.setName(name);

        // Validate that the image file is provided
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Image file cannot be empty");
            return "redirect:/add-category"; // Redirect to the add category page
        }

        // Generate a unique file name using current timestamp to avoid conflicts
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Define the upload directory path
        String uploadDir = "src/main/resources/static/img/";  // This works locally

        // Ensure the upload directory exists
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs(); // Create directories if they don't exist
        }

        // Define the destination file path (relative to static/img)
        File destinationFile = new File(uploadDirectory, fileName);

        // Save the image file to the destination directory
        file.transferTo(destinationFile);

        // Log the file path for debugging purposes
        System.out.println("File saved to: " + destinationFile.getAbsolutePath());

        // Store the relative path for the image in the category object
        category.setImageUrl("/img/" + fileName); // Use relative path

        // Save the category object
        categoryService.saveCategory(category);

        // Add a flash attribute to indicate success
        redirectAttributes.addFlashAttribute("message", "Category added successfully!");

        return "redirect:/api/categories/";  // Redirect to the list of categories (or wherever you need)
    }



}
