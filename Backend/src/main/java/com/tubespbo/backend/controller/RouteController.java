package com.tubespbo.backend.controller;

import com.tubespbo.backend.model.RouteResult;
import com.tubespbo.backend.model.TravelMode;
import com.tubespbo.backend.service.AStarPathfinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private AStarPathfinder aStarPathfinder;

    @GetMapping("/find")
    public ResponseEntity<?> findRoute(
            @RequestParam String startNodeId,
            @RequestParam String targetNodeId,
            @RequestParam String mode,
            // ✅ FIX: Tambah param requireParking — default false agar tidak breaking
            @RequestParam(required = false, defaultValue = "false") boolean requireParking) {
        try {
            TravelMode travelMode = TravelMode.valueOf(mode.toUpperCase());
            // ✅ FIX: Gunakan overload yang menerima requireParking
            RouteResult result = aStarPathfinder.findShortestPath(
                startNodeId, targetNodeId, travelMode, requireParking
            );
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(404).body("Maaf, rute tidak ditemukan antara titik tersebut.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                "Error: Titik tidak valid atau mode kendaraan salah. Gunakan PEDESTRIAN, MOTORCYCLE, atau CAR."
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Terjadi kesalahan internal: " + e.getMessage());
        }
    }
}