package com.tubespbo.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;

    private String username;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    
    @Column(name = "profile_photo")
    private String profilePhoto;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_provider")
    private LoginProvider loginProvider;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "token_expired_at")
    private LocalDateTime tokenExpiredAt;

    public enum LoginProvider {
        LOCAL, GOOGLE
    }

    // ==========================================
    // GETTER & SETTER MANUAL (ANTI-ERROR)
    // ==========================================
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public LoginProvider getLoginProvider() { return loginProvider; }
    public void setLoginProvider(LoginProvider loginProvider) { this.loginProvider = loginProvider; }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public LocalDateTime getTokenExpiredAt() { return tokenExpiredAt; }
    public void setTokenExpiredAt(LocalDateTime tokenExpiredAt) { this.tokenExpiredAt = tokenExpiredAt; }
}