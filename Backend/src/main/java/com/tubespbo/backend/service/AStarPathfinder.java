package com.tubespbo.backend.service;

import com.tubespbo.backend.model.Edge;
import com.tubespbo.backend.model.MainNode;
import com.tubespbo.backend.model.Node;
import com.tubespbo.backend.model.RouteResult;
import com.tubespbo.backend.model.TravelMode;
import com.tubespbo.backend.repository.EdgeRepository;
import com.tubespbo.backend.repository.NodeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AStarPathfinder {

    private List<Node> nodeList = new ArrayList<>();
    private List<Edge> edgeList = new ArrayList<>();

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private EdgeRepository edgeRepository;

    private Map<String, Node> nodeMap = new HashMap<>();
    private Map<String, List<Edge>> adjacencyList = new HashMap<>();

    public AStarPathfinder() {}

    @PostConstruct
    public void initializeMapFromDatabase() {
        System.out.println("⏳ Mengambil data peta dari Supabase...");

        List<Node> allNodes = nodeRepository.findAll();
        List<Edge> allEdges = edgeRepository.findAll();

        nodeMap.clear();
        adjacencyList.clear();

        for (Node node : allNodes) {
            nodeMap.put(node.getId(), node);
            adjacencyList.put(node.getId(), new ArrayList<>());
        }

        // Verifikasi edge pertama
        if (!allEdges.isEmpty()) {
            Edge firstEdge = allEdges.get(0);
            System.out.println("DEBUG: Edge[0] id=" + firstEdge.getId());
            System.out.println("DEBUG: Edge[0] fromNode=" + firstEdge.getFromNode());
            System.out.println("DEBUG: Edge[0] toNode=" + firstEdge.getToNode());
        }

        int mapped = 0, unmapped = 0;
        for (Edge edge : allEdges) {
            String from = edge.getFromNode();
            String to = edge.getToNode();

            if (from != null && adjacencyList.containsKey(from)) {
                adjacencyList.get(from).add(edge);
                mapped++;
            } else {
                if (unmapped < 3) {
                    System.out.println("DEBUG: Edge gagal - id='" + edge.getId()
                        + "' from='" + from + "'");
                }
                unmapped++;
            }

            if (edge.getIsOneWay() == null || !edge.getIsOneWay()) {
                if (to != null && adjacencyList.containsKey(to)) {
                    adjacencyList.get(to).add(edge);
                }
            }
        }

        System.out.println("✅ Peta dimuat! Node: " + allNodes.size()
            + " | Edge: " + allEdges.size()
            + " | Terpetakan: " + mapped
            + " | Gagal: " + unmapped);
    }

    public void loadMapData(List<Node> nodes, List<Edge> edges) {
        this.nodeList = nodes;
        this.edgeList = edges;
        this.nodeMap.clear();
        this.adjacencyList.clear();

        for (Node node : nodes) {
            nodeMap.put(node.getId(), node);
            adjacencyList.put(node.getId(), new ArrayList<>());
        }

        for (Edge edge : edges) {
            if (edge.getFromNode() != null && adjacencyList.containsKey(edge.getFromNode())) {
                adjacencyList.get(edge.getFromNode()).add(edge);
            }
            if ((edge.getIsOneWay() == null || !edge.getIsOneWay())
                    && edge.getToNode() != null
                    && adjacencyList.containsKey(edge.getToNode())) {
                adjacencyList.get(edge.getToNode()).add(edge);
            }
        }
    }

    private Float haversineDistance(List<Double> coord1, List<Double> coord2) {
        final int R = 6371000;
        double lon1 = coord1.get(0); double lat1 = coord1.get(1);
        double lon2 = coord2.get(0); double lat2 = coord2.get(1);

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (float) (R * c);
    }

    private record PathStep(String previousNodeId, Edge edgeUsed) {}
    private record QueueItem(String nodeId, float fScore) {}

    private RouteResult executeAStar(String startNodeId, String targetNodeId, TravelMode mode) {
        if (!nodeMap.containsKey(startNodeId) || !nodeMap.containsKey(targetNodeId)) {
            throw new IllegalArgumentException("Node awal atau akhir tidak ditemukan di peta.");
        }

        PriorityQueue<QueueItem> openSet = new PriorityQueue<>(Comparator.comparingDouble(QueueItem::fScore));
        Map<String, PathStep> cameFrom = new HashMap<>();
        Map<String, Float> gScore = new HashMap<>();
        Set<String> closedSet = new HashSet<>();

        gScore.put(startNodeId, 0f);
        float initialH = haversineDistance(
            nodeMap.get(startNodeId).getCoordinates(),
            nodeMap.get(targetNodeId).getCoordinates()
        );
        openSet.add(new QueueItem(startNodeId, initialH));

        while (!openSet.isEmpty()) {
            QueueItem current = openSet.poll();
            String currentId = current.nodeId();

            if (currentId.equals(targetNodeId)) {
                return reconstructPath(cameFrom, currentId, mode);
            }

            if (!closedSet.add(currentId)) continue;

            List<Edge> neighbors = adjacencyList.getOrDefault(currentId, Collections.emptyList());

            for (Edge edge : neighbors) {
                if (!edge.isAccessibleBy(mode)) continue;

                String neighborId = edge.getFromNode().equals(currentId)
                    ? edge.getToNode()
                    : edge.getFromNode();

                if (neighborId == null || closedSet.contains(neighborId)) continue;

                Node n1 = nodeMap.get(currentId);
                Node n2 = nodeMap.get(neighborId);

                if (n1 == null || n2 == null) continue;

                float distance = haversineDistance(n1.getCoordinates(), n2.getCoordinates());
                float edgeCost = mode == TravelMode.PEDESTRIAN
                    ? distance
                    : edge.getDuration(mode, distance);

                float currentG = gScore.getOrDefault(currentId, Float.MAX_VALUE);
                float tentativeG = currentG + edgeCost;

                if (tentativeG < gScore.getOrDefault(neighborId, Float.MAX_VALUE)) {
                    cameFrom.put(neighborId, new PathStep(currentId, edge));
                    gScore.put(neighborId, tentativeG);

                    Node neighborNode = nodeMap.get(neighborId);
                    if (neighborNode != null) {
                        float h = haversineDistance(
                            neighborNode.getCoordinates(),
                            nodeMap.get(targetNodeId).getCoordinates()
                        );
                        openSet.add(new QueueItem(neighborId, tentativeG + h));
                    }
                }
            }
        }

        return null;
    }

    // -----------------------------------------------------------------------
    // ORKESTRATOR RUTE (MULTI-LEG ROUTING)
    // -----------------------------------------------------------------------
    
    // Method default untuk Controller (Otomatis Drop-Off)
    public RouteResult findShortestPath(String startNodeId, String targetNodeId, TravelMode mode) {
        return findShortestPath(startNodeId, targetNodeId, mode, false);
    }

    // Method utama yang mendukung Smart Parking
    public RouteResult findShortestPath(String startNodeId, String targetNodeId, TravelMode mode, boolean requireParking) {
        if (!nodeMap.containsKey(startNodeId) || !nodeMap.containsKey(targetNodeId)) {
            throw new IllegalArgumentException("Node awal atau akhir tidak ditemukan di peta.");
        }

        // Jika jalan kaki, murni 1x A* saja
        if (mode == TravelMode.PEDESTRIAN) {
            String actualStart = getClosestValidNodeId(startNodeId, mode, false);
            String actualTarget = getClosestValidNodeId(targetNodeId, mode, false);
            RouteResult result = executeAStar(actualStart, actualTarget, mode);
            
            // Tambahkan garis putus-putus jika keluar dari gedung terisolasi
            if (result != null) {
                if (!startNodeId.equals(actualStart)) result.setWalkCoordinatesStart(java.util.List.of(nodeMap.get(startNodeId).getCoordinates(), nodeMap.get(actualStart).getCoordinates()));
                if (!targetNodeId.equals(actualTarget)) result.setWalkCoordinatesEnd(java.util.List.of(nodeMap.get(actualTarget).getCoordinates(), nodeMap.get(targetNodeId).getCoordinates()));
                generateNavigationInstructions(result);
            }
            return result;
        }

        // --- JIKA NAIK KENDARAAN (ESTAFET) ---
        String routingStartId = getClosestValidNodeId(startNodeId, mode, requireParking);
        String routingTargetId = getClosestValidNodeId(targetNodeId, mode, requireParking);

        // Jika tempat parkirnya sama dengan titik awal, berarti cukup jalan kaki saja
        if (routingStartId.equals(routingTargetId)) {
            return findShortestPath(startNodeId, targetNodeId, TravelMode.PEDESTRIAN, false);
        }

        RouteResult walkStartPath = null;
        RouteResult walkEndPath = null;

        // 1. A* Jalan Kaki dari Gedung ke Mobil/Parkiran
        if (!routingStartId.equals(startNodeId)) walkStartPath = executeAStar(startNodeId, routingStartId, TravelMode.PEDESTRIAN);
        
        // 2. A* Jalan Kaki dari Parkiran ke Gedung Tujuan
        if (!routingTargetId.equals(targetNodeId)) walkEndPath = executeAStar(routingTargetId, targetNodeId, TravelMode.PEDESTRIAN);

        // 3. A* Berkendara (Rute Utama)
        RouteResult mainDrive = executeAStar(routingStartId, routingTargetId, mode);
        if (mainDrive == null) return null; // Gagal cari rute kendaraan

        // Gabungkan Ketiganya!
        return combineRouteResults(startNodeId, targetNodeId, walkStartPath, mainDrive, walkEndPath, mode);
    }

    private RouteResult combineRouteResults(String originalStartId, String originalTargetId, 
                                            RouteResult startWalk, RouteResult mainDrive, 
                                            RouteResult endWalk, TravelMode mode) {
        List<Node> finalNodes = new java.util.ArrayList<>();
        List<Edge> finalEdges = new java.util.ArrayList<>();
        List<List<Double>> finalCoords = new java.util.ArrayList<>();
        float finalDist = 0f; float finalDur = 0f;

        // Rangkai Walk Start
        if (startWalk != null) {
            finalNodes.addAll(startWalk.getNodes()); finalEdges.addAll(startWalk.getEdges());
            finalCoords.addAll(startWalk.getCoordinates());
            finalDist += startWalk.getTotalDistance(); finalDur += startWalk.getTotalDuration();
        }

        // Rangkai Main Drive
        if (!finalNodes.isEmpty() && !mainDrive.getNodes().isEmpty()) {
            finalNodes.addAll(mainDrive.getNodes().subList(1, mainDrive.getNodes().size())); // Hindari node ganda
        } else {
            finalNodes.addAll(mainDrive.getNodes());
        }
        finalEdges.addAll(mainDrive.getEdges());
        if (!finalCoords.isEmpty() && !mainDrive.getCoordinates().isEmpty()) {
            finalCoords.addAll(mainDrive.getCoordinates().subList(1, mainDrive.getCoordinates().size()));
        } else {
            finalCoords.addAll(mainDrive.getCoordinates());
        }
        finalDist += mainDrive.getTotalDistance(); finalDur += mainDrive.getTotalDuration();

        // Rangkai Walk End
        if (endWalk != null) {
            if (!finalNodes.isEmpty() && !endWalk.getNodes().isEmpty()) finalNodes.addAll(endWalk.getNodes().subList(1, endWalk.getNodes().size()));
            else finalNodes.addAll(endWalk.getNodes());
            
            finalEdges.addAll(endWalk.getEdges());
            if (!finalCoords.isEmpty() && !endWalk.getCoordinates().isEmpty()) finalCoords.addAll(endWalk.getCoordinates().subList(1, endWalk.getCoordinates().size()));
            else finalCoords.addAll(endWalk.getCoordinates());
            
            finalDist += endWalk.getTotalDistance(); finalDur += endWalk.getTotalDuration();
        }

        RouteResult combined = new RouteResult(finalNodes, finalEdges, finalDist, finalDur, finalCoords, mode);

        // Fallback Garis Putus-Putus jika terisolasi dari trotoar pejalan kaki
        String rStartId = mainDrive.getNodes().get(0).getId();
        String rTargetId = mainDrive.getNodes().get(mainDrive.getNodes().size()-1).getId();

        if (startWalk == null && !originalStartId.equals(rStartId)) {
            combined.setWalkCoordinatesStart(java.util.List.of(nodeMap.get(originalStartId).getCoordinates(), nodeMap.get(rStartId).getCoordinates()));
        }
        if (endWalk == null && !originalTargetId.equals(rTargetId)) {
            combined.setWalkCoordinatesEnd(java.util.List.of(nodeMap.get(rTargetId).getCoordinates(), nodeMap.get(originalTargetId).getCoordinates()));
        }

        generateNavigationInstructions(combined);
        return combined;
    }

    private RouteResult reconstructPath(Map<String, PathStep> cameFrom, String currentId, TravelMode mode) {
        List<Node> path = new java.util.LinkedList<>();
        List<Edge> edgesUsed = new java.util.LinkedList<>();
        float totalDistance = 0f;
        float totalDuration = 0f;

        String curr = currentId;
        path.add(0, nodeMap.get(curr));

        while (cameFrom.containsKey(curr)) {
            PathStep step = cameFrom.get(curr);
            Node n1 = nodeMap.get(step.previousNodeId());
            Node n2 = nodeMap.get(curr);
            float dist = haversineDistance(n1.getCoordinates(), n2.getCoordinates());
            
            totalDistance += dist;
            totalDuration += step.edgeUsed().getDuration(mode, dist);
            edgesUsed.add(0, step.edgeUsed());
            
            curr = step.previousNodeId();
            path.add(0, nodeMap.get(curr));
        }

        List<List<Double>> coordinates = new java.util.ArrayList<>();
        for (int i = 0; i < edgesUsed.size(); i++) {
            Edge edge = edgesUsed.get(i);
            Node startNodeOfThisEdge = path.get(i); // Titik kita berdiri saat ini
            
            // Copy list agar tidak merubah urutan asli di memori Java
            List<List<Double>> edgeCoords = new java.util.ArrayList<>(edge.getCoordinates());
            
            // JIKA jalan ini dilewati dari arah terbalik, REVERSE urutannya!
            // (Sama seperti [...coords].reverse() di TypeScript)
            if (!edge.getFromNode().equals(startNodeOfThisEdge.getId())) {
                java.util.Collections.reverse(edgeCoords);
            }

            if (edgeCoords != null && !edgeCoords.isEmpty()) {
                if (i == 0) coordinates.addAll(edgeCoords);
                else coordinates.addAll(edgeCoords.subList(1, edgeCoords.size()));
            }
        }

        return new RouteResult(path, edgesUsed, totalDistance, totalDuration, coordinates, mode);
    }

    // =================================================================================
    // 1. FITUR SNAP TO ROAD (Mencari jalan terdekat jika user di dalam gedung)
    // =================================================================================
    private String getClosestValidNodeId(String nodeId, TravelMode mode, boolean requireParking) {
        List<Node> validNodes = new java.util.ArrayList<>();
        
        // 1. Kumpulkan semua jalan/node yang bisa dilewati kendaraan tersebut
        for (Map.Entry<String, List<Edge>> entry : adjacencyList.entrySet()) {
            boolean hasAccess = false;
            for (Edge e : entry.getValue()) {
                if (e.isAccessibleBy(mode)) { hasAccess = true; break; }
            }
            if (hasAccess && nodeMap.containsKey(entry.getKey())) {
                validNodes.add(nodeMap.get(entry.getKey()));
            }
        }

        List<Node> nodesToCheck = validNodes;

        // 2. Filter KHUSUS Parkiran (Hanya jika requireParking = true)
        if (requireParking && mode != TravelMode.PEDESTRIAN) {
            List<Node> parkingNodes = new java.util.ArrayList<>();
            for (Node n : validNodes) {
                boolean isParking = n.getId().toLowerCase().contains("parkiran");
                if (n instanceof MainNode && "parking".equalsIgnoreCase(((MainNode) n).getCategory())) {
                    isParking = true;
                }

                // Jangan biarkan motor parkir di tempat mobil, dan sebaliknya
                if (isParking) {
                    String idLower = n.getId().toLowerCase();
                    if (mode == TravelMode.MOTORCYCLE && idLower.contains("mobil")) continue;
                    if (mode == TravelMode.CAR && idLower.contains("motor")) continue;
                    parkingNodes.add(n);
                }
            }
            if (!parkingNodes.isEmpty()) {
                nodesToCheck = parkingNodes; // Prioritaskan parkiran jika ada!
            }
        }

        // 3. Cari yang jaraknya paling dekat dengan tujuan
        Node targetNode = nodeMap.get(nodeId);
        String closestId = nodeId;
        float minDistance = Float.MAX_VALUE;

        for (Node candidate : nodesToCheck) {
            float dist = haversineDistance(targetNode.getCoordinates(), candidate.getCoordinates());
            if (dist < minDistance) {
                minDistance = dist;
                closestId = candidate.getId();
            }
        }
        return closestId;
    }

    // =================================================================================
    // 2. FITUR NAVIGASI (Turn-by-Turn Math)
    // =================================================================================
    private double calculateBearing(List<Double> start, List<Double> end) {
        double startLat = Math.toRadians(start.get(1));
        double startLng = Math.toRadians(start.get(0));
        double endLat = Math.toRadians(end.get(1));
        double endLng = Math.toRadians(end.get(0));

        double dLng = endLng - startLng;
        double y = Math.sin(dLng) * Math.cos(endLat);
        double x = Math.cos(startLat) * Math.sin(endLat) -
                   Math.sin(startLat) * Math.cos(endLat) * Math.cos(dLng);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    private String getTurnInstruction(double bearing1, double bearing2) {
        double diff = (bearing2 - bearing1 + 360) % 360;
        if (diff > 340 || diff < 20) return "straight";
        if (diff >= 20 && diff < 160) return "turn_right";
        if (diff >= 160 && diff <= 200) return "uturn";
        if (diff > 200 && diff <= 340) return "turn_left";
        return "straight";
    }

    public void generateNavigationInstructions(RouteResult result) {
        List<RouteResult.NavigationInstruction> instructions = new java.util.ArrayList<>();
        List<Node> nodes = result.getNodes();
        List<Edge> edges = result.getEdges();
        TravelMode mode = result.getMode();

        if (nodes.size() < 2) return;

        float currentSegmentDist = 0;
        float currentSegmentDur = 0;
        String currentInstructionType = "straight";

        for (int i = 0; i < nodes.size() - 1; i++) {
            Node currentNode = nodes.get(i);
            Node nextNode = nodes.get(i + 1);
            Edge currentEdge = edges.get(i);

            float edgeDist = haversineDistance(currentNode.getCoordinates(), nextNode.getCoordinates());
            float edgeDur = currentEdge.getDuration(mode, edgeDist);

            currentSegmentDist += edgeDist;
            currentSegmentDur += edgeDur;

            if (i < nodes.size() - 2) {
                Node nextNextNode = nodes.get(i + 2);
                double bearing1 = calculateBearing(currentNode.getCoordinates(), nextNode.getCoordinates());
                double bearing2 = calculateBearing(nextNode.getCoordinates(), nextNextNode.getCoordinates());
                
                String turn = getTurnInstruction(bearing1, bearing2);

                if (!turn.equals("straight")) {
                    String text = turn.equals("turn_left") ? "Turn left" :
                                  turn.equals("turn_right") ? "Turn right" : "U-turn";
                    text += " sejauh " + Math.round(currentSegmentDist) + " meter";

                    instructions.add(new RouteResult.NavigationInstruction(
                        turn, currentSegmentDist, currentSegmentDur, text, nextNode.getCoordinates()
                    ));

                    currentSegmentDist = 0;
                    currentSegmentDur = 0;
                    currentInstructionType = turn;
                }
            }
        }

        // Tambahkan instruksi sampai tujuan
        instructions.add(new RouteResult.NavigationInstruction(
            "arrive", currentSegmentDist, currentSegmentDur, 
            "Tiba di tujuan setelah " + Math.round(currentSegmentDist) + " meter", 
            nodes.get(nodes.size() - 1).getCoordinates()
        ));

        result.setInstructions(instructions);
    }
}