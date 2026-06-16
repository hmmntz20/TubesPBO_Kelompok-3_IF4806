package com.tubespbo.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Node {

    @Id
    protected String id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    protected List<Double> coordinates;

    public Node() {}

    public Node(String id, List<Double> coordinates) {
        this.id = id;
        this.coordinates = coordinates;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<Double> getCoordinates() { return coordinates; }
    public void setCoordinates(List<Double> coordinates) { this.coordinates = coordinates; }

    public abstract String getDetails();
}