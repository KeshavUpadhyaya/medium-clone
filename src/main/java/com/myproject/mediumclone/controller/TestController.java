package com.myproject.mediumclone.controller;

import com.myproject.mediumclone.model.Test;
import com.myproject.mediumclone.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("medium-clone/api/v1")
public class TestController {

    @Autowired
    private TestRepository testRepository;

    @GetMapping("/simple-test")
    public String test() {
        return "Hello world";
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
