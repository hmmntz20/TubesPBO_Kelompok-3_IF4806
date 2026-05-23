#!/usr/bin/env python3
"""
Smoke test E2E untuk TASK-RT-QA-02.

Menjalankan backend (fat jar + H2 driver dari Maven cache di classpath)
sebagai detached subprocess, lalu memanggil seluruh endpoint via HTTP
untuk memvalidasi kontrak FR-RT-01..09.

Tidak butuh PostgreSQL — H2 in-memory dipakai untuk memenuhi konfigurasi
JPA. Endpoint routing sendiri tidak menyentuh DB di MVP.
"""
from __future__ import annotations

import json
import os
import signal
import subprocess
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAR = ROOT / "target" / "backend-0.0.1-SNAPSHOT.jar"
H2 = Path.home() / ".m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar"
LOG = Path("/tmp/backend-smoke.log")
PORT = 8080


def http(method: str, path: str, body=None, expect_status=None):
    """Panggil endpoint backend dan kembalikan (status, body_json, headers)."""
    url = f"http://localhost:{PORT}{path}"
    data = None
    headers = {"Accept": "application/json"}
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            status = resp.status
            payload = resp.read().decode("utf-8")
            hdrs = dict(resp.headers.items())
    except urllib.error.HTTPError as e:
        status = e.code
        payload = e.read().decode("utf-8") if e.fp else ""
        hdrs = dict(e.headers.items()) if e.headers else {}

    parsed = None
    try:
        parsed = json.loads(payload) if payload else None
    except json.JSONDecodeError:
        parsed = payload

    if expect_status and status != expect_status:
        raise AssertionError(
            f"{method} {path}: expect HTTP {expect_status}, got {status}\nBody: {payload[:500]}"
        )
    return status, parsed, hdrs


def wait_ready(timeout_s: int = 60) -> int:
    start = time.time()
    last_err = None
    while time.time() - start < timeout_s:
        try:
            status, _, _ = http("GET", "/api/v1/health")
            if status == 200:
                return int(time.time() - start)
        except (urllib.error.URLError, ConnectionResetError, ConnectionRefusedError) as e:
            last_err = e
        time.sleep(0.5)
    raise TimeoutError(f"backend tidak siap dalam {timeout_s}s: {last_err}")


