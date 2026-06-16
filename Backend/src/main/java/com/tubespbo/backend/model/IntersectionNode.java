package com.tubespbo.backend.model;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "intersection_node")
public class IntersectionNode extends Node {
    
    public IntersectionNode() {}

    public IntersectionNode(String id, List<Double> coordinates) {
        super(id, coordinates);
    }

    @Override
    public String getDetails() {
        return "Intersection Node: " + this.id;
    }
}