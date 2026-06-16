package com.tubespbo.backend.model;

import java.util.List;

public class RouteResult {
    private List<Node> nodes;
    private List<Edge> edges;
    private Float totalDistance;
    private Float totalDuration;
    private List<List<Double>> coordinates;
    private TravelMode mode;
    private List<List<Double>> walkCoordinatesStart;
    private List<List<Double>> walkCoordinatesEnd;
    private List<NavigationInstruction> instructions;

    public RouteResult(List<Node> nodes, List<Edge> edges, Float totalDistance, Float totalDuration, 
                       List<List<Double>> coordinates, TravelMode mode) {
        this.nodes = nodes;
        this.edges = edges;
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.coordinates = coordinates;
        this.mode = mode;
    }

    // --- Getter & Setter Standard ---
    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
    public Float getTotalDistance() { return totalDistance; }
    public Float getTotalDuration() { return totalDuration; }
    public List<List<Double>> getCoordinates() { return coordinates; }
    public TravelMode getMode() { return mode; }
    
    public List<List<Double>> getWalkCoordinatesStart() { return walkCoordinatesStart; }
    public void setWalkCoordinatesStart(List<List<Double>> walkCoordinatesStart) { this.walkCoordinatesStart = walkCoordinatesStart; }
    public List<List<Double>> getWalkCoordinatesEnd() { return walkCoordinatesEnd; }
    public void setWalkCoordinatesEnd(List<List<Double>> walkCoordinatesEnd) { this.walkCoordinatesEnd = walkCoordinatesEnd; }
    public List<NavigationInstruction> getInstructions() { return instructions; }
    public void setInstructions(List<NavigationInstruction> instructions) { this.instructions = instructions; }

    // Inner Class untuk instruksi navigasi (sama seperti di TS)
    public static class NavigationInstruction {
        public String type; // "straight", "turn_left", "turn_right", "uturn", "arrive"
        public float distance;
        public float duration;
        public String text;
        public List<Double> coordinate;

        public NavigationInstruction(String type, float distance, float duration, String text, List<Double> coordinate) {
            this.type = type;
            this.distance = distance;
            this.duration = duration;
            this.text = text;
            this.coordinate = coordinate;
        }
    }
}