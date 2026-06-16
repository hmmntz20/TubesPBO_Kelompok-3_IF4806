package com.tubespbo.backend.controller;

import com.tubespbo.backend.model.MainNode;
import com.tubespbo.backend.model.Node;
import com.tubespbo.backend.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/map")
public class MapController {

    // Gunakan NodeRepository yang sudah mencakup semua jenis Node
    @Autowired
    private NodeRepository nodeRepository;

    // Mengambil semua lokasi (untuk dropdown)
    @GetMapping("/locations")
    public ResponseEntity<List<MainNode>> getAllLocations() {
        // Ambil semua node, saring hanya yang merupakan MainNode (bukan persimpangan)
        List<MainNode> mainNodes = nodeRepository.findAll().stream()
                .filter(node -> node instanceof MainNode)
                .map(node -> (MainNode) node)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(mainNodes);
    }

    // Mencari lokasi berdasarkan nama (Search bar)
    @GetMapping("/search")
    public ResponseEntity<List<MainNode>> searchLocations(@RequestParam String query) {
        List<MainNode> filtered = nodeRepository.findAll().stream()
                .filter(node -> node instanceof MainNode)
                .map(node -> (MainNode) node)
                // Pastikan nama tidak null dan cocok dengan pencarian
                .filter(mainNode -> mainNode.getName() != null && 
                                    mainNode.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(filtered);
    }
}