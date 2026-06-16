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

    // Method untuk menyimpan riwayat baru setelah user mencari rute
    public History saveHistory(History history) {
        return historyRepository.save(history);
    }

    // Mengambil semua history milik user
    public List<History> getHistoryList(String userId) {
        return historyRepository.findByUser_UserIdOrderByTimestampDesc(userId);
    }

    // Method sesuai Class Diagram: Logika Filter di Layar History
    public List<History> applyFilter(String userId, TravelMode filterType) {
        List<History> allHistory = getHistoryList(userId);
        
        // Jika tidak ada filter, kembalikan semua
        if (filterType == null) {
            return allHistory;
        }

        // Filter berdasarkan transport mode
        return allHistory.stream()
                .filter(h -> h.getTransportMode() == filterType)
                .collect(Collectors.toList());
    }
}