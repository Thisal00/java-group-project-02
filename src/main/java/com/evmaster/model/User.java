package com.evmaster.model;

import com.evmaster.community.model.Post;
import com.evmaster.community.model.PostLike;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Main User entity
 */
@Entity
@Table(name = "EV_USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore  //  NEVER expose password in API
    private String password;

    @Column(nullable = false)
    private String nic;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    // User Type
    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType;

    // OTP fields
    @Column(name = "reset_otp")
    @JsonIgnore
    private String resetOtp;

    @Column(name = "otp_issued")
    private Boolean otpIssued = false;


    // RELATIONS (IGNORED IN JSON)


    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Post> posts;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<PostLike> likes;


    // ENUM


    public enum UserType {
        EV_OWNER,
        STATION_MANAGER,
        SUPER_ADMIN
    }

    // Signup constructor
    public User(String email, String password, String nic, String fullName, String phoneNumber, UserType userType) {
        this.email = email;
        this.password = password;
        this.nic = nic;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
    }
}
