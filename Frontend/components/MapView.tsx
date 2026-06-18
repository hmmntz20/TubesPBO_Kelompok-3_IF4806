import { Ionicons } from "@expo/vector-icons";
import React, { useEffect, useRef, useState } from "react";
import { Dimensions, View } from "react-native";
import MapView, {
  Marker,
  Polygon,
  Polyline,
  PROVIDER_GOOGLE,
  Region,
} from "react-native-maps";
import tw from "twrnc";
import { Colors } from "../constants/Colors";
import { GeoNode, PathResult, TravelMode } from "@/config/types";
import CustomPinMarker from "./CustomPinMaker";

const { width, height } = Dimensions.get("window");

const TELKOM_REGION: Region = {
  latitude: -6.972,
  longitude: 107.6304,
  latitudeDelta: 0.008,
  longitudeDelta: 0.008,
};

interface Props {
  selectedMode: TravelMode;
  fromNodeId: string | null;
  toNodeId: string | null;
  exactStartCoord?: [number, number] | null;
  isNavigating?: boolean;
  mapType: "standard" | "satellite" | "hybrid" | "terrain";
  recenterSignal: number;
  routeResult: PathResult | null; // Data rute dari backend (via index.tsx)
  nodes: GeoNode[];               // Data lokasi dari backend (via index.tsx)
}

