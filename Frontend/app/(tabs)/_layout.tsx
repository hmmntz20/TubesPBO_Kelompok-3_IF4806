import { Stack } from "expo-router";
import { createContext, useContext, useState } from "react";

import NavigationBar from "@/components/NavigationBar";

export const TabBarContext = createContext({
  isTabBarVisible: true,
  setIsTabBarVisible: (visible: boolean) => {},
});

export const useTabBar = () => useContext(TabBarContext);

export default function TabLayout() {
  const [isTabBarVisible, setIsTabBarVisible] = useState(true);

  return (
    <TabBarContext.Provider
      value={{
        isTabBarVisible,
        setIsTabBarVisible,
      }}
    >
      <>
        <Stack
          screenOptions={{
            headerShown: false,
          }}
        >
          <Stack.Screen name="index" />
          <Stack.Screen name="history" />
          <Stack.Screen name="profile" />
        </Stack>

        {isTabBarVisible && <NavigationBar />}
      </>
    </TabBarContext.Provider>
  );
}