package com.evmaster.community.service;

import com.evmaster.community.model.Post;
import com.evmaster.community.repository.PostRepository;
import com.evmaster.model.User;
import com.evmaster.model.User.UserType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // CREATE
    @Override
    public Post createPost(Post post) {
        post.setHidden(false); // always visible by default
        return postRepository.save(post);
    }

    //  GET ALL VISIBLE POSTS
    @Override
    public List<Post> getAllVisiblePosts() {
        return postRepository.findByHiddenFalseOrderByCreatedAtDesc();
    }

    // GET POST BY ID
    @Override
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    //  UPDATE POST (ONLY OWNER) 
    @Override
    public Post updatePostSecure(Long postId, String content, User user) {

        Post post = getPostById(postId);

        // check ownership
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to edit this post");
        }

        post.setContent(content);
        return postRepository.save(post);
    }

    //  HIDE POST (ONLY ADMIN)
    @Override
    public void hidePostSecure(Long postId, User user) {

        // check admin
        if (user.getUserType() != UserType.SUPER_ADMIN) {
            throw new RuntimeException("Only admin can hide posts");
        }

        Post post = getPostById(postId);
        post.setHidden(true);
        postRepository.save(post);
    }

    // DELETE POST
    @Override
    public void deletePostSecure(Long postId, User user) {

        Post post = getPostById(postId);

        boolean isOwner = post.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getUserType() == UserType.SUPER_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("You are not allowed to delete this post");
        }

        postRepository.delete(post);
    }
}
