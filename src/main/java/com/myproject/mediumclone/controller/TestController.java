package com.myproject.mediumclone.controller;

import com.myproject.mediumclone.model.Test;
import com.myproject.mediumclone.repository.TestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private TestRepository testRepository;

    @GetMapping("/hello")
    public Map<String, String> test() {
        return Map.of("data", "hello world!");
    }

    @PostMapping("/data")
    public ResponseEntity<Test> putData(@RequestBody Test test) {
        Test savedTest = testRepository.save(test);
        return new ResponseEntity<Test>(savedTest, HttpStatus.CREATED);
    }

    @GetMapping("/data")
    public List<Test> getAllTestData() {
        return testRepository.findAll();

    }
}
