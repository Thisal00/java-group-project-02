package com.evmaster.community.service;

import com.evmaster.community.model.Post;
import com.evmaster.model.User;

import java.util.List;

public interface PostService {

    // BASIC

    // create or save post
    Post createPost(Post post);

    // get only visible posts (hidden = false)
    List<Post> getAllVisiblePosts();

    // get single post by id
    Post getPostById(Long id);


    // SECURE OPERATIONS

    // update post (only owner)
    Post updatePostSecure(Long postId, String content, User user);

    // hide post (only admin)
    void hidePostSecure(Long postId, User user);

    // delete post (owner or admin)
    void deletePostSecure(Long postId, User user);
}
