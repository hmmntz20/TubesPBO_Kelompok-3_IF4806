/**
 * Unit test untuk {@code auth-service}.
 *
 * <p>Mock-strategi: kita mock modul {@code @/services/supabase-client}
 * dan {@code expo-web-browser} sehingga tidak ada call jaringan & tidak
 * bergantung pada env. Setiap skenario menyetel mock yang dibutuhkan.</p>
 */
import {
  AuthCancelledError,
  AuthConfigError,
  AuthError,
  type AuthUser,
} from '@/types/auth';

// ── Mocks ──────────────────────────────────────────────────────────────

const mockSignInWithOAuth = jest.fn();
const mockSignOut = jest.fn();
const mockGetSession = jest.fn();
const mockSetSession = jest.fn();
const mockOpenAuthSessionAsync = jest.fn();

jest.mock('@/services/supabase-client', () => ({
  supabase: {
    auth: {
      signInWithOAuth: (...args: unknown[]) => mockSignInWithOAuth(...args),
      signOut: () => mockSignOut(),
      getSession: () => mockGetSession(),
      setSession: (...args: unknown[]) => mockSetSession(...args),
    },
  },
}));

jest.mock('expo-web-browser', () => ({
  openAuthSessionAsync: (...args: unknown[]) => mockOpenAuthSessionAsync(...args),
}));

jest.mock('expo-linking', () => ({
  createURL: (path: string) => `telkomroute://${path.replace(/^\/+/, '')}`,
}));

// Mock konfigurasi Supabase agar terasa sudah lengkap.
jest.mock('@/constants/env', () => ({
  env: {
    apiBaseUrl: 'http://test.local:8080',
    supabaseUrl: 'https://test.supabase.co',
    supabaseAnonKey: 'test-anon-key',
  },
  isSupabaseConfigured: () => true,
}));

// Import setelah mocks agar service memakai modul ter-mock.
// eslint-disable-next-line @typescript-eslint/no-require-imports
const authService = require('@/services/auth-service') as typeof import('@/services/auth-service');

beforeEach(() => {
  jest.clearAllMocks();
});

describe('auth-service.signInWithGoogle', () => {
  it('happy path: terima URL → buka browser → setSession → kembalikan AuthUser', async () => {
    mockSignInWithOAuth.mockResolvedValueOnce({
      data: { url: 'https://test.supabase.co/auth/v1/authorize?provider=google' },
      error: null,
    });
    mockOpenAuthSessionAsync.mockResolvedValueOnce({
      type: 'success',
      url: 'telkomroute://auth/callback#access_token=abc&refresh_token=def&expires_in=3600',
    });
    mockSetSession.mockResolvedValueOnce({
      data: { user: { id: 'uuid-1', email: 'alice@example.com' }, session: {} },
      error: null,
    });

    const user: AuthUser = await authService.signInWithGoogle();

    expect(user).toEqual({ id: 'uuid-1', email: 'alice@example.com' });
    expect(mockSignInWithOAuth).toHaveBeenCalledWith({
      provider: 'google',
      options: expect.objectContaining({ skipBrowserRedirect: true }),
    });
    expect(mockSetSession).toHaveBeenCalledWith({
      access_token: 'abc',
      refresh_token: 'def',
    });
  });

  it('user batal di browser → AuthCancelledError', async () => {
    mockSignInWithOAuth.mockResolvedValueOnce({
      data: { url: 'https://test.supabase.co/auth/v1/authorize?provider=google' },
      error: null,
    });
    mockOpenAuthSessionAsync.mockResolvedValueOnce({ type: 'cancel' });

    await expect(authService.signInWithGoogle()).rejects.toBeInstanceOf(AuthCancelledError);
    expect(mockSetSession).not.toHaveBeenCalled();
  });

  it('Supabase signInWithOAuth error → AuthError', async () => {
    mockSignInWithOAuth.mockResolvedValueOnce({
      data: null,
      error: { message: 'oauth provider unavailable' },
    });

    await expect(authService.signInWithGoogle()).rejects.toBeInstanceOf(AuthError);
  });

  it('redirect URL tanpa token → AuthError', async () => {
    mockSignInWithOAuth.mockResolvedValueOnce({
      data: { url: 'https://test.supabase.co/...' },
      error: null,
    });
    mockOpenAuthSessionAsync.mockResolvedValueOnce({
      type: 'success',
      url: 'telkomroute://auth/callback', // tidak ada fragment
    });

    await expect(authService.signInWithGoogle()).rejects.toThrow(/Token tidak ditemukan/);
  });
});

describe('auth-service.signOut', () => {
  it('memanggil supabase.auth.signOut', async () => {
    mockSignOut.mockResolvedValueOnce({ error: null });
    await authService.signOut();
    expect(mockSignOut).toHaveBeenCalled();
  });

  it('error dari Supabase di-bungkus AuthError', async () => {
    mockSignOut.mockResolvedValueOnce({ error: { message: 'session not found' } });
    await expect(authService.signOut()).rejects.toBeInstanceOf(AuthError);
  });
});

describe('auth-service.getCurrentUser & getAccessToken', () => {
  it('mengembalikan null ketika tidak ada sesi', async () => {
    mockGetSession.mockResolvedValueOnce({ data: { session: null }, error: null });
    expect(await authService.getCurrentUser()).toBeNull();

    mockGetSession.mockResolvedValueOnce({ data: { session: null }, error: null });
    expect(await authService.getAccessToken()).toBeNull();
  });

  it('mengembalikan user + token ketika sesi ada', async () => {
    mockGetSession.mockResolvedValueOnce({
      data: {
        session: {
          access_token: 'jwt-xyz',
          user: { id: 'uuid-2', email: 'bob@example.com' },
        },
      },
      error: null,
    });
    expect(await authService.getCurrentUser()).toEqual({
      id: 'uuid-2',
      email: 'bob@example.com',
    });

    mockGetSession.mockResolvedValueOnce({
      data: {
        session: {
          access_token: 'jwt-xyz',
          user: { id: 'uuid-2', email: 'bob@example.com' },
        },
      },
      error: null,
    });
    expect(await authService.getAccessToken()).toBe('jwt-xyz');
  });
});

describe('auth-service config guard', () => {
  it('AuthConfigError saat env Supabase belum lengkap', async () => {
    jest.resetModules();
    jest.doMock('@/constants/env', () => ({
      env: { apiBaseUrl: '', supabaseUrl: '', supabaseAnonKey: '' },
      isSupabaseConfigured: () => false,
    }));
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const fresh = require('@/services/auth-service') as typeof import('@/services/auth-service');

    // Pakai cek nama (bukan instanceof) karena resetModules membuat salinan
    // kelas baru di modul fresh — instanceof antar-modul akan false.
    await expect(fresh.signInWithGoogle()).rejects.toMatchObject({
      name: 'AuthConfigError',
    });
  });
});
