import React from 'react';
import { View, Platform} from 'react-native';
import tw from 'twrnc';
import { useColorScheme } from '../hooks/use-color-scheme';

interface Props {
  children: React.ReactNode;
  style?: any;
  dark?: boolean;
}

export default function GlassmorphismCard({ children, style, dark = false }: Props) {
  const colorScheme = useColorScheme() ?? 'dark';
  const isDark = dark || colorScheme === 'dark';

  const shadowStyle = Platform.select({
    ios: {
      shadowColor: '#000',
      shadowOffset: { width: 0, height: 8 },
      shadowOpacity: 0.15,
      shadowRadius: 20,
    },
    android: {
      elevation: 8,
    },
  });

  return (
    <View style={[
      tw`rounded-[20px] bg-blue-200 border overflow-hidden`,
      isDark ? tw`bg-[#1E1E1E]/65 border-white/10` : tw`bg-red/15 border-white/25`,
      shadowStyle,
      style
    ]}>
      {children}
    </View>
  );
}