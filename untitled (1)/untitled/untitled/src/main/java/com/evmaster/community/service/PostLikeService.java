package com.evmaster.community.service;

import com.evmaster.community.model.Post;
import com.evmaster.community.model.PostLike;
import com.evmaster.community.repository.PostLikeRepository;
import com.evmaster.model.User;
import org.springframework.stereotype.Service;

@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    public PostLikeService(PostLikeRepository postLikeRepository) {
        this.postLikeRepository = postLikeRepository;
    }

    // Toggle Like / Unlike
    public int toggleLike(Post post, User user) {

        boolean alreadyLiked = postLikeRepository.existsByPostAndUser(post, user);

        if (alreadyLiked) {
            // Unlike
            postLikeRepository.deleteByPostAndUser(post, user);
        } else {
            // Like
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(user);
            postLikeRepository.save(like);
        }

        // Return new like count
        return postLikeRepository.countByPost(post);
    }

    // Get like count
    public int getLikeCount(Post post) {
        return postLikeRepository.countByPost(post);
    }

    // Check user liked or not
    public boolean isLikedByUser(Post post, User user) {
        return postLikeRepository.existsByPostAndUser(post, user);
    }
}
