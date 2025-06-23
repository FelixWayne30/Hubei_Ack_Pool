package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin
public class CatalogController extends BaseController {

    @Autowired
    private CatalogService catalogService;

    @GetMapping(value = {"/catalogs"})
    public Object getCatalogs() {
        try {
            List<Map<String, Object>> catalogs = catalogService.getCatalogs();
            return renderSuccess(catalogs);
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }
}