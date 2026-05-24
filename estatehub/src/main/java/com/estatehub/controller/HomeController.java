package com.estatehub.controller;

import com.estatehub.entity.Property;
import com.estatehub.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private PropertyService propertyService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Property> featured = propertyService.getAllAvailableProperties();
        // Show up to 6 featured properties on landing page
        model.addAttribute("featuredProperties", featured.size() > 6 ? featured.subList(0, 6) : featured);
        model.addAttribute("cities", propertyService.getValidCities());
        return "home";
    }

    @GetMapping("/about")
    public String about() { return "about"; }

    @GetMapping("/contact")
    public String contact() { return "contact"; }
}
