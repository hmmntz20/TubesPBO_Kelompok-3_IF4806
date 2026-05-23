#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────
# backend/scripts/set-env.example.sh
#
# Template untuk men-export environment variable yang dibutuhkan backend
# saat run lokal (dev).
#
# CARA PAKAI:
#   1. Salin file ini menjadi `backend/scripts/set-env.sh` (sudah .gitignore).
#   2. Edit nilai sesuai kredensial Supabase Anda.
#   3. Sebelum menjalankan backend:
#        source backend/scripts/set-env.sh
#        cd backend && ./mvnw spring-boot:run
#
# JANGAN commit `set-env.sh` — file itu berisi rahasia.
# ──────────────────────────────────────────────────────────────────────────

# ── Database (Supabase Postgres) ──
# Ambil di: Supabase dashboard → Project Settings → Database → Connection info.
# Format host: db.<project-ref>.supabase.co
export DB_HOST="db.your-project-ref.supabase.co"
export DB_PORT="5432"
export DB_NAME="postgres"
export DB_USER="postgres"
export DB_PASSWORD="your-supabase-db-password"

# ── Supabase Auth ──
# JWT secret: Supabase dashboard → Project Settings → API → "JWT Secret".
# WAJIB rahasia — beda dengan anon key.
export SUPABASE_JWT_SECRET="your-supabase-jwt-secret-256bit-min"

# Project ref (opsional, untuk validasi 'iss' nanti).
export SUPABASE_PROJECT_REF="your-project-ref"

echo "[set-env] Backend env loaded:"
echo "  DB_HOST            = $DB_HOST"
echo "  DB_NAME            = $DB_NAME"
echo "  SUPABASE_JWT_SECRET= $([ -n "$SUPABASE_JWT_SECRET" ] && echo '<set>' || echo '<empty>')"
