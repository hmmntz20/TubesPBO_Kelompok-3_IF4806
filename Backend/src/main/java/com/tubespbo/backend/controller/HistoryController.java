package com.tubespbo.backend.controller;

import com.tubespbo.backend.model.History;
import com.tubespbo.backend.model.TravelMode;
import com.tubespbo.backend.service.HistoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryManager historyManager;

    // Endpoint untuk mengambil riwayat (bisa difilter)
    // Contoh Panggilan: GET /api/history/user123?filter=CAR
    @GetMapping("/{userId}")
    public ResponseEntity<List<History>> getUserHistory(
            @PathVariable String userId,
            @RequestParam(required = false) TravelMode filter) {
        
        List<History> result;
        if (filter != null) {
            result = historyManager.applyFilter(userId, filter);
        } else {
            result = historyManager.getHistoryList(userId);
        }
        
        return ResponseEntity.ok(result);
    }

    // Endpoint untuk menyimpan riwayat baru
    @PostMapping("/save")
    public ResponseEntity<?> saveHistory(@RequestBody History history) {
        
        // Cek apakah user dan userId-nya ada
        if (history.getUser() == null || history.getUser().getUserId() == null) {
            return ResponseEntity.badRequest().body("Error: userId tidak boleh kosong!");
        }
        
        if (history.getHistoryId() == null || history.getHistoryId().isEmpty()) {
            history.setHistoryId(java.util.UUID.randomUUID().toString());
        }
        if (history.getTimestamp() == null) {
            history.setTimestamp(java.time.LocalDateTime.now());
        }
        
        History saved = historyManager.saveHistory(history);
        return ResponseEntity.ok(saved);
    }
}