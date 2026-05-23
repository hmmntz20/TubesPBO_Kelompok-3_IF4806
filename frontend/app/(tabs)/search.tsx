/**
 * Tab "Cari" — layar pencarian rute (FR-UI-RT-01..03, FR-UI-RT-07).
 *
 * <h3>Flow utama</h3>
 * <ol>
 *   <li>Pengguna memilih moda via {@link ModeSelector}.</li>
 *   <li>Tap "Pilih di peta" pada kartu Asal/Tujuan → set
 *       {@code picking} di store + navigasi ke tab Peta.</li>
 *   <li>Setelah kembali, tombol "Cari Rute" aktif bila kedua titik terisi.</li>
 *   <li>Tekan "Cari Rute" → panggil {@code routing-service.findRoute},
 *       set {@code currentRoute}, navigasi ke tab Peta untuk menampilkan
 *       polyline + bottom sheet.</li>
 * </ol>
 *
 * <h3>Error UX</h3>
 * <p>Tiga jenis error ditangani via {@link ErrorBanner} in-place:
 * {@code RouteNotFoundError}, {@code RouteValidationError},
 * {@code RouteNetworkError}/{@code RouteUnavailableError}. Pesan dalam
 * Bahasa Indonesia mengikuti FR-UI-RT-07.</p>
 */
