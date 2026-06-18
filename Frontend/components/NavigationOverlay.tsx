import React, { useEffect, useState, useRef } from 'react';
import tw from 'twrnc';
import {
  View,
  Text,
  TouchableOpacity,
  Animated,
  Dimensions,
  Platform,
  Easing
} from 'react-native';
import { Ionicons, FontAwesome5 } from '@expo/vector-icons';
import * as Location from 'expo-location';
import { PathResult, TravelMode } from '@/config/types'; // Pastikan path import benar

const { width } = Dimensions.get('window');

interface Props {
  isVisible: boolean;
  pathResult: PathResult | null;
  mode: TravelMode;
  onStopNavigation: () => void;
  onArrive: () => void;
}

// Menghitung jarak (dalam meter) antara posisi User dan Waypoint berikutnya
const getDistance = (coord1: [number, number], coord2: [number, number]) => {
  const R = 6371e3;
  const lat1 = (coord1[1] * Math.PI) / 180;
  const lat2 = (coord2[1] * Math.PI) / 180;
  const deltaLat = ((coord2[1] - coord1[1]) * Math.PI) / 180;
  const deltaLon = ((coord2[0] - coord1[0]) * Math.PI) / 180;

  const a =
    Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
  return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
};

export default function NavigationOverlay({
  isVisible,
  pathResult,
  mode,
  onStopNavigation,
  onArrive
}: Props) {
  const [currentStep, setCurrentStep] = useState(0);
  const slideAnim = useRef(new Animated.Value(-300)).current;

  // Instruksi sekarang datang langsung dari Backend!
  const instructions = pathResult?.instructions || [];

  useEffect(() => {
    let locationSubscription: Location.LocationSubscription | null = null;
    let hasArrived = false; // Flag agar tidak trigger berkali-kali

    const startRealtimeNavigation = async () => {
      if (isVisible && pathResult && instructions.length > 0) {
        setCurrentStep(0);
        
        Animated.timing(slideAnim, {
          toValue: 0,
          duration: 500,
          easing: Easing.out(Easing.cubic),
          useNativeDriver: true,
        }).start();

        const { status } = await Location.requestForegroundPermissionsAsync();
        if (status !== 'granted') return;

        locationSubscription = await Location.watchPositionAsync(
          {
            accuracy: Location.Accuracy.BestForNavigation,
            timeInterval: 2000, 
            distanceInterval: 2, 
          },
          (location) => {
            if (hasArrived) return; // Hentikan tracking jika sudah sampai

            const userCoord: [number, number] = [location.coords.longitude, location.coords.latitude];

            // --- DETEKSI SUDAH SAMPAI (Jarak ke titik paling akhir < 20 meter) ---
            const finalInst = instructions[instructions.length - 1];
            const distanceToFinal = getDistance(userCoord, finalInst.coordinate as [number, number]);

            if (distanceToFinal < 20) {
              hasArrived = true;
              onArrive(); // Memicu fungsi selesai dari index.tsx!
              return;
            }

            // --- ADVANCE KE STEP BERIKUTNYA ---
            setCurrentStep((prevStep) => {
              if (prevStep >= instructions.length - 1) return prevStep;
              const nextInst = instructions[prevStep + 1];
              const distanceToNext = getDistance(userCoord, nextInst.coordinate as [number, number]);

              if (distanceToNext < 15) {
                return prevStep + 1;
              }
              return prevStep;
            });
          }
        );
      } else {
        Animated.timing(slideAnim, {
          toValue: -300,
          duration: 300,
          useNativeDriver: true,
        }).start();
      }
    };

    startRealtimeNavigation();

    return () => {
      if (locationSubscription) locationSubscription.remove();
    };
  }, [isVisible, pathResult]);

  const formatDistance = (meters: number): string => {
    if (meters < 1000) return `${Math.round(meters)} m`;
    return `${(meters / 1000).toFixed(1)} km`;
  };

  const formatDuration = (seconds: number): string => {
    const mins = Math.round(seconds / 60);
    if (mins < 60) return `${mins} mnt`;
    const hours = Math.floor(mins / 60);
    return `${hours}j ${mins % 60}m`;
  };

  const getInstructionIcon = (type: string) => {
    switch(type) {
      case 'turn_left': return 'arrow-undo';
      case 'turn_right': return 'arrow-redo';
      case 'uturn': return 'return-up-back';
      case 'arrive': return 'flag';
      default: return 'arrow-up'; // straight
    }
  };

  const currentInstruction = instructions[currentStep];
  
  // Hitung sisa jarak & waktu dari langkah saat ini sampai tujuan
  const remainingDistance = instructions.slice(currentStep).reduce((sum, inst) => sum + (inst.distance || 0), 0);
  const remainingDuration = instructions.slice(currentStep).reduce((sum, inst) => sum + (inst.duration || 0), 0);

  if (!isVisible || !currentInstruction) return null;

  // Sanitasi visual mode
  const actualMode = (mode as any) === 'walk' ? 'pedestrian' : mode;

  // Shadow tetap didefinisikan terpisah untuk kompatibilitas native cross-platform
  const navCardShadow = Platform.select({
    ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 8 }, shadowOpacity: 0.3, shadowRadius: 16 },
    android: { elevation: 10 },
  });

  return (
    <Animated.View style={[tw`absolute top-[50px] left-4 right-4 z-[100]`, { transform: [{ translateY: slideAnim }] }]}>
      <View style={[tw`bg-[#7B1113] rounded-[24px] p-5`, navCardShadow]}>
        
        {/* Turn Indicator */}
        <View style={tw`flex-row items-center mb-4`}>
          <View style={tw`w-14 h-14 rounded-full bg-white/20 justify-center items-center mr-4`}>
            <Ionicons name={getInstructionIcon(currentInstruction.type)} size={32} color="#FFF" />
          </View>
          <View style={tw`flex-1`}>
            <Text style={tw`text-white text-lg font-bold mb-1`}>{currentInstruction.text}</Text>
            {currentInstruction.type !== 'arrive' && (
              <Text style={tw`text-white/80 text-2xl font-extrabold`}>{formatDistance(currentInstruction.distance)}</Text>
            )}
          </View>
        </View>

        {/* Progress Bar */}
        <View style={tw`h-1 bg-white/20 rounded-sm mb-4 overflow-hidden`}>
          <View style={[
            tw`h-full bg-white rounded-sm`,
            { width: `${((currentStep / Math.max(instructions.length - 1, 1)) * 100)}%` }
          ]} />
        </View>

        {/* Stats Row */}
        <View style={tw`flex-row justify-around`}>
          <View style={tw`flex-row items-center gap-1.5`}>
            <Ionicons name="time-outline" size={18} color="#FFF" />
            <Text style={tw`text-white text-sm font-semibold`}>{formatDuration(remainingDuration)}</Text>
          </View>
          <View style={tw`flex-row items-center gap-1.5`}>
            <Ionicons name="navigate-outline" size={18} color="#FFF" />
            <Text style={tw`text-white text-sm font-semibold`}>{formatDistance(remainingDistance)}</Text>
          </View>
          <View style={tw`flex-row items-center gap-1.5`}>
            <FontAwesome5 
              name={actualMode === 'pedestrian' ? 'walking' : actualMode === 'motorcycle' ? 'motorcycle' : 'car'} 
              size={16} 
              color="#FFF" 
            />
            <Text style={tw`text-white text-sm font-semibold`}>
              {actualMode === 'pedestrian' ? 'Walk' : actualMode === 'motorcycle' ? 'Moto' : 'Car'}
            </Text>
          </View>
        </View>
      </View>

      <TouchableOpacity 
        style={tw`flex-row items-center justify-center self-center mt-4 bg-black/50 py-2.5 px-5 rounded-[20px] gap-2`} 
        onPress={onStopNavigation}
      >
        <Ionicons name="close-circle" size={20} color="#FFF" />
        <Text style={tw`text-white text-sm font-semibold`}>End Navigation</Text>
      </TouchableOpacity>
    </Animated.View>
  );
}