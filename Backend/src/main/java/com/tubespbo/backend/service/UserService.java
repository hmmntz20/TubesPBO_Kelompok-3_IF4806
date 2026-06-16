package com.tubespbo.backend.service;

import com.tubespbo.backend.model.User;
import com.tubespbo.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional; // <-- Ini yang tadi bikin error "Optional cannot be resolved"
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 1. Method Register
    public User register(String username, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar!");
        }

        User newUser = new User();
        newUser.setUserId(UUID.randomUUID().toString()); 
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password); 
        newUser.setLoginProvider(User.LoginProvider.LOCAL);
        
        newUser.setVerificationToken(UUID.randomUUID().toString()); 
        newUser.setTokenExpiredAt(LocalDateTime.now().plusMinutes(15)); 

        return userRepository.save(newUser); 
    }

    // 2. Method Verify Email
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (user.getTokenExpiredAt().isBefore(LocalDateTime.now())) {
                return false; 
            }
            
            user.setIsEmailVerified(true);
            user.setVerificationToken(null); 
            userRepository.save(user);
            return true;
        }
        return false; 
    }

    // 3. Method Resend Verification Email (Bonus pelengkap Controller)
    public void resendVerificationEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.getIsEmailVerified()) {
                // Buat token baru dan perpanjang waktu 15 menit
                user.setVerificationToken(UUID.randomUUID().toString());
                user.setTokenExpiredAt(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);
                
                // Catatan: Nanti logika kirim email beneran pakai Gmail ditaruh di sini
            } else {
                throw new RuntimeException("Email sudah diverifikasi sebelumnya.");
            }
        } else {
            throw new RuntimeException("Email tidak ditemukan.");
        }
    }

    public boolean loginLocal(String email, String password) {
        // Cari user berdasarkan email
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Lapis 1: Cek apakah akun ini terdaftar lewat Google
            if (user.getLoginProvider() != User.LoginProvider.LOCAL) {
                throw new RuntimeException("Gagal: Akun ini didaftarkan menggunakan Google. Silakan Login with Google.");
            }

            // Lapis 2: Cek apakah user sudah memverifikasi emailnya
            if (!user.getIsEmailVerified()) {
                throw new RuntimeException("Gagal: Email belum diverifikasi! Silakan cek email Anda untuk memasukkan kode token.");
            }

            // Lapis 3: Cek kecocokan password
            // (Catatan PBO: Nanti jika pakai enkripsi, gunakan passwordEncoder.matches() di sini)
            if (user.getPassword().equals(password)) {
                return true; // Semua lolos, Login Berhasil!
            } else {
                throw new RuntimeException("Gagal: Password salah!");
            }
        }
        
        throw new RuntimeException("Gagal: Email tidak terdaftar di sistem kami!");
    }

} // <-- Ini tutup kurung kurawal yang tadi hilang!