package com.evmaster.community.service;

import com.evmaster.community.model.Comment;
import com.evmaster.model.User;

import java.util.List;

public interface CommentService {

    // ================= BASIC =================

    // add new comment
    Comment addComment(Comment comment);

    // get visible comments for a post
    List<Comment> getCommentsByPost(Long postId);


    // ================= SECURE OPERATIONS =================

    // hide comment (only admin)
    void hideCommentSecure(Long commentId, User user);

    // delete comment (owner or admin)
    void deleteCommentSecure(Long commentId, User user);
}
