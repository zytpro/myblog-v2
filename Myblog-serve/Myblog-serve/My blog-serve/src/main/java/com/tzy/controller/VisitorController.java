package com.tzy.controller;

import com.tzy.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class VisitorController {
    @Autowired
    private VisitorService visitorService;

    @GetMapping("/visitor/count")
    public List<Map<String, Object>> getUniqueVisitorCount() {
        return visitorService.getUniqueVisitors();
    }
}
