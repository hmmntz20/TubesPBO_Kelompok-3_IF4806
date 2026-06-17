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
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // Panggil service untuk menyimpan user yang dikirim via JSON body
            User registeredUser = userService.register(user);
            return ResponseEntity.ok("Registrasi berhasil!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/verify")
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
            User user = userService.loginLocal(email, password); 
            
            return ResponseEntity.ok(user); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login-google")
    public ResponseEntity<?> loginGoogleUser(@RequestParam String email, 
                                             @RequestParam String username,
                                             @RequestParam(required = false) String profilePhoto) {
        try {
            User user = userService.loginGoogle(email, username, profilePhoto);
            
            // PENTING: Kembalikan objek user-nya juga!
            return ResponseEntity.ok(user); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
// Endpoint untuk Ubah Username dan Profile Photo
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestParam String email, 
                                           @RequestParam(required = false) String newUsername,
                                           @RequestParam(required = false) String newProfilePhoto) {
        try {
            User updatedUser = userService.updateProfile(email, newUsername, newProfilePhoto);
            return ResponseEntity.ok("Profil berhasil diperbarui!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint untuk Ubah Password
    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestParam String email, 
                                            @RequestParam String oldPassword,
                                            @RequestParam String newPassword) {
        try {
            boolean isSuccess = userService.updatePassword(email, oldPassword, newPassword);
            if (isSuccess) {
                return ResponseEntity.ok("Password berhasil diubah!");
            }
            return ResponseEntity.badRequest().body("Gagal mengubah password.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}