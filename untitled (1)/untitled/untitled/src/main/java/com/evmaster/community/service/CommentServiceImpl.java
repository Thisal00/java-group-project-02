package com.evmaster.community.service;

import com.evmaster.community.model.Comment;
import com.evmaster.community.repository.CommentRepository;
import com.evmaster.model.User;
import com.evmaster.model.User.UserType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // ADD COMMENT 
    @Override
    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    //  GET COMMENTS FOR A POST 
    @Override
    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdAndHiddenFalse(postId);
    }

    // HIDE COMMENT (ONLY ADMIN) 
    @Override
    public void hideCommentSecure(Long commentId, User user) {

        if (user.getUserType() != UserType.SUPER_ADMIN) {
            throw new RuntimeException("Only admin can hide comments");
        }

        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        c.setHidden(true);
        commentRepository.save(c);
    }

  
    @Override
    public void deleteCommentSecure(Long commentId, User user) {

        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isOwner = c.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getUserType() == UserType.SUPER_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("You are not allowed to delete this comment");
        }

        commentRepository.deleteById(commentId);
    }
}
