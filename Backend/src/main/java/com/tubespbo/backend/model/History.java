package com.tubespbo.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "history")
public class History {

    @Id
    @Column(name = "id")
    private String historyId;

    // Relasi Many-to-One ke tabel users
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime timestamp;

    @Column(name = "start_point_name")
    private String startPointName;

    @Column(name = "end_point_name")
    private String endPointName;

    @Column(name = "distance")
    private Float distance;

    @Column(name = "duration")
    private Float duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode")
    private TravelMode transportMode;

    public History() {}

    public History(String historyId, User user, LocalDateTime timestamp, String startPointName, 
                   String endPointName, Float distance, Float duration, TravelMode transportMode) {
        this.historyId = historyId;
        this.user = user;
        this.timestamp = timestamp;
        this.startPointName = startPointName;
        this.endPointName = endPointName;
        this.distance = distance;
        this.duration = duration;
        this.transportMode = transportMode;
    }

    // --- Getter Setter ---
    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getStartPointName() { return startPointName; }
    public void setStartPointName(String startPointName) { this.startPointName = startPointName; }
    public String getEndPointName() { return endPointName; }
    public void setEndPointName(String endPointName) { this.endPointName = endPointName; }
    public Float getDistance() { return distance; }
    public void setDistance(Float distance) { this.distance = distance; }
    public Float getDuration() { return duration; }
    public void setDuration(Float duration) { this.duration = duration; }
    public TravelMode getTransportMode() { return transportMode; }
    public void setTransportMode(TravelMode transportMode) { this.transportMode = transportMode; }

    // --- Method Khusus Sesuai Class Diagram ---
    public String getFormattedDate() {
        if (timestamp == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        return timestamp.format(formatter);
    }

    public String getFormattedDistance() {
        if (distance == null) return "0 m";
        if (distance >= 1000) {
            return String.format("%.2f km", distance / 1000);
        }
        return String.format("%.0f m", distance);
    }
}