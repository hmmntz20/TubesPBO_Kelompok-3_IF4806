import { Link } from 'expo-router';
import { StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';

export default function ModalScreen() {
  return (
    <ThemedView style={styles.container}>
      <ThemedText type="title">This is a modal</ThemedText>

      {/*
        Sample NativeWind: memverifikasi bahwa class `bg-brand-maroon` &
        `rounded-glass` dari tailwind.config.js bekerja.
        Akan dihapus saat TASK-FE-MAP-04 mengganti modal dengan modal sungguhan.
      */}
      <View
        accessibilityLabel="brand-color-sample"
        className="bg-brand-maroon rounded-glass mt-4 px-6 py-3"
      >
        <ThemedText style={styles.sampleText}>Brand sample</ThemedText>
      </View>

      <Link href="/" dismissTo style={styles.link}>
        <ThemedText type="link">Go to home screen</ThemedText>
      </Link>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  link: {
    marginTop: 15,
    paddingVertical: 15,
  },
  sampleText: {
    color: 'white',
  },
});
