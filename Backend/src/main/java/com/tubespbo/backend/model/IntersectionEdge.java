package com.tubespbo.backend.model;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "intersection_edge")
public class IntersectionEdge extends Edge {
    public IntersectionEdge() {}
    public IntersectionEdge(String id, String fromNode, String toNodeId, List<List<Double>> coordinates, Boolean isOneWay, Integer speedPedestrian, Integer speedMotorcycle, Integer speedCar, Boolean isPedestrianAllowed, Boolean isMotorcycleAllowed, Boolean isCarAllowed) {
        super(id, fromNode, toNodeId, coordinates, isOneWay, speedPedestrian, speedMotorcycle, speedCar, isPedestrianAllowed, isMotorcycleAllowed, isCarAllowed);
    }
}