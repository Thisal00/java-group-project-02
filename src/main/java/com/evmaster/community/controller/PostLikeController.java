package com.evmaster.community.controller;

import com.evmaster.community.model.Post;
import com.evmaster.community.service.PostLikeService;
import com.evmaster.community.service.PostService;
import com.evmaster.model.User;
import com.evmaster.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community")
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final PostService postService;
    private final UserRepository userRepository;

    public PostLikeController(PostLikeService postLikeService,
                              PostService postService,
                              UserRepository userRepository) {
        this.postLikeService = postLikeService;
        this.postService = postService;
        this.userRepository = userRepository;
    }


    // ️ TOGGLE LIKE

    @PostMapping("/post/{postId}/like")
    public int toggleLike(@PathVariable Long postId, Authentication auth) {

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Post post = postService.getPostById(postId);

        return postLikeService.toggleLike(post, user);
    }


    // GET LIKE COUNT

    @GetMapping("/post/{postId}/likes")
    public int getLikeCount(@PathVariable Long postId) {
        Post post = postService.getPostById(postId);
        return postLikeService.getLikeCount(post);
    }
}
