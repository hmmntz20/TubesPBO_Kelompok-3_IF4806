package com.tubespbo.backend.model;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "main_edge")
public class MainEdge extends Edge {
    public MainEdge() {}
    public MainEdge(String id, String fromNode, String toNodeId, List<List<Double>> coordinates, Boolean isOneWay, Integer speedPedestrian, Integer speedMotorcycle, Integer speedCar, Boolean isPedestrianAllowed, Boolean isMotorcycleAllowed, Boolean isCarAllowed) {
        super(id, fromNode, toNodeId, coordinates, isOneWay, speedPedestrian, speedMotorcycle, speedCar, isPedestrianAllowed, isMotorcycleAllowed, isCarAllowed);
    }
}