package com.example.controller;


import com.example.service.DishFlavorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dishflavor")
public class DishFlavorController {
    @Autowired
    private DishFlavorService dishFlavorService;
}
