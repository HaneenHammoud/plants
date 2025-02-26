package com.proposal.Nature.Heaven.controller;

import com.proposal.Nature.Heaven.model.Category;
import com.proposal.Nature.Heaven.model.Plant;
import com.proposal.Nature.Heaven.model.PlantGuide;
import com.proposal.Nature.Heaven.repository.CategoryRepository;
import com.proposal.Nature.Heaven.repository.PlantGuideRepository;
import com.proposal.Nature.Heaven.repository.PlantRepository;
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
@RequestMapping("/api/plants")
public class PlantController {

    private final PlantService plantService;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    private final PlantGuideRepository plantGuideRepository;
    private final PlantRepository plantRepository;


    @Autowired
    public PlantController(PlantService plantService,CategoryService categoryService,CategoryRepository categoryRepository,
                           PlantGuideRepository plantGuideRepository,PlantRepository plantRepository) {
        this.plantService = plantService;
        this.categoryService=categoryService;
        this.plantGuideRepository=plantGuideRepository;
        this.plantRepository=plantRepository;
        this.categoryRepository=categoryRepository;
    }

    @GetMapping("/")
    public String getPlants(Model model) {
        List<Plant> plantsList = plantService.getAllPlants(); // Fetching all plants
        model.addAttribute("plantsList", plantsList); // Adding the list to the model
        return "plant";
    }

    // Get all plants
    @GetMapping
    public ResponseEntity<List<Plant>> getAllPlants() {
        List<Plant> plants = plantService.getAllPlants();
        return new ResponseEntity<>(plants, HttpStatus.OK);
    }

    @GetMapping("/plant/{id}/guide")
    public String viewPlantGuide(@PathVariable("id") Long plantId, Model model) {
        Optional<Plant> plant = plantService.getPlantById(plantId);

        if (plant.isPresent()) {
            model.addAttribute("plant", plant.get());
            return "plant-guide";
        } else {
            model.addAttribute("error", "Plant not found");
            return "error";
        }
    }



    // Get plant by ID
    @GetMapping("/{id}")
    public ResponseEntity<Plant> getPlantById(@PathVariable Long id) {
        Optional<Plant> plant = plantService.getPlantById(id);
        return plant.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Create a new plant
    @PostMapping
    public ResponseEntity<Plant> createPlant(@RequestBody Plant plant) {
        Plant savedPlant = plantService.savePlant(plant);
        return new ResponseEntity<>(savedPlant, HttpStatus.CREATED);
    }


    @GetMapping("/add")
    public String showAddPlantForm(Model model) {
        // Initialize the plant object and its associated plantGuide
        Plant plant = new Plant();
        plant.setPlantGuide(new PlantGuide());  // Ensure plantGuide is initialized

        model.addAttribute("plant", plant);

        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        return "add-plant"; // Thymeleaf template name
    }


    // Handle form submission to add a new plant
    @PostMapping("/add")
    public String addPlant(@ModelAttribute("plant") Plant plant, Model model) {
        plantGuideRepository.save(plant.getPlantGuide());
        plantRepository.save(plant);
        model.addAttribute("plant", plant);
        return "redirect:/api/plants/";
    }


    // Update a plant
    @PutMapping("/{id}")
    public ResponseEntity<Plant> updatePlant(@PathVariable Long id, @RequestBody Plant plant) {
        Optional<Plant> existingPlant = plantService.getPlantById(id);
        if (existingPlant.isPresent()) {
            plant.setId(id);
            Plant updatedPlant = plantService.savePlant(plant);
            return new ResponseEntity<>(updatedPlant, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete a plant for ajax
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePlant(@PathVariable Long id) {
        Optional<Plant> existingPlant = plantService.getPlantById(id);
        if (existingPlant.isPresent()) {
            plantService.deletePlant(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
