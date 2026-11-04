package com.pratham.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/demo-controller")
public class DemoController {
    @GetMapping("/greet")
    public String greet() {
        return "Hello from secure endpoint";
    }
}
