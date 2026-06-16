package com.tubespbo.backend.repository;

import com.tubespbo.backend.model.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, String> {
    // Mengambil riwayat milik user tertentu, diurutkan dari yang paling baru
    List<History> findByUser_UserIdOrderByTimestampDesc(String userId);
}