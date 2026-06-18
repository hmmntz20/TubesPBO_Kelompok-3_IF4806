// File: types.ts

export type TravelMode = "pedestrian" | "motorcycle" | "car";

export interface GeoNode {
  id: string;
  name: string;      // Datang dari backend MainNode
  category: string;  // Datang dari backend MainNode
  coordinates: number[]; // [longitude, latitude]
}

export interface PathResult {
  totalDistance: number;
  totalDuration: number;
  nodes: GeoNode[];
  edges: any[];
  coordinates: number[][]; // Kumpulan titik polyline rute utama
  walkCoordinatesStart?: number[][]; // Garis putus-putus awal
  walkCoordinatesEnd?: number[][];   // Garis putus-putus akhir
  instructions: any[];
}