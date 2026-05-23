/**
 * Tab "Pengaturan" — pemilih tema + status akun (login Google).
 *
 * <p>Bagian akun (Fitur 1) ditampilkan di atas pemilih tema:</p>
 * <ul>
 *   <li>Belum login → tombol "Masuk dengan Google".</li>
 *   <li>Sudah login → email user + tombol "Keluar".</li>
 *   <li>Loading state pada keduanya saat operasi sedang berjalan.</li>
 *   <li>Error banner di-render bila operasi terakhir gagal.</li>
 * </ul>
 */
import { Pressable, ScrollView, StyleSheet, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { ErrorBanner } from '@/components/route/ErrorBanner';
import { ThemedText } from '@/components/themed-text';
import { GlassButton } from '@/components/ui/GlassButton';
import { GlassCard } from '@/components/ui/GlassCard';
import { isSupabaseConfigured } from '@/constants/env';
import { Radius, Spacing } from '@/constants/theme';
import { useAppTheme } from '@/hooks/use-app-theme';
import { useAuthStore } from '@/stores/auth-store';
import { useThemeStore, type ThemeMode } from '@/stores/theme-store';

const THEME_OPTIONS: { value: ThemeMode; label: string }[] = [
  { value: 'system', label: 'Sistem' },
  { value: 'light', label: 'Terang' },
  { value: 'dark', label: 'Gelap' },
];

export default function SettingsScreen() {
  const insets = useSafeAreaInsets();
  const { colors, preference } = useAppTheme();
  const setMode = useThemeStore((s) => s.setMode);

  return (
    <ScrollView
      style={styles.scroll}
      contentContainerStyle={[
        styles.content,
        {
          paddingTop: insets.top + Spacing.xl,
          paddingBottom: insets.bottom + 96 + Spacing.xl,
        },
      ]}
    >
      <ThemedText type="title" style={styles.title}>
        Pengaturan
      </ThemedText>

      <AccountSection />

      <ThemedText type="subtitle" style={styles.sectionLabel}>
        Tema
      </ThemedText>
      <ThemedText style={[styles.helper, { color: colors.textSecondary }]}>
        Pilih tampilan terang, gelap, atau ikuti pengaturan sistem.
      </ThemedText>

      <View style={[styles.segmentGroup, { borderColor: colors.border }]}>
        {THEME_OPTIONS.map((opt, idx) => {
          const active = preference === opt.value;
          return (
            <Pressable
              key={opt.value}
              accessibilityRole="button"
              accessibilityLabel={`Pilih tema ${opt.label}`}
              accessibilityState={{ selected: active }}
              onPress={() => setMode(opt.value)}
              style={[
                styles.segment,
                idx > 0 && { borderLeftWidth: StyleSheet.hairlineWidth, borderLeftColor: colors.border },
                active && { backgroundColor: colors.brandMaroon },
              ]}
            >
              <ThemedText
                style={[styles.segmentLabel, { color: active ? '#FFFFFF' : colors.text }]}
              >
                {opt.label}
              </ThemedText>
            </Pressable>
          );
        })}
      </View>
    </ScrollView>
  );
}

/**
 * Bagian akun: status login + aksi sign in/out + error banner.
 */
function AccountSection() {
  const { colors } = useAppTheme();
  const user = useAuthStore((s) => s.user);
  const loading = useAuthStore((s) => s.loading);
  const error = useAuthStore((s) => s.error);
  const signIn = useAuthStore((s) => s.signIn);
  const signOut = useAuthStore((s) => s.signOut);
  const clearError = useAuthStore((s) => s.clearError);

  const supabaseConfigured = isSupabaseConfigured();

  return (
    <View style={styles.accountWrapper}>
      <ThemedText type="subtitle" style={styles.sectionLabel}>
        Akun
      </ThemedText>

      {!supabaseConfigured ? (
        <GlassCard tone="regular">
          <ThemedText style={[styles.helper, { color: colors.textSecondary }]}>
            Konfigurasi Supabase belum lengkap. Salin{' '}
            <ThemedText style={[styles.code, { color: colors.text }]}>.env.example</ThemedText>{' '}
            ke{' '}
            <ThemedText style={[styles.code, { color: colors.text }]}>.env</ThemedText> dan isi{' '}
            <ThemedText style={[styles.code, { color: colors.text }]}>EXPO_PUBLIC_SUPABASE_URL</ThemedText>{' '}
            +{' '}
            <ThemedText style={[styles.code, { color: colors.text }]}>EXPO_PUBLIC_SUPABASE_ANON_KEY</ThemedText>.
          </ThemedText>
        </GlassCard>
      ) : user ? (
        <GlassCard tone="regular">
          <View style={styles.userRow}>
            <View style={styles.userInfo}>
              <ThemedText style={[styles.userLabel, { color: colors.textSecondary }]}>
                Masuk sebagai
              </ThemedText>
              <ThemedText style={[styles.userEmail, { color: colors.text }]}>
                {user.email || '(tanpa email)'}
              </ThemedText>
            </View>
            <GlassButton
              label={loading ? 'Keluar…' : 'Keluar'}
              variant="ghost"
              loading={loading}
              onPress={signOut}
            />
          </View>
        </GlassCard>
      ) : (
        <GlassCard tone="regular">
          <ThemedText style={[styles.helper, { color: colors.textSecondary, marginBottom: Spacing.md }]}>
            Masuk untuk menyimpan riwayat pencarian rute Anda.
          </ThemedText>
          <GlassButton
            label={loading ? 'Mengarahkan ke Google…' : 'Masuk dengan Google'}
            loading={loading}
            onPress={signIn}
          />
        </GlassCard>
      )}

      <ErrorBanner
        message={error}
        onDismiss={clearError}
        style={styles.errorBanner}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
  },
  content: {
    paddingHorizontal: Spacing.lg,
  },
  title: {
    marginBottom: Spacing.lg,
  },
  accountWrapper: {
    marginBottom: Spacing.xl,
    gap: Spacing.sm,
  },
  sectionLabel: {
    marginBottom: Spacing.xs,
  },
  helper: {
    marginBottom: Spacing.md,
    fontSize: 14,
    lineHeight: 20,
  },
  code: {
    fontFamily: 'monospace',
    fontSize: 13,
  },
  segmentGroup: {
    flexDirection: 'row',
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: Radius.lg,
    overflow: 'hidden',
  },
  segment: {
    flex: 1,
    paddingVertical: Spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  segmentLabel: {
    fontSize: 15,
    fontWeight: '600',
  },
  userRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.md,
  },
  userInfo: {
    flex: 1,
    gap: 2,
  },
  userLabel: {
    fontSize: 12,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    fontWeight: '600',
  },
  userEmail: {
    fontSize: 16,
    fontWeight: '600',
  },
  errorBanner: {
    marginTop: Spacing.sm,
  },
});
