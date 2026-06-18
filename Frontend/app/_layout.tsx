import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { SafeAreaProvider } from "react-native-safe-area-context";

export default function RootLayout() {
  return (
    <SafeAreaProvider>
      <StatusBar style="dark" />

      <Stack
        screenOptions={{
          headerShown: false,
        }}
      >
        <Stack.Screen name="login" />

        <Stack.Screen name="register" />

        <Stack.Screen name="(tabs)" />

        <Stack.Screen
          name="+not-found"
          options={{
            title: "Oops!",
          }}
        />
      </Stack>
    </SafeAreaProvider>
  );
}