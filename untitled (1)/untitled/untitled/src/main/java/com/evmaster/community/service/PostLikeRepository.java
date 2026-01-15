package com.evmaster.community.service;

import com.evmaster.community.model.Post;
import com.evmaster.community.model.PostLike;
import com.evmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // Check if user already liked this post
    boolean existsByPostAndUser(Post post, User user);

    // Count likes for a post
    int countByPost(Post post);

    // Remove like
    void deleteByPostAndUser(Post post, User user);
}