export default function TelkomMapView({
  selectedMode,
  fromNodeId,
  toNodeId,
  exactStartCoord,
  isNavigating = false,
  mapType,
  recenterSignal,
  routeResult,
  nodes,
}: Props) {
  // Hanya simpan state untuk Polygon batas kampus
  const [polygonCoords, setPolygonCoords] = useState<{ latitude: number; longitude: number }[]>([]);
  const mapRef = useRef<MapView>(null);

  // 1. Load statis untuk Polygon batas Tel-U (Opsional, jika file ada)
  useEffect(() => {
    try {
      const fullMapJson = require("@/assets/geojson/FullMap.json");
      let polygonFeature = null;
      if (fullMapJson.type === "FeatureCollection") {
        polygonFeature = fullMapJson.features.find((f: any) => f.geometry.type === "Polygon");
      } else if (fullMapJson.geometry && fullMapJson.geometry.type === "Polygon") {
        polygonFeature = fullMapJson;
      }

      if (polygonFeature && polygonFeature.geometry.coordinates) {
        const formattedCoords = polygonFeature.geometry.coordinates[0].map(
          ([lng, lat]: [number, number]) => ({
            latitude: lat,
            longitude: lng,
          })
        );
        setPolygonCoords(formattedCoords);
      }
    } catch (err) {
      console.warn("Polygon batas kampus tidak dimuat. Aman untuk diabaikan.");
    }
  }, []);

  // 2. Efek untuk Recenter kamera
  useEffect(() => {
    if (recenterSignal > 0) {
      mapRef.current?.animateToRegion(TELKOM_REGION, 1000);
    }
  }, [recenterSignal]);

  // 3. Efek untuk fokus kamera otomatis saat rute dari Backend diterima
  useEffect(() => {
    if (routeResult && mapRef.current) {
      const coords = routeResult.coordinates.map(([lng, lat]) => ({
        latitude: lat,
        longitude: lng,
      }));
      mapRef.current.fitToCoordinates(coords, {
        edgePadding: { top: 150, right: 50, bottom: 250, left: 50 },
        animated: true,
      });
    }
  }, [routeResult]);

  // Cari data Node untuk Start dan End berdasarkan ID yang dipilih
  const startNodeData = nodes.find((n) => n.id === fromNodeId);
  const endNodeData = nodes.find((n) => n.id === toNodeId);
  const actualStartCoord = exactStartCoord || (startNodeData ? startNodeData.coordinates : null);

  return (
    <View style={tw`flex-1`}>
      <MapView
        ref={mapRef}
        style={{ width, height }}
        provider={PROVIDER_GOOGLE}
        initialRegion={TELKOM_REGION}
        showsUserLocation
        showsMyLocationButton={false}
        showsCompass={false}
        showsScale={false}
        showsTraffic={false}
        showsBuildings={false}
        showsIndoors={false}
        showsIndoorLevelPicker={false}
        mapType={mapType}
        customMapStyle={mapStyle}
      >
        {/* Render Polygon Batas Area Kampus TelU */}
        {polygonCoords.length > 0 && (
          <Polygon
            coordinates={polygonCoords}
            fillColor="rgba(255, 0, 4, 0.10)"
            strokeColor={Colors.dark.maroon.primary}
            strokeWidth={2}
            geodesic={true}
            tappable={false}
          />
        )}

        {/* Render Semua Lokasi (Marker) dari Backend */}
        {!isNavigating &&
          nodes
            .filter(
              (node) =>
                node.id !== fromNodeId &&
                node.id !== toNodeId &&
                !node.id.toLowerCase().includes("intersection")
            )
            .map((node) => <CustomPinMarker key={node.id} node={node} />)}

        {/* Titik Awal (Current Location atau Node yang Dipilih) */}
        {actualStartCoord && (
          <Marker
            key="start-marker"
            coordinate={{
              latitude: actualStartCoord[1],
              longitude: actualStartCoord[0],
            }}
            anchor={{ x: 0.5, y: 0.5 }}
            zIndex={10}
          >
            <View style={tw`items-center justify-center`}>
              <View style={tw`w-[30px] h-[30px] rounded-full bg-[#2980b9]/25 justify-center items-center`}>
                <View style={tw`w-[18px] h-[18px] rounded-full bg-[#2980b9] border-[3px] border-white`} />
              </View>
            </View>
          </Marker>
        )}

        {/* --- DOTTED LINE 1: Titik GPS asli ke node rute (jika pakai Locate Me) --- */}
        {routeResult && exactStartCoord && routeResult.nodes && routeResult.nodes.length > 0 && (
          <Polyline
            coordinates={[
              { latitude: exactStartCoord[1], longitude: exactStartCoord[0] },
              {
                latitude: routeResult.nodes[0].coordinates[1],
                longitude: routeResult.nodes[0].coordinates[0],
              },
            ]}
            strokeWidth={3}
            strokeColor="#2980b9"
            lineDashPattern={[15, 15]}
            lineCap="butt"
          />
        )}

        {/* --- DOTTED LINE 2: Rute Jalan Kaki ke Tempat Kendaraan (Walk Start) --- */}
        {routeResult?.walkCoordinatesStart && (
          <Polyline
            key={`walk-start-${fromNodeId}-${toNodeId}-${selectedMode}`}
            coordinates={routeResult.walkCoordinatesStart.map(([lng, lat]) => ({
              latitude: lat,
              longitude: lng,
            }))}
            strokeWidth={3}
            strokeColor={Colors.dark.maroon.primary}
            lineDashPattern={[15, 15]}
            lineCap="butt"
          />
        )}

        {/* --- DOTTED LINE 3: Rute Jalan Kaki dari Parkir ke Gedung Tujuan (Walk End) --- */}
        {routeResult?.walkCoordinatesEnd && (
          <Polyline
            key={`walk-end-${fromNodeId}-${toNodeId}-${selectedMode}`}
            coordinates={routeResult.walkCoordinatesEnd.map(([lng, lat]) => ({
              latitude: lat,
              longitude: lng,
            }))}
            strokeWidth={3}
            strokeColor={Colors.dark.maroon.primary}
            lineDashPattern={[15, 15]}
            lineCap="butt"
          />
        )}

        {/* --- MAIN ROUTE: Garis Rute Utama --- */}
        {routeResult?.coordinates && (
          <Polyline
            key={`main-route-${fromNodeId}-${toNodeId}-${selectedMode}`}
            coordinates={routeResult.coordinates.map(([lng, lat]) => ({
              latitude: lat,
              longitude: lng,
            }))}
            strokeWidth={4}
            strokeColor={Colors.dark.maroon.primary}
            lineCap="butt"
            lineJoin="butt"
            lineDashPattern={
              selectedMode === "walk" || (selectedMode as any) === "pedestrian"
                ? [15, 15]
                : selectedMode === "motorcycle"
                ? [55, 15]
                : undefined
            }
          />
        )}

        {/* CUSTOM TITIK DESTINASI (TUJUAN) */}
        {endNodeData && (
          <Marker
            key="end-marker"
            coordinate={{
              latitude: endNodeData.coordinates[1],
              longitude: endNodeData.coordinates[0],
            }}
            anchor={{ x: 0.37, y: 0.95 }}
            zIndex={11}
          >
            <View style={tw`items-center justify-center`}>
              <View style={tw`w-[25px] h-[25px] rounded-full bg-[#7B1113] justify-center items-center border-2 border-white z-10`}>
                <Ionicons name="flag" size={12} color="#FFFFFF" />
              </View>
              <View style={tw`w-0 h-0 border-solid border-l-[6px] border-r-[6px] border-t-[10px] border-l-transparent border-r-transparent border-t-white mt-[-2px] z-0`} />
              <View style={tw`w-6 h-1.5 rounded-full bg-black/25 mt-0.5`} />
            </View>
          </Marker>
        )}
      </MapView>
    </View>
  );
}

const mapStyle = [
  {
    featureType: "poi",
    stylers: [{ visibility: "off" }],
  },
  {
    featureType: "transit",
    stylers: [{ visibility: "off" }],
  },
  {
    featureType: "administrative",
    stylers: [{ visibility: "off" }],
  },
  {
    featureType: "road",
    elementType: "labels",
    stylers: [{ visibility: "off" }],
  },
  {
    elementType: "labels",
    stylers: [{ visibility: "off" }],
  },
];