// Setup global untuk Jest. Dipanggil sekali sebelum suite jalan.

// Mock AsyncStorage native module dengan implementasi in-memory standar.
jest.mock('@react-native-async-storage/async-storage', () =>
  require('@react-native-async-storage/async-storage/jest/async-storage-mock'),
);

// expo-asset memerlukan native module yang tidak tersedia di Node;
// mock minimal yang cukup untuk service tests.
jest.mock('expo-asset', () => ({
  Asset: {
    fromModule: () => ({
      downloadAsync: () => Promise.resolve(),
      localUri: 'mocked://bundle.geojson',
      uri: 'mocked://bundle.geojson',
    }),
  },
}));

// expo-constants — kita butuh extra.apiBaseUrl di constants/env.ts.
jest.mock('expo-constants', () => ({
  __esModule: true,
  default: {
    expoConfig: {
      extra: {
        apiBaseUrl: 'http://test.local:8080',
      },
    },
  },
}));
