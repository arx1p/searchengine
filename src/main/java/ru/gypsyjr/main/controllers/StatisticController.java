package ru.gypsyjr.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gypsyjr.main.Storage;
import ru.gypsyjr.main.models.ApiStatistics;

@RequestMapping
@RestController
public class StatisticController {
    @Autowired
    private Storage storage;

    @GetMapping("/statistics")
    public ResponseEntity<ApiStatistics> getStatistics() {
        return ResponseEntity.ok().body(storage.getStatistic());
    }
}
