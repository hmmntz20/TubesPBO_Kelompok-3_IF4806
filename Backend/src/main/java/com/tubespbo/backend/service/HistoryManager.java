package com.tubespbo.backend.service;

import com.tubespbo.backend.model.History;
import com.tubespbo.backend.model.TravelMode;
import com.tubespbo.backend.repository.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryManager {

    @Autowired
    private HistoryRepository historyRepository;

    public History saveHistory(History history) {
        return historyRepository.save(history);
    }

    // --- UBAH BAGIAN INI ---
    public List<History> getHistoryList(String userId) {
        // Konversi String dari Frontend menjadi tipe UUID untuk Database
        java.util.UUID userUuid = java.util.UUID.fromString(userId);
        return historyRepository.findByUser_UserIdOrderByTimestampDesc(userUuid);
    }

    public List<History> applyFilter(String userId, TravelMode filterType) {
        List<History> allHistory = getHistoryList(userId);
        
        if (filterType == null) {
            return allHistory;
        }

        return allHistory.stream()
                .filter(h -> h.getTransportMode() == filterType)
                .collect(Collectors.toList());
    }
}