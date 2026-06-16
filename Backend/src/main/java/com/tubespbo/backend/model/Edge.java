package com.tubespbo.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Edge {

    @Id
    protected String id;

    @Column(name = "from_node")
    protected String fromNodeId;

    @Column(name = "to_node")
    protected String toNodeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    protected List<List<Double>> coordinates;

    @Column(name = "is_one_way")
    protected Boolean isOneWay;

    @Column(name = "speed_pedestrian")
    protected Integer speedPedestrian;

    @Column(name = "speed_motorcycle")
    protected Integer speedMotorcycle;

    @Column(name = "speed_car")
    protected Integer speedCar;

    @Column(name = "is_pedestrian_allowed")
    protected Boolean isPedestrianAllowed;

    @Column(name = "is_motorcycle_allowed")
    protected Boolean isMotorcycleAllowed;

    @Column(name = "is_car_allowed")
    protected Boolean isCarAllowed;

    public Edge() {}

    // Constructor sesuai class diagram (bisa disesuaikan isinya jika kepanjangan)
    public Edge(String id, String fromNodeId, String toNodeId, List<List<Double>> coordinates, Boolean isOneWay, Integer speedPedestrian, Integer speedMotorcycle, Integer speedCar, Boolean isPedestrianAllowed, Boolean isMotorcycleAllowed, Boolean isCarAllowed) {
        this.id = id;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.coordinates = coordinates;
        this.isOneWay = isOneWay;
        this.speedPedestrian = speedPedestrian;
        this.speedMotorcycle = speedMotorcycle;
        this.speedCar = speedCar;
        this.isPedestrianAllowed = isPedestrianAllowed;
        this.isMotorcycleAllowed = isMotorcycleAllowed;
        this.isCarAllowed = isCarAllowed;
    }

    // Getter Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFromNode() { return fromNodeId; }
    public void setFromNode(String fromNodeId) { this.fromNodeId = fromNodeId; }
    public String getToNode() { return toNodeId; }
    public void setToNode(String toNodeId) { this.toNodeId = toNodeId; }
    public List<List<Double>> getCoordinates() { return coordinates; }
    public void setCoordinates(List<List<Double>> coordinates) { this.coordinates = coordinates; }
    public Boolean getIsOneWay() { return isOneWay; }
    public void setIsOneWay(Boolean isOneWay) { this.isOneWay = isOneWay; }

    // Logic Method Sesuai Spesifikasi
    public Float calculateDistance() {
        // Logika Haversine opsional untuk ngitung dari coordinates
        return 0f;
    }

    public Boolean isAccessibleBy(TravelMode mode) {
        return switch (mode) {
            case PEDESTRIAN -> isPedestrianAllowed != null ? isPedestrianAllowed : true;
            case MOTORCYCLE -> isMotorcycleAllowed != null ? isMotorcycleAllowed : true;
            case CAR -> isCarAllowed != null ? isCarAllowed : true;
        };
    }

    public Float getDuration(TravelMode mode, float distanceInMeters) {
        float speedKmh = switch (mode) {
            case PEDESTRIAN -> speedPedestrian != null ? speedPedestrian : 5;
            case MOTORCYCLE -> speedMotorcycle != null ? speedMotorcycle : 20;
            case CAR -> speedCar != null ? speedCar : 15;
        };
        float speedMs = speedKmh / 3.6f;
        return distanceInMeters / speedMs;
    }
}