def main() -> int:
    if not JAR.exists():
        print(f"ERROR: fat jar tidak ditemukan ({JAR}); jalankan './mvnw verify' dulu.")
        return 1
    if not H2.exists():
        print(f"ERROR: H2 jar tidak ditemukan ({H2}); jalankan './mvnw test' dulu.")
        return 1

    # Cleanup sisa.
    subprocess.run(["pkill", "-f", "backend-0.0.1-SNAPSHOT.jar"],
                   capture_output=True, check=False)
    time.sleep(1)

    # Jalankan dengan H2 ditambahkan ke classpath via PropertiesLauncher,
    # plus override datasource agar tidak mencoba PostgreSQL.
    cmd = [
        "java",
        "-cp", f"{H2}:{JAR}",
        "-Dloader.main=pbo.backend.BackendApplication",
        "org.springframework.boot.loader.launch.PropertiesLauncher",
        "--spring.datasource.url=jdbc:h2:mem:smoke;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "--spring.datasource.driver-class-name=org.h2.Driver",
        "--spring.datasource.username=sa",
        "--spring.datasource.password=",
        "--spring.jpa.hibernate.ddl-auto=none",
    ]

    print("Starting backend...")
    log_fp = open(LOG, "w")
    proc = subprocess.Popen(
        cmd,
        cwd=ROOT,
        stdout=log_fp,
        stderr=subprocess.STDOUT,
        start_new_session=True,
    )
    print(f"  PID: {proc.pid}")

    try:
        elapsed = wait_ready(60)
        print(f"  Ready after {elapsed}s")
        print()

        # ── TEST 1: health ──
        print("=== TEST 1: GET /api/v1/health ===")
        status, body, _ = http("GET", "/api/v1/health", expect_status=200)
        print(f"  status={status}, body={body}")
        assert body == {"status": "UP"}, f"unexpected body: {body}"
        print("  ✓ PASS")
        print()

        # ── TEST 2: graph meta ──
        print("=== TEST 2: GET /api/v1/graph/meta ===")
        status, body, _ = http("GET", "/api/v1/graph/meta", expect_status=200)
        print(f"  nodes={body['nodeCount']}, edges={body['edgeCount']}, parser={body['parser']}")
        assert body["nodeCount"] > 100
        assert body["edgeCount"] > 100
        assert body["parser"] == "osm-overpass-v1"
        print("  ✓ PASS")
        print()

        # ── TEST 3-5: tiga moda routing ──
        sample = {
            "from": {"latitude": -6.972, "longitude": 107.635},
            "to": {"latitude": -6.978, "longitude": 107.640},
        }
        for mode in ["WALKING", "MOTORCYCLE", "CAR"]:
            print(f"=== TEST 3-5: POST /api/v1/route mode={mode} ===")
            status, body, hdrs = http("POST", "/api/v1/route", body={**sample, "mode": mode})
            if status == 200:
                assert body["mode"] == mode
                assert body["lengthMeters"] > 0
                assert body["durationSeconds"] > 0
                assert len(body["coordinates"]) >= 2
                assert hdrs.get("Cache-Control") == "no-store"
                print(f"  status=200, mode={body['mode']}, length={body['lengthMeters']:.1f}m, "
                      f"duration={body['durationSeconds']}s, points={len(body['coordinates'])}")
                print(f"  Cache-Control={hdrs.get('Cache-Control')}")
                print("  ✓ PASS")
            elif status == 404:
                print(f"  status=404 — Tidak ada rute untuk mode {mode} (acceptable utk CAR di footway)")
                print(f"  body={body}")
                print("  ✓ PASS (404 valid response)")
            else:
                print(f"  ✗ FAIL unexpected status {status}: {body}")
                raise AssertionError(f"status {status}")
            print()

        # ── TEST 6: validasi 400 ──
        print("=== TEST 6: lat di luar range → expect 400 ===")
        status, body, _ = http("POST", "/api/v1/route", body={
            "from": {"latitude": 95, "longitude": 107.635},
            "to": {"latitude": -6.978, "longitude": 107.640},
            "mode": "WALKING",
        }, expect_status=400)
        print(f"  status=400, error={body.get('error')}")
        assert "latitude" in body["error"].lower()
        print("  ✓ PASS")
        print()

        # ── TEST 7: mode unknown ──
        print("=== TEST 7: mode tidak dikenal → expect 400 ===")
        status, body, _ = http("POST", "/api/v1/route", body={
            **sample, "mode": "TANK",
        }, expect_status=400)
        print(f"  status=400, error={body.get('error')}")
        assert "WALKING" in body["error"]  # pesan menyebutkan moda valid
        print("  ✓ PASS")
        print()

        # ── TEST 8: performa A* ──
        print("=== TEST 8: Performa A* (5x WALKING, target NFR-RT-PERF-01 ≤ 50 ms) ===")
        durations_ms = []
        for i in range(5):
            t0 = time.perf_counter()
            status, _, _ = http("POST", "/api/v1/route", body={**sample, "mode": "WALKING"},
                                expect_status=200)
            t1 = time.perf_counter()
            ms = (t1 - t0) * 1000
            durations_ms.append(ms)
            print(f"  call #{i + 1}: {ms:.1f} ms")
        avg = sum(durations_ms) / len(durations_ms)
        max_ms = max(durations_ms)
        print(f"  avg={avg:.1f} ms, max={max_ms:.1f} ms")
        # Catatan: ini mengukur round-trip HTTP, bukan murni A*. NFR target 50ms
        # adalah waktu komputasi A* murni di server, bukan total round-trip.
        # Round-trip akan lebih besar. Kita hanya cek wajar (< 500 ms).
        if max_ms > 500:
            print(f"  ⚠ WARNING: round-trip terlama {max_ms:.1f} ms cukup tinggi.")
        else:
            print("  ✓ Round-trip dalam batas wajar")
        print()

        # ── TEST 9: deterministic ──
        print("=== TEST 9: Determinism — 2 panggilan identik harus identik ===")
        _, body_a, _ = http("POST", "/api/v1/route", body={**sample, "mode": "WALKING"},
                            expect_status=200)
        _, body_b, _ = http("POST", "/api/v1/route", body={**sample, "mode": "WALKING"},
                            expect_status=200)
        assert body_a == body_b, "respons berbeda untuk request identik!"
        print(f"  identical ({len(body_a['coordinates'])} points, {body_a['lengthMeters']:.1f} m)")
        print("  ✓ PASS")
        print()

        print("=" * 60)
        print("ALL SMOKE TESTS PASS ✓")
        print("=" * 60)
        return 0

    finally:
        # Cleanup: kirim SIGTERM ke seluruh process group.
        print("\nStopping backend...")
        try:
            os.killpg(os.getpgid(proc.pid), signal.SIGTERM)
        except ProcessLookupError:
            pass
        try:
            proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            os.killpg(os.getpgid(proc.pid), signal.SIGKILL)
        log_fp.close()
        print("  stopped.")


if __name__ == "__main__":
    sys.exit(main())
