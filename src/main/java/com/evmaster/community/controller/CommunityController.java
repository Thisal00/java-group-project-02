package com.evmaster.community.controller;

import com.evmaster.community.model.Comment;
import com.evmaster.community.model.Post;
import com.evmaster.community.service.CommentService;
import com.evmaster.community.service.PostService;
import com.evmaster.model.User;
import com.evmaster.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/community")
public class CommunityController {

    private final PostService postService;
    private final CommentService commentService;
    private final UserRepository userRepository;

    public CommunityController(
            PostService postService,
            CommentService commentService,
            UserRepository userRepository
    ) {
        this.postService = postService;
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    // ===================== GET LOGGED USER =====================
    private User getLoggedUser(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    // ===================== CREATE TEXT POST =====================
    @PostMapping("/post")
    public Post createPost(@RequestParam String content, Authentication auth) {
        User user = getLoggedUser(auth);

        Post post = new Post();
        post.setUser(user);
        post.setContent(content);

        return postService.createPost(post);
    }

    // ===================== CREATE POST WITH IMAGE =====================
    @PostMapping("/post-with-image")
    public Post createPostWithImage(
            @RequestParam String content,
            @RequestParam MultipartFile image,
            Authentication auth
    ) throws Exception {

        User user = getLoggedUser(auth);

        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, image.getBytes());

        Post post = new Post();
        post.setUser(user);
        post.setContent(content);
        post.setImagePath("/uploads/" + fileName);

        return postService.createPost(post);
    }

    // ===================== GET ALL VISIBLE POSTS =====================
    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        return postService.getAllVisiblePosts();
    }

    // ===================== DELETE POST =====================
    @DeleteMapping("/post/{id}")
    public void deletePost(@PathVariable Long id, Authentication auth) {
        User user = getLoggedUser(auth);
        postService.deletePostSecure(id, user);
    }

    // ===================== EDIT POST =====================
    @PutMapping("/post/{id}")
    public Post editPost(
            @PathVariable Long id,
            @RequestParam String content,
            Authentication auth
    ) {
        User user = getLoggedUser(auth);
        return postService.updatePostSecure(id, content, user);
    }

    //  HIDE POST (ADMIN)
    @PostMapping("/post/{id}/hide")
    public void hidePost(@PathVariable Long id, Authentication auth) {
        User user = getLoggedUser(auth);
        postService.hidePostSecure(id, user);
    }

    //  ADD COMMENT
    @PostMapping("/post/{postId}/comment")
    public Comment addComment(
            @PathVariable Long postId,
            @RequestParam String text,
            Authentication auth
    ) {

        User user = getLoggedUser(auth);
        Post post = postService.getPostById(postId);

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setText(text);

        return commentService.addComment(comment);
    }

    // GET COMMENTS
    @GetMapping("/post/{postId}/comments")
    public List<Comment> getComments(@PathVariable Long postId) {
        return commentService.getCommentsByPost(postId);
    }

    //  DELETE COMMENT
    @DeleteMapping("/comment/{id}")
    public void deleteComment(@PathVariable Long id, Authentication auth) {
        User user = getLoggedUser(auth);
        commentService.deleteCommentSecure(id, user);
    }

    //  HIDE COMMENT (ADMIN)
    @PostMapping("/comment/{id}/hide")
    public void hideComment(@PathVariable Long id, Authentication auth) {
        User user = getLoggedUser(auth);
        commentService.hideCommentSecure(id, user);
    }
}
