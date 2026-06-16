package com.tubespbo.backend.controller;

import com.tubespbo.backend.model.User;
import com.tubespbo.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam String username, 
                                          @RequestParam String email, 
                                          @RequestParam String password) {
        try {
            User registeredUser = userService.register(username, email, password);
            return ResponseEntity.ok("Registrasi berhasil! Silakan cek email Anda. Token Anda: " + registeredUser.getVerificationToken());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String token) {
        boolean isVerified = userService.verifyEmail(token);
        if (isVerified) {
            return ResponseEntity.ok("Email berhasil diverifikasi! Sekarang Anda bisa login.");
        } else {
            return ResponseEntity.badRequest().body("Token salah atau sudah kadaluwarsa.");
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendToken(@RequestParam String email) {
        userService.resendVerificationEmail(email);
        return ResponseEntity.ok("Email verifikasi baru telah dikirim.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String email, 
                                       @RequestParam String password) {
        try {
            boolean isSuccess = userService.loginLocal(email, password);
            if (isSuccess) {
                // Di dunia nyata, di sini kita me-return Token JWT (Session).
                // Tapi untuk saat ini, kita return pesan sukses saja.
                return ResponseEntity.ok("Login Berhasil! Selamat datang di aplikasi.");
            }
            return ResponseEntity.status(401).body("Login gagal karena alasan yang tidak diketahui.");
        } catch (Exception e) {
            // Menangkap pesan error dari UserService (misal: "Password salah!" atau "Email belum diverifikasi!")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}