import { router } from 'expo-router';
import { ScrollView, StyleSheet, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { ErrorBanner } from '@/components/route/ErrorBanner';
import { ModeSelector } from '@/components/route/ModeSelector';
import { ThemedText } from '@/components/themed-text';
import { GlassButton } from '@/components/ui/GlassButton';
import { GlassCard } from '@/components/ui/GlassCard';
import { Spacing } from '@/constants/theme';
import { useAppTheme } from '@/hooks/use-app-theme';
import { findRoute } from '@/services/routing-service';
import { useRouteStore } from '@/stores/route-store';
import {
  RouteNetworkError,
  RouteNotFoundError,
  RouteUnavailableError,
  RouteValidationError,
  RoutingError,
  type TransportMode,
} from '@/types/route';
import type { LatLng } from 'react-native-maps';

export default function SearchScreen() {
  const insets = useSafeAreaInsets();

  // Selector spesifik agar render seminimal mungkin.
  const mode = useRouteStore((s) => s.mode);
  const pendingFrom = useRouteStore((s) => s.pendingFrom);
  const pendingTo = useRouteStore((s) => s.pendingTo);
  const loading = useRouteStore((s) => s.loading);
  const error = useRouteStore((s) => s.error);

  const setMode = useRouteStore((s) => s.setMode);
  const setPicking = useRouteStore((s) => s.setPicking);
  const swapPending = useRouteStore((s) => s.swapPending);
  const setLoading = useRouteStore((s) => s.setLoading);
  const setError = useRouteStore((s) => s.setError);
  const setCurrentRoute = useRouteStore((s) => s.setCurrentRoute);

  const canSearch = !!(pendingFrom && pendingTo) && !loading;

  const handlePick = (which: 'from' | 'to') => {
    setPicking(which);
    router.navigate('/(tabs)');
  };

  const handleSearch = async () => {
    if (!pendingFrom || !pendingTo) return;
    setError(null);
    setLoading(true);
    try {
      const route = await findRoute({ from: pendingFrom, to: pendingTo, mode });
      setCurrentRoute(route);
      router.navigate('/(tabs)');
    } catch (cause) {
      setError(toFriendlyError(cause));
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView
      style={styles.scroll}
      contentContainerStyle={[
        styles.content,
        {
          paddingTop: insets.top + Spacing.xl,
          paddingBottom: insets.bottom + 96 + Spacing.xl, // ruang tab bar
        },
      ]}
      keyboardShouldPersistTaps="handled"
    >
      <ThemedText type="title" style={styles.heading}>
        Cari Rute
      </ThemedText>
      <ThemedText style={styles.subheading}>
        Pilih moda, tentukan asal & tujuan dengan tap di peta.
      </ThemedText>

      <ModeSelector value={mode} onChange={(m: TransportMode) => setMode(m)} style={styles.section} />

      <PointCard
        title="Asal"
        coord={pendingFrom}
        onPick={() => handlePick('from')}
        style={styles.section}
      />

      <PointCard
        title="Tujuan"
        coord={pendingTo}
        onPick={() => handlePick('to')}
        style={styles.section}
      />

      {pendingFrom && pendingTo ? (
        <GlassButton
          label="Tukar Asal & Tujuan"
          variant="ghost"
          onPress={swapPending}
          style={styles.section}
        />
      ) : null}

      <GlassButton
        label={loading ? 'Mencari…' : 'Cari Rute'}
        loading={loading}
        disabled={!canSearch}
        onPress={handleSearch}
        style={styles.searchButton}
      />

      <ErrorBanner
        message={error?.message ?? null}
        onDismiss={() => setError(null)}
        style={styles.section}
      />
    </ScrollView>
  );
}

interface PointCardProps {
  title: string;
  coord: LatLng | null;
  onPick: () => void;
  style?: import('react-native').StyleProp<import('react-native').ViewStyle>;
}

function PointCard({ title, coord, onPick, style }: PointCardProps) {
  const { colors } = useAppTheme();
  const filled = coord != null;
  return (
    <GlassCard tone="regular" style={style}>
      <View style={styles.cardRow}>
        <View style={styles.cardText}>
          <ThemedText style={[styles.cardTitle, { color: colors.text }]}>{title}</ThemedText>
          {filled ? (
            <ThemedText style={[styles.cardCoord, { color: colors.textSecondary }]}>
              {coord!.latitude.toFixed(5)}, {coord!.longitude.toFixed(5)}
            </ThemedText>
          ) : (
            <ThemedText style={[styles.cardHint, { color: colors.textSecondary }]}>
              Belum dipilih
            </ThemedText>
          )}
        </View>
        <GlassButton
          label={filled ? 'Ganti' : 'Pilih di peta'}
          variant={filled ? 'ghost' : 'primary'}
          onPress={onPick}
        />
      </View>
    </GlassCard>
  );
}

/**
 * Memetakan error dari {@code routing-service} ke pesan friendly Bahasa
 * Indonesia (FR-UI-RT-07). Mengembalikan instance {@link Error}-compatible
 * agar {@link ErrorBanner} bisa membaca {@code .message}.
 */
function toFriendlyError(cause: unknown): Error {
  if (cause instanceof RouteNotFoundError) {
    return new Error('Tidak ada rute untuk moda ini. Coba moda lain atau titik berbeda.');
  }
  if (cause instanceof RouteValidationError) {
    return new Error(`Permintaan tidak valid: ${cause.message}`);
  }
  if (cause instanceof RouteUnavailableError) {
    return new Error('Server peta belum siap. Silakan coba beberapa saat lagi.');
  }
  if (cause instanceof RouteNetworkError) {
    return new Error('Tidak dapat terhubung ke server. Periksa koneksi internet.');
  }
  if (cause instanceof RoutingError) {
    return new Error(cause.message);
  }
  return new Error('Terjadi kesalahan yang tidak terduga.');
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
  },
  content: {
    paddingHorizontal: Spacing.lg,
    gap: Spacing.md,
  },
  heading: {
    fontSize: 28,
    lineHeight: 34,
    marginBottom: Spacing.xs,
  },
  subheading: {
    fontSize: 14,
    lineHeight: 20,
    marginBottom: Spacing.lg,
    opacity: 0.75,
  },
  section: {
    // Spacing antar-section di-handle oleh content.gap.
  },
  searchButton: {
    marginTop: Spacing.sm,
  },
  cardRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.md,
  },
  cardText: {
    flex: 1,
    gap: 2,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '700',
  },
  cardCoord: {
    fontSize: 13,
    fontVariant: ['tabular-nums'],
  },
  cardHint: {
    fontSize: 13,
    fontStyle: 'italic',
  },
});
