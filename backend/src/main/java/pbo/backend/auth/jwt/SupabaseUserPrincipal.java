package pbo.backend.auth.jwt;

import java.security.Principal;
import java.util.Objects;

/**
 * Principal Spring Security yang merepresentasikan user terautentikasi
 * via Supabase Auth.
 *
 * <p>Hanya menyimpan dua field minimum yang dibutuhkan MVP — sesuai cakupan
 * spec Fitur 1: {@code id} (UUID dari klaim {@code sub}) dan {@code email}
 * (dari klaim {@code email}). Tidak menyimpan token mentah agar tidak
 * bocor lewat logging atau {@code toString}.</p>
 *
 * @param id    user id (UUID Supabase, klaim {@code sub}); non-null & tidak blank.
 * @param email email pengguna (klaim {@code email}); non-null (boleh string kosong
 *              kalau OAuth provider tidak menyertakan email — defensif).
 */
public record SupabaseUserPrincipal(String id, String email) implements Principal {

    public SupabaseUserPrincipal {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(email, "email");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id tidak boleh blank");
        }
    }

    /**
     * @return identifier untuk Spring Security (= {@link #id()}).
     *         Spring memakai {@code Principal.getName()} di banyak tempat.
     */
    @Override
    public String getName() {
        return id;
    }
}
