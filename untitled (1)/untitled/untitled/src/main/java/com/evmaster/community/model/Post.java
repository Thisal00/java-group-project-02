package com.evmaster.community.model;

import com.evmaster.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    //  POST OWNER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore   // 🔥 Prevent infinite JSON loop
    private User user;

  
    //  CONTENT
    @Column(length = 2000, nullable = false)
    private String content;

  
    //  IMAGE
    @Column(name = "image_path")
    private String imagePath;


    // HIDDEN BY ADMIN
    @Column(nullable = false)
    private boolean hidden = false;

  
    // CREATED TIME
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

  
    // LIKES
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PostLike> likes = new ArrayList<>();

 
    //  COMMENTS
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();


    // VIRTUAL FIELDS FOR API

    @Transient
    public int getLikeCount() {
        return likes == null ? 0 : likes.size();
    }

    @Transient
    public int getCommentCount() {
        return comments == null ? 0 : comments.size();
    }

    // SEND USER NAME SAFELY TO FRONTEND
    @Transient
    public String getUserName() {
        if (user == null) return "User";
        if (user.getFullName() != null && !user.getFullName().isEmpty()) {
            return user.getFullName();
        }
        return user.getEmail();
    }

    // SEND USER EMAIL IF NEEDED
    @Transient
    public String getUserEmail() {
        if (user == null) return "";
        return user.getEmail();
    }

 
    // GETTERS & SETTERS


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PostLike> getLikes() {
        return likes;
    }

    public void setLikes(List<PostLike> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}


