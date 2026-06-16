package com.tubespbo.backend.model;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "main_node")
public class MainNode extends Node {
    @Column(name = "name")
    private String name;

    @Column(name = "category")
    private String category;

    public MainNode() {}

    public MainNode(String id, List<Double> coordinates, String name, String category) {
        super(id, coordinates);
        this.name = name;
        this.category = category;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String getDetails() {
        return "Main Node: " + this.name + " (" + this.category + ")";
    }
}