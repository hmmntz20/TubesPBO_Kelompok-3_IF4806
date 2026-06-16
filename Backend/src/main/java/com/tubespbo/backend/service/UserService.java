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
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    // 1. Method Register
    public User register(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar!");
        }

        if (user.getUserId() == null) {
            user.setUserId(UUID.randomUUID().toString());
        }

        user.setLoginProvider(User.LoginProvider.LOCAL);

        String token = UUID.randomUUID().toString();

        user.setVerificationToken(token);
        user.setTokenExpiredAt(LocalDateTime.now().plusMinutes(15));

        User savedUser = userRepository.save(user);

        String verificationLink = "http://localhost:8080/api/users/verify?token=" + token;
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationLink);
        return savedUser;

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
                String newLink = "http://localhost:8080/api/users/verify?token=" + user.getVerificationToken();
                emailService.sendVerificationEmail(user.getEmail(), newLink);
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

    // --------------------------------------------------------
    // FITUR UPDATE PROFILE (USERNAME & PHOTO)
    // Berlaku untuk akun LOCAL maupun GOOGLE
    // --------------------------------------------------------
    public User updateProfile(String email, String newUsername, String newProfilePhoto) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Update username jika diisi
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                user.setUsername(newUsername);
            }
            
            // Update profile photo jika diisi
            if (newProfilePhoto != null && !newProfilePhoto.trim().isEmpty()) {
                user.setProfilePhoto(newProfilePhoto);
            }
            
            return userRepository.save(user);
        }
        throw new RuntimeException("Gagal: User dengan email tersebut tidak ditemukan.");
    }

    // --------------------------------------------------------
    // FITUR UPDATE PASSWORD
    // HANYA berlaku untuk akun LOCAL
    // --------------------------------------------------------
    public boolean updatePassword(String email, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Validasi Provider: Tolak jika ini akun Google
            if (user.getLoginProvider() == User.LoginProvider.GOOGLE) {
                throw new RuntimeException("Gagal: Akun Google tidak dapat mengubah password melalui aplikasi ini.");
            }
            
            // Validasi Password Lama
            if (!user.getPassword().equals(oldPassword)) {
                throw new RuntimeException("Gagal: Password lama yang Anda masukkan salah!");
            }
            
            // Update Password Baru
            user.setPassword(newPassword);
            userRepository.save(user);
            return true;
        }
        throw new RuntimeException("Gagal: User dengan email tersebut tidak ditemukan.");
    }

    // --------------------------------------------------------
    // FITUR LOGIN GOOGLE
    // --------------------------------------------------------
    public User loginGoogle(String email, String username, String profilePhoto) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            // Jika user sudah ada, langsung kembalikan datanya (Login)
            return userOpt.get();
        } else {
            // Jika belum ada, otomatis daftarkan (Register by Google)
            User newUser = new User();
            newUser.setUserId(UUID.randomUUID().toString());
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setProfilePhoto(profilePhoto);
            newUser.setLoginProvider(User.LoginProvider.GOOGLE);
            newUser.setIsEmailVerified(true); // Google pastinya sudah verified
            return userRepository.save(newUser);
        }
    }

} // <-- Ini tutup kurung kurawal yang tadi hilang!