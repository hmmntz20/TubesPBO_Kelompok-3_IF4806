import { Link, Stack } from 'expo-router';
import { View, Text, StyleSheet } from 'react-native';

export default function NotFoundScreen() {
  return (
    <>
      <Stack.Screen options={{ title: 'Oops!' }} />
      <View style={styles.container}>
        <Text style={styles.text}>Halaman tidak ditemukan</Text>
        <Link href="/" style={styles.link}>
          <Text>Kembali ke Beranda</Text>
        </Link>
      </View>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
    backgroundColor: '#0A0A0A',
  },
  text: {
    fontSize: 20,
    fontWeight: '600',
    color: '#FFF',
  },
  link: {
    marginTop: 15,
    paddingVertical: 15,
    color: '#9B2528',
  },